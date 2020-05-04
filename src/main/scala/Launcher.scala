import bot.PlaceHunterBot
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.config.ConfigFactory
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import model.Credentials._
import model.GooglePlacesResponseModel.SearchResponse
import model.{ChatId, JdbcConfig, SearchRequest}
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
    {
      for {
        logger <- Slf4jLogger.create[F]
        token <- Sync[F].delay(System.getenv("PLACE_HUNTER_BOT_TOKEN"))
        apiKey <- Sync[F].delay(System.getenv("GOOGLE_API_KEY"))
        jdbc <- readConfiguration()
        credentials = BotKeys(BotToken(token), PlacesAPIKey(apiKey))
        requests <- Ref.of[F, Map[ChatId, SearchRequest]](Map.empty[ChatId, SearchRequest])
        responses <- Ref.of[F, Map[ChatId, SearchResponse]](Map.empty[ChatId, SearchResponse])
        mod <- Sync[F].delay(new BotModule[F](BotToken(token), requests, responses, client, credentials, logger, jdbc))
        _ <- mod.init()
      } yield mod.bot
    }

  private def readConfiguration[F[_]: Sync](): F[JdbcConfig] = {
    Sync[F].delay {
      val config = ConfigFactory.load().getConfig("database")
      JdbcConfig(
        url = config.getString("url"),
        user = config.getString("user"),
        password = config.getString("password"),
        driver = "com.mysql.cj.jdbc.Driver"
      )
    }
  }
}
