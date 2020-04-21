import bot.PlaceHunterBot
import cats.effect.{Async, ContextShift}
import cats.effect.concurrent.Ref
import model.{ChatId, SearchRequest}
import repositories.{InMemorySearchRepository, InMemorySearchResponseRepository}
import com.softwaremill.macwire.wire
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
                                           credentials: BotKeys) {
  val searchRepository: InMemorySearchRepository[F] = wire[InMemorySearchRepository[F]]
  val responseRepository: InMemorySearchResponseRepository[F] = wire[InMemorySearchResponseRepository[F]]

  // Uncomment to request data from Google
  val placeApi: GooglePlacesApi[F] = wire[GooglePlacesApi[F]]

  //val placeApi: MockGooglePlacesAPI[F] = wire[MockGooglePlacesAPI[F]]
  val placeHunterService: PlaceHunterServiceImpl[F] = wire[PlaceHunterServiceImpl[F]]
  val bot: PlaceHunterBot[F] = wire[PlaceHunterBot[F]]
}
