import bot.PlaceHunterBot
import cats.effect.{Async, ContextShift}
import cats.effect.concurrent.Ref
import model.{ChatId, SearchRequest}
import repositories.InMemorySearchRepository
import com.softwaremill.macwire.wire
import io.chrisdavenport.log4cats.Logger
import model.Credentials.BotToken
import model.Credentials.BotKeys
import services.PlaceHunterServiceImpl
import org.http4s.client.Client
import places.api.{GooglePlacesApi, MockGooglePlacesAPI}

class BotModule[F[_]: Async: ContextShift](token: BotToken,
                                           requests: Ref[F, Map[ChatId, SearchRequest]],
                                           httpClient: Client[F],
                                           credentials: BotKeys,
                                           loggerF: Logger[F]) {
  val searchRepository: InMemorySearchRepository[F] = wire[InMemorySearchRepository[F]]

  // Uncomment to request data from Google
  //val placeApi: GooglePlacesApi[F] = wire[GooglePlacesApi[F]]

  implicit val logger = loggerF
  val placeApi: MockGooglePlacesAPI[F] = wire[MockGooglePlacesAPI[F]]
  val placeHunterService: PlaceHunterServiceImpl[F] = wire[PlaceHunterServiceImpl[F]]
  val bot: PlaceHunterBot[F] = wire[PlaceHunterBot[F]]
}
