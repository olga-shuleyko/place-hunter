import bot.PlaceHunterBot
import cats.effect.{Async, ContextShift}
import cats.effect.concurrent.Ref
import model.{ChatId, SearchRequest}
import repositories.InMemorySearchRepository
import com.softwaremill.macwire.wire
import model.Credentials.BotToken
import model.Credentials.BotKeys
import org.http4s.EntityDecoder
import services.PlaceHunterServiceImpl
import org.http4s.circe._
import org.http4s.client.Client
import places.api.DeveloperHerePlacesAPI

class BotModule[F[_]: Async: ContextShift](token: BotToken,
                                           requests: Ref[F, Map[ChatId, SearchRequest]],
                                           httpClient: Client[F],
                                           credentials: BotKeys) {

  import model.ResponseModel._
  implicit val searchResponseDecoder: EntityDecoder[F, SearchResponse] = jsonOf[F, SearchResponse]

  val searchRepository: InMemorySearchRepository[F] = wire[InMemorySearchRepository[F]]
  val placeApi: DeveloperHerePlacesAPI[F] = wire[DeveloperHerePlacesAPI[F]]
  val placeHunterService: PlaceHunterServiceImpl[F] = wire[PlaceHunterServiceImpl[F]]
  val bot: PlaceHunterBot[F] = wire[PlaceHunterBot[F]]
}
