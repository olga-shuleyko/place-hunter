import bot.PlaceHunterBot
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.hikari.HikariTransactor
import model.Credentials._
import org.http4s.client.Client

object Launcher extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    BotModule.createServer[IO]().flatMap(_ => BotModule.createClient[IO]()).use { client =>
      BotModule.createTransactor[IO]().use { transactor =>
        for {
          bot <- bot[IO](client, transactor)
          _ <- bot.startPolling
        } yield ExitCode.Success
      }
    }

  private def bot[F[_] : ConcurrentEffect : ContextShift]
  (client: Client[F], transactor: HikariTransactor[F]): F[PlaceHunterBot[F]] =
    {
      for {
        token <- BotModule.readEnvironmentVariable("PLACE_HUNTER_BOT_TOKEN")
        apiKey <- BotModule.readEnvironmentVariable("GOOGLE_API_KEY")
        credentials = BotKeys(BotToken(token), PlacesAPIKey(apiKey))
        bot <- new BotModule[F](BotToken(token), client, credentials, transactor).run
      } yield bot
    }
}
