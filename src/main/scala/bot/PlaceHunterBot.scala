package bot

import cats.Traverse
import cats.data.OptionT
import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicativeError._
import cats.syntax.applicative._
import cats.syntax.show._
import cats.syntax.option._
import com.bot4s.telegram.models.ReplyMarkup
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.Polling
import com.bot4s.telegram.methods.SendLocation
import com.bot4s.telegram.models.Message
import io.chrisdavenport.log4cats.Logger
import model.ClientError.{DistanceIsIncorrect, PlaceTypeIsIncorrect}
import model.Credentials.BotToken
import model.GooglePlacesResponseModel.{FromIndex, Response, Result}
import model.{ChatId, Distance, Keyboards, Likes, NextResults, PlaceType}
import services.PlaceHunterService
import util.GooglePlacesAPI.linkToPlace
import util.{BotQuestions, Util}

class PlaceHunterBot[F[_]: Async : ContextShift: Logger](token: BotToken,
                                                         placeHunterService: PlaceHunterService[F])
  extends AbstractBot[F](token)
    with Polling[F]
    with Commands[F]
    with RegexCommands[F] {

  // Provide a keyboard on search
  onCommand("search") { implicit msg: Message =>
    attempt {
      Logger[F].info(s"Search command: chatID=${msg.chat.id}") >>
        placeHunterService.clearStorage(ChatId(msg.chat.id)) >> requestPlaceType
    }
  }

  // Requests distance
  onRegex(Keyboards.placeRegex) { implicit msg: Message =>
    _ =>
      attempt {
        val chatId = ChatId(msg.chat.id)
        PlaceType.parse(msg.text).map { placeType =>
          placeHunterService.savePlace(chatId, placeType) >> requestDistance
        }.getOrElse(PlaceTypeIsIncorrect(chatId).raiseError)
      }
  }

  // Requests location
  onRegex(Keyboards.distancesRegex) { implicit msg: Message =>
    _ =>
      attempt {
        val chatId = ChatId(msg.chat.id)
        Distance.parse(msg.text).map { distance =>
          placeHunterService.saveDistance(chatId, distance) >> requestLocation
        }.getOrElse(DistanceIsIncorrect(chatId).raiseError)
      }
  }

  // Process absolutely all messages and reply on received location
  onMessage { implicit msg: Message =>
    attempt {
      import cats.instances.option._
      Logger[F].info(s"Received message:chatID=${msg.chat.id},from=${msg.from},text=${msg.text},location=${msg.location}") >>
        Traverse[Option].traverse(msg.location) { location =>
          for {
            result <- placeHunterService.searchForPlaces(ChatId(msg.chat.id), location)
            _ <- replyWithSearchResults(result)
          } yield ()
        }
    }
  }

  onRegex(Keyboards.likesRegex) { implicit msg: Message =>
    _ =>
      attempt {
        for {
          likeNumber <- OptionT.fromOption(Likes.parse(msg.text))
          result <- OptionT(placeHunterService.stopSearch(ChatId(msg.chat.id), likeNumber.some))
          _ <- OptionT.liftF(replyOnStop(result))
        } yield ()
      }
  }

  onRegex(Keyboards.dislikeRegex) { implicit msg: Message =>
    _ =>
      attempt {
        placeHunterService.stopSearch(ChatId(msg.chat.id), none) >> replyOnDislike
      }
  }

  onRegex(Keyboards.nextResultsRegex) { implicit msg: Message =>
    _ =>
      attempt {
        for {
          (from, until) <- OptionT.fromOption(NextResults.parse(msg.text))
          start = from - 1
          result <- OptionT(placeHunterService.searchForPlaces(ChatId(msg.chat.id), start, until))
          _ <- OptionT.liftF(replyWithSearchResults(result, start, until))
        } yield ()
      }
  }

  // Provide a keyboard on search
  onCommand("chosen_places") { implicit msg: Message =>
    attempt {
      for {
        res <- placeHunterService.loadChosenPlaces(ChatId(msg.chat.id))
        _ <- replyChosenPlaces(res)
      } yield ()
    }
  }

  private def replyWithSearchResults(response: Response, from: Int = 0, until: Int = Util.numberOfReplies)
                                    (implicit message: Message) = {
    val responseSize = response.size
    val nextTo = if (responseSize > until) (until, Math.min(until + Util.numberOfReplies, responseSize)).some else none
    val remainingAmount = Math.min(until, responseSize)

    val replyResults = replySearchResults(response, from)
    if (remainingAmount > 0)
      replyResults >>
        reply(BotQuestions.selectResult, replyMarkup = Keyboards.likesKeyboard((1 to remainingAmount).toList, nextTo))
    else replyResults
  }

  private def replySearchResults(response: Response, from: Int)(implicit message: Message) = {
    implicit val fromIndex: FromIndex = FromIndex(from)
    val (messageToReply, buttonsToReply: Option[ReplyMarkup]) =
      if (response.searchResponse.results.isEmpty)
        (BotQuestions.nothingToRecommend, Keyboards.removeKeyBoard)
      else
        (BotQuestions.recommends + response.searchResponse.show, Keyboards.inlineKeyboardButtons(response.buttons))
    Logger[F].info(s"ChatId=${message.chat.id}, SearchResult is $messageToReply") >>
      replyMd(messageToReply, replyMarkup = buttonsToReply)
  }

  private def replyChosenPlaces(res: List[Result])(implicit message: Message) = {
    val text = if (res.isEmpty) BotQuestions.noChosenPlaces else {
      BotQuestions.chosenPlaces + res.map { entry =>
        val link = linkToPlace(entry.placeId, entry.name)
        s"[${entry.name}]($link)"
      }.mkString("\n")
    }
    replyMd(text)
  }

  private def requestLocation(implicit msg: Message) =
    reply(BotQuestions.location, replyMarkup = Keyboards.shareLocation)

  private def requestDistance(implicit msg: Message) =
    reply(BotQuestions.distance, replyMarkup = Keyboards.distance)

  private def requestPlaceType(implicit msg: Message) =
    reply(BotQuestions.place, replyMarkup = Keyboards.placeTypes)

  private def replyOnStop(result: Result)(implicit msg: Message) =
    replyMd(BotQuestions.finishSearch + result.show + BotQuestions.newSearch, replyMarkup = Keyboards.removeKeyBoard) >>
      request(SendLocation(msg.chat.id, result.geometry.location.lat, result.geometry.location.lng))

  private def replyOnDislike(implicit msg: Message) =
    reply(BotQuestions.dislikeSearch, replyMarkup = Keyboards.removeKeyBoard)

  // Log the error and rethrow it back.
  private def attempt[T](block: F[T]): F[Unit] =
    (block onError {
      case error =>
        Logger[F].error(s"Error ${error.getClass}, the message is ${error.getMessage}.")
    }).void

  private def attempt[T, A](block: OptionT[F, A]): F[Unit] = attempt(block.value)
}
