import bot.PlaceHunterBot
import cats.effect.{Async, ContextShift}
import cats.effect.concurrent.Ref
import model.{ChatId, SearchRequest}
import repositories.InMemorySearchRepository
import com.softwaremill.macwire.wire
import io.circe.Decoder
import model.Credentials.BotToken
import model.Credentials.BotKeys
import org.http4s.EntityDecoder
import services.PlaceHunterServiceImpl
import org.http4s.circe._
import org.http4s.client.Client
import places.api.{GooglePlacesApi, MockGooglePlacesAPI}

class BotModule[F[_]: Async: ContextShift](token: BotToken,
                                           requests: Ref[F, Map[ChatId, SearchRequest]],
                                           httpClient: Client[F],
                                           credentials: BotKeys) {

  import model.GooglePlacesResponseModel._
  val searchRepository: InMemorySearchRepository[F] = wire[InMemorySearchRepository[F]]

  /*

  // Uncomment to request data from Google
  implicit val searchResponseDecoder: EntityDecoder[F, SearchResponse] = jsonOf[F, SearchResponse]
  val placeApi: GooglePlacesApi[F] = wire[GooglePlacesApi[F]]

   */
  val placeApi: MockGooglePlacesAPI[F] = wire[MockGooglePlacesAPI[F]]
  val placeHunterService: PlaceHunterServiceImpl[F] = wire[PlaceHunterServiceImpl[F]]
  val bot: PlaceHunterBot[F] = wire[PlaceHunterBot[F]]
}
