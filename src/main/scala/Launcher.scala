import bot.PlaceHunterBot
import cats.effect.{ExitCode, IO, IOApp}

object Launcher extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      token <- IO(System.getenv("PLACE_HUNTER_BOT_TOKEN"))
      bot <- new PlaceHunterBot[IO](token).startPolling.map(_ => ExitCode.Success)
    } yield bot
}
