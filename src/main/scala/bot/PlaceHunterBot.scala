package bot

import cats.Traverse
import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicativeError._
import cats.syntax.applicative._
import cats.syntax.show._
import cats.syntax.option._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.Polling
import com.bot4s.telegram.methods.SendLocation
import com.bot4s.telegram.models.Message
import model.ClientError.LikeNumberIsIncorrect
import model.Credentials.BotToken
import model.GooglePlacesResponseModel.{FromIndex, Response}
import model.{ChatId, Keyboards, Likes, NextResults}
import services.PlaceHunterService
import util.BotQuestions

class PlaceHunterBot[F[_]: Async : ContextShift](token: BotToken,
                                                 placeHunterService: PlaceHunterService[F])
  extends AbstractBot[F](token)
    with Polling[F]
    with Commands[F]
    with RegexCommands[F] {

  // Provide a keyboard on search
  onCommand("search") { implicit msg: Message =>
    onError {
      logger.debug(s"Search command: chatID=${msg.chat.id}").pure >>
        placeHunterService.clearStorage(ChatId(msg.chat.id)) >>
        reply(BotQuestions.place, replyMarkup = Keyboards.placeTypes).void
    }
  }

  // Requests distance
  onRegex(Keyboards.placeRegex) { implicit msg: Message =>
    _ =>
      onError {
        placeHunterService.savePlace(ChatId(msg.chat.id), msg.text) >>
          reply(BotQuestions.distance, replyMarkup = Keyboards.distance).void
      }
  }

  // Requests location
  onRegex(Keyboards.distancesRegex) { implicit msg: Message =>
    _ =>
      onError {
        placeHunterService.saveDistance(ChatId(msg.chat.id), msg.text) >>
          reply(BotQuestions.location, replyMarkup = Keyboards.shareLocation).void
      }
  }

  // Process absolutely all messages and reply on received location
  onMessage { implicit msg: Message =>
    logger.info(s"Received message: chatID=${msg.chat.id}, from=${msg.from}, text=${msg.text}, location=${msg.location}"
    ).pure >> {
      msg.location match {
        case Some(location) =>
          onError {
            for {
              response <- placeHunterService.searchForPlaces(ChatId(msg.chat.id), location)
              _ <- replySearchResults(response)
            } yield ()
          }
        case None => ().pure[F]
      }
    }
  }

  onRegex(Keyboards.likesRegex) { implicit msg: Message =>
    _ =>
      val chatId = ChatId(msg.chat.id)
      onError {
        Likes.parse(msg.text).map { like =>
          import cats.instances.option._
          for {
            response <- placeHunterService.stopSearch(chatId, like.some)
            _ <- Traverse[Option].traverse(response) { result =>
              replyMd(BotQuestions.finishSearch + result.show + BotQuestions.newSearch, replyMarkup = Keyboards.removeKeyBoard) >>
                request(SendLocation(msg.chat.id, result.geometry.location.lat, result.geometry.location.lng))
            }
          } yield ()
        }.fold(LikeNumberIsIncorrect(chatId).raiseError[F, Unit])(identity)
      }
  }

  onRegex(Keyboards.dislikeRegex) { implicit msg: Message =>
    _ =>
      val chatId = ChatId(msg.chat.id)
      onError {
        placeHunterService.stopSearch(chatId, none) >>
          reply(BotQuestions.dislikeSearch, replyMarkup = Keyboards.removeKeyBoard).void
      }
  }

  onRegex(Keyboards.nextResultsRegex) { implicit msg: Message =>
    _ =>
      val chatId = ChatId(msg.chat.id)
      onError {
        NextResults.parse(msg.text).map { case (from, until) =>
          import cats.instances.option._
          val start = from - 1
          for {
            response <- placeHunterService.searchForPlaces(ChatId(msg.chat.id), start, until)
            _ <- Traverse[Option].traverse(response)(replySearchResults(_, start, until))
          } yield ()

        }.fold(LikeNumberIsIncorrect(chatId).raiseError[F, Unit])(identity)
      }
  }

  private def replySearchResults(response: Response, from: Int = 0, until: Int = 5)(implicit message: Message): F[Unit] = {
    implicit val fromIndex: FromIndex = FromIndex(from)
    val (messageToReply, buttonsToReply) =
      if (response.searchResponse.results.isEmpty)
        (BotQuestions.nothingToRecommend, Keyboards.removeKeyBoard)
      else
        (BotQuestions.recommends + response.searchResponse.show, Keyboards.inlineKeyboardButtons(response.buttons))
    val responseSize = response.size
    val nextTo = if (responseSize > until) (until, Math.min(until + 5, responseSize)).some else none
    val likeNum = Math.min(until, responseSize)

    val result = logger.info(s"ChatId=${message.chat.id}, Search result is $messageToReply").pure[F] >>
      replyMd(messageToReply, replyMarkup = buttonsToReply)
    if (likeNum > 0)
      result >>
        reply(BotQuestions.selectResult, replyMarkup = Keyboards.likesKeyboard((1 to likeNum).toList, nextTo)).void
    else result.void
  }

  // Log the error and rethrow it back.
  def onError[T](block: F[T]): F[T] = {
    block onError {
      case error =>
        logger.error(s"Error ${error.getClass}, the message is ${error.getMessage}.").pure
    }
  }
}
