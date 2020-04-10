package repositories

import cats.MonadError
import cats.effect.concurrent.Ref
import cats.syntax.option._
import cats.syntax.functor._
import cats.syntax.flatMap._
import com.bot4s.telegram.models.Location
import model.RepositoryError.SearchRecordIsMissing
import model.{ChatId, PlaceType, SearchRequest}

class InMemorySearchRepository[F[_] : MonadError[*[_], Throwable]](val requests: Ref[F, Map[ChatId, SearchRequest]])
  extends SearchRequestRepository[F]{

  override def clearRequest(chatId: ChatId): F[Unit] = requests.update(map => map - chatId)

  override def savePlace(chatId: ChatId, placeType: PlaceType): F[Unit] =
    requests.update(map => map + (chatId -> SearchRequest(placeType)))

  override def saveLocation(chatId: ChatId, location: Location): F[Unit] = {
    val ME = MonadError[F, Throwable]
    for {
      requestsMap <- requests.get
      searchRequestOpt = requestsMap.get(chatId)
      searchRequest <- searchRequestOpt.fold(raiseMissingRecord(chatId))(record => ME.pure(record.copy(location = location.some)))
      _ <- requests.update(_ + (chatId -> searchRequest))
    } yield ()
  }

  override def loadRequest(chatId: ChatId): F[SearchRequest] = {
    val ME = MonadError[F, Throwable]
    for {
      requestsMap <- requests.get
      searchRequestOpt = requestsMap.get(chatId)
      searchRequest <- searchRequestOpt.fold(raiseMissingRecord(chatId))(ME.pure)
    } yield searchRequest
  }

  private def raiseMissingRecord(chatId: ChatId)(implicit ME: MonadError[F, Throwable]): F[SearchRequest] =
    ME.raiseError[SearchRequest](SearchRecordIsMissing(chatId))
}
