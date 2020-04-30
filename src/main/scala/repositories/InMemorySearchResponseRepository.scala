package repositories
import cats.Monad
import cats.effect.concurrent.Ref
import model.ChatId
import cats.syntax.functor._
import model.GooglePlacesResponseModel.SearchResponse

class InMemorySearchResponseRepository[F[_]: Monad](val responses: Ref[F, Map[ChatId, SearchResponse]])
  extends SearchResponseRepository[F] {
  override def saveSearchResponse(chatId: ChatId, response: SearchResponse): F[Unit] =
    responses.update(map => map + (chatId -> response))

  override def loadResponse(chatId: ChatId, from: Int, until: Int): F[Option[(SearchResponse, Int)]] =
    load(chatId) { searchResponse =>
      (searchResponse.copy(results = searchResponse.results.slice(from, until)), searchResponse.results.size)
    }

  override def loadResult(chatId: ChatId, idx: Int): F[Option[SearchResponse]] =
    load(chatId) { searchResponse =>
      searchResponse.copy(results = searchResponse.results.lift(idx - 1).toList)
    }

  override def clearResponse(chatId: ChatId): F[Unit] =
    responses.update(map => map - chatId)

  private def load[A](chatId: ChatId)(transform: SearchResponse => A) =
    responses.get.map { cachedResponse =>
      cachedResponse.get(chatId).map(transform)
    }
}
