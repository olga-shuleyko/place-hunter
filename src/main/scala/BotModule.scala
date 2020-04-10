import bot.PlaceHunterBot
import cats.effect.{Async, ContextShift, IO}
import cats.effect.concurrent.Ref
import model.{ChatId, SearchRequest, Token}
import repositories.InMemorySearchRepository
import com.softwaremill.macwire.wire
import services.PlaceHunterServiceImpl

class BotModule[F[_]: Async: ContextShift](token: Token, requests: Ref[F, Map[ChatId, SearchRequest]]) {

  val searchRepository: InMemorySearchRepository[F] = wire[InMemorySearchRepository[F]]
  val placeHunterService: PlaceHunterServiceImpl[F] = wire[PlaceHunterServiceImpl[F]]
  val bot: PlaceHunterBot[F] = wire[PlaceHunterBot[F]]
}
