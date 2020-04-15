import bot.PlaceHunterBot
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import model.Credentials._
import model.{ChatId, SearchRequest}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

object Launcher extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    createClient[IO]().use { client =>
      for {
        bot <- bot[IO](client)
        _ <- bot.startPolling
      } yield ExitCode.Success
    }

  private def createClient[F[_] : ConcurrentEffect](): Resource[F, Client[F]] =
    BlazeClientBuilder[F](global).resource

  private def bot[F[_] : ConcurrentEffect : ContextShift](client: Client[F]): F[PlaceHunterBot[F]] =
    for {
      token <- System.getenv("PLACE_HUNTER_BOT_TOKEN").pure[F]
      apiKey <- System.getenv("GOOGLE_API_KEY").pure[F]
      credentials = BotKeys(BotToken(token), PlacesAPIKey(apiKey))
      requests <- Ref.of[F, Map[ChatId, SearchRequest]](Map.empty[ChatId, SearchRequest])
      mod <- new BotModule[F](BotToken(token), requests, client, credentials).pure[F]
    } yield mod.bot
}
