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
import model.Credentials.BotToken
import model.{ChatId, Keyboards}
import services.PlaceHunterService

class PlaceHunterBot[F[_]: Async : ContextShift](token: BotToken,
                                                 placeHunterService: PlaceHunterService[F])
  extends AbstractBot[F](token)
    with Polling[F]
    with Commands[F]
    with RegexCommands[F] {

  // Provide a keyboard on search
  onCommand("search") { implicit msg: Message =>
    logger.debug(s"Search command: chatID=${msg.chat.id}")
    reply("What are you looking for?", replyMarkup = Keyboards.placeTypes).void
  }

  // Requests location
  onRegex(Keyboards.placeRegex) { implicit msg: Message =>
    _ =>
      adaptError {
        for {
          _ <- placeHunterService.savePlace(ChatId(msg.chat.id), msg.text)
          _ <- reply("Can you please send your current location?", replyMarkup = Keyboards.shareLocation)
        } yield ()
      }
  }

  // Process absolutely all messages and reply on received location
  onMessage { implicit msg: Message =>
    logger.info(s"Received message: chatID=${msg.chat.id}, from=${msg.from}, text=${msg.text}, location=${msg.location}")
    msg.location match {
      case Some(location) =>
        val chatId = ChatId(msg.chat.id)
        adaptError {
          for {
            searchRequest <- placeHunterService.saveLocation(chatId, location)
            response <- placeHunterService.searchForPlaces(chatId, searchRequest)
            _ <- replyMd(response.show, replyMarkup = Keyboards.removeKeyBoard).void
          } yield ()
        }
      case None => ().pure[F]
    }
  }

  // Log the error and rethrow it back.
  def adaptError[T](block: F[T]): F[T] = {
    block adaptErr {
      case error =>
        logger.error(s"Error ${error.getClass}, the message is ${error.getMessage}.")
        error
    }
  }
}
