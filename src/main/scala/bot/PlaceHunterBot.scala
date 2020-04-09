package bot

import cats.effect.{Async, ContextShift}
import cats.syntax.functor._
import cats.syntax.applicative._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.Polling
import com.bot4s.telegram.models.Message

class PlaceHunterBot[F[_]: Async : ContextShift](token: String)
  extends AbstractBot[F](token)
    with Polling[F]
    with Commands[F]
    with RegexCommands[F] {

  // Provide a keyboard on search
  onCommand("search") { implicit msg: Message =>
    reply("What are you looking for?", replyMarkup = Keyboards.placeTypes).void
  }

  // Requests location
  onRegex(Keyboards.placeRegex) { implicit msg: Message =>
    _ =>
      reply("Can you please send your current location?", replyMarkup = Keyboards.shareLocation).void
  }

  // Process all messages and reply on received location
  onMessage { implicit msg: Message =>
    msg.location match {
      case Some(location) =>
        reply(s"Thanks for your location $location", replyMarkup = Keyboards.removeKeyBoard).void
      case None => ().pure[F]
    }
  }
}
