import bot.PlaceHunterBot
import cats.effect.concurrent.Ref
import cats.effect.{Async, ContextShift, ExitCode, IO, IOApp}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import model.{ChatId, SearchRequest, Token}

object Launcher extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      bot <- bot[IO]
      _ <- bot.startPolling
    } yield ExitCode.Success


  def bot[F[_]: Async: ContextShift]: F[PlaceHunterBot[F]] =
    for {
      token <- System.getenv("PLACE_HUNTER_BOT_TOKEN").pure[F]
      requests <- Ref.of[F, Map[ChatId, SearchRequest]](Map.empty[ChatId, SearchRequest])
      mod <- new BotModule[F](Token(token), requests).pure[F]
    } yield mod.bot
}
