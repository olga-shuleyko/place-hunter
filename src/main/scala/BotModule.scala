import bot.PlaceHunterBot
import cats.effect.{Async, ContextShift}
import cats.effect.concurrent.Ref
import model.{ChatId, JdbcConfig, SearchRequest}
import repositories.{DoobieChosenPlacesRepository, DoobieSchemaManager, InMemorySearchRepository, InMemorySearchResponseRepository}
import com.softwaremill.macwire.wire
import doobie.util.transactor.Transactor
import io.chrisdavenport.log4cats.Logger
import model.Credentials.BotToken
import model.Credentials.BotKeys
import model.GooglePlacesResponseModel.SearchResponse
import services.PlaceHunterServiceImpl
import org.http4s.client.Client
import places.api.{GooglePlacesApi, MockGooglePlacesAPI}

class BotModule[F[_]: Async: ContextShift](token: BotToken,
                                           requests: Ref[F, Map[ChatId, SearchRequest]],
                                           responses: Ref[F, Map[ChatId, SearchResponse]],
                                           httpClient: Client[F],
                                           credentials: BotKeys,
                                           loggerF: Logger[F],
                                           config: JdbcConfig) {

  val xa = Transactor.fromDriverManager[F](config.driver, config.url, config.user, config.password)
  implicit val logger = loggerF
  val searchRepository = wire[InMemorySearchRepository[F]]
  val responseRepository= wire[InMemorySearchResponseRepository[F]]
  val chosenPlacesRepository = wire[DoobieChosenPlacesRepository[F]]
  val schemaManager = wire[DoobieSchemaManager[F]]

  // Uncomment to request data from Google
  val placeApi: GooglePlacesApi[F] = wire[GooglePlacesApi[F]]

  //val placeApi: MockGooglePlacesAPI[F] = wire[MockGooglePlacesAPI[F]]
  val placeHunterService: PlaceHunterServiceImpl[F] = wire[PlaceHunterServiceImpl[F]]
  val bot: PlaceHunterBot[F] = wire[PlaceHunterBot[F]]

  def init(): F[Int] =
    schemaManager.createSchema()
}
