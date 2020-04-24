package bot

import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicativeError._
import cats.syntax.applicative._
import cats.syntax.show._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.Polling
import com.bot4s.telegram.models.Message
import io.chrisdavenport.log4cats.Logger
import model.Credentials.BotToken
import model.{ChatId, Keyboards}
import services.PlaceHunterService
import util.BotQuestions

class PlaceHunterBot[F[_]: Async : ContextShift: Logger](token: BotToken,
                                                 placeHunterService: PlaceHunterService[F])
  extends AbstractBot[F](token)
    with Polling[F]
    with Commands[F]
    with RegexCommands[F] {

  // Provide a keyboard on search
  onCommand("search") { implicit msg: Message =>
    Logger[F].debug(s"Search command: chatID=${msg.chat.id}") >>
      reply(BotQuestions.place, replyMarkup = Keyboards.placeTypes).void
  }

  // Requests distance
  onRegex(Keyboards.placeRegex) { implicit msg: Message =>
    _ =>
      onError {
        for {
          _ <- placeHunterService.savePlace(ChatId(msg.chat.id), msg.text)
          _ <- reply(BotQuestions.distance, replyMarkup = Keyboards.distance)
        } yield ()
      }
  }

  // Requests location
  onRegex(Keyboards.distancesRegex) { implicit msg: Message =>
    _ =>
      onError {
        for {
          _ <- placeHunterService.saveDistance(ChatId(msg.chat.id), msg.text)
          _ <- reply(BotQuestions.location, replyMarkup = Keyboards.shareLocation)
        } yield ()
      }
  }

  // Process absolutely all messages and reply on received location
  onMessage { implicit msg: Message =>
    Logger[F].info(s"Received message: chatID=${msg.chat.id}, from=${msg.from}, text=${msg.text}, location=${msg.location}"
    ) >> {
      msg.location match {
        case Some(location) =>
          val chatId = ChatId(msg.chat.id)
          onError {
            for {
              response <- placeHunterService.searchForPlaces(chatId, location)
              messageToReply = if (response.results.isEmpty)
                BotQuestions.nothingToRecommend else BotQuestions.recommends + response.show
              _ <- Logger[F].info(s"ChatId=$chatId, Search result is $messageToReply")
              _ <- replyMd(messageToReply, replyMarkup = Keyboards.removeKeyBoard).void
            } yield ()
          }
        case None => ().pure[F]
      }
    }
  }

  // Log the error and rethrow it back.
  def onError[T](block: F[T]): F[T] = {
    block onError {
      case error =>
        Logger[F].error(s"Error ${error.getClass}, the message is ${error.getMessage}.")
    }
  }
}
