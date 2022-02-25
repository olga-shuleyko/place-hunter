import bot.PlaceHunterBot
import cats.effect.concurrent.Ref
import cats.effect.{Async, ConcurrentEffect, ContextShift, Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import com.typesafe.config.ConfigFactory
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import model.ClientError.EnvVarIsMissing
import model.Credentials.{BotKeys, BotToken}
import model.GooglePlacesResponseModel.SearchResponse
import model.{ChatId, JdbcConfig, SearchRequest}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import places.api.GooglePlacesApi
import repositories.{DoobieChosenPlacesRepository, DoobieSchemaManager, InMemorySearchRepository, InMemorySearchResponseRepository}
import services.PlaceHunterServiceImpl

class BotModule[F[_]: ConcurrentEffect: ContextShift](token: BotToken,
                                                      httpClient: Client[F],
                                                      credentials: BotKeys,
                                                      transactor: HikariTransactor[F]) {

  def run: F[PlaceHunterBot[F]] = {
    val S = Sync[F]
    for {
      logger <- Slf4jLogger.create[F]
      searchRequestRepo <- inMemorySearchRepository()
      searchResponseRepo <- inMemorySearchResponseRepository()
      chosenPlaceRepo <- S.delay(new DoobieChosenPlacesRepository(transactor))
      schemaManager <- S.delay(new DoobieSchemaManager(transactor))
      // or Sync[F].delay(new MockGooglePlacesAPI)
      placesApi <- S.delay(new GooglePlacesApi(httpClient, credentials))
      placeHunterService <- S.delay(
        new PlaceHunterServiceImpl(
          searchRequestRepo,
          searchResponseRepo,
          chosenPlaceRepo,
          placesApi)
      )
      bot <- S.delay(new PlaceHunterBot(token, logger, placeHunterService))
      _ <- schemaManager.createSchema()
    } yield bot
  }

  private def inMemorySearchRepository(): F[InMemorySearchRepository[F]] =
    Ref.of[F, Map[ChatId, SearchRequest]](Map.empty[ChatId, SearchRequest])
      .map(new InMemorySearchRepository(_))

  private def inMemorySearchResponseRepository() =
    Ref.of[F, Map[ChatId, SearchResponse]](Map.empty[ChatId, SearchResponse])
      .map(new InMemorySearchResponseRepository(_))
}

object BotModule {
  private def readConfiguration[F[_] : Sync](): Resource[F, JdbcConfig] = {
    Resource.liftF {
      Sync[F].delay {
        val config = ConfigFactory.load().getConfig("database")
        JdbcConfig(
          url = config.getString("url"),
          username = config.getString("user"),
          password = config.getString("password"),
          driver = "org.postgresql.ds.PGSimpleDataSource",
          poolSize = config.getInt("poolSize")
        )
      }
    }
  }

  def createTransactor[F[_] : Async : ContextShift](): Resource[F, HikariTransactor[F]] = {
    for {
      dbConfig <- readConfiguration()
      ce <- ExecutionContexts.fixedThreadPool[F](dbConfig.poolSize)
      be <- ExecutionContexts.cachedThreadPool[F]
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = dbConfig.driver,
        url = dbConfig.url,
        user = dbConfig.username,
        pass = dbConfig.password,
        ce,
        be)
    } yield xa
  }

  def createClient[F[_] : ConcurrentEffect](): Resource[F, Client[F]] =
    BlazeClientBuilder[F](scala.concurrent.ExecutionContext.global).resource

  def readEnvironmentVariable[F[_] : Sync](envVar: String): F[String] =
    for {
      res <- Sync[F].delay(System.getenv(envVar))
      result <- Option(res).fold(EnvVarIsMissing(envVar).raiseError[F, String])(value => value.pure[F])
    } yield result
}
