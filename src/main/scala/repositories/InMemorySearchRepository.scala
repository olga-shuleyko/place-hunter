package repositories

import cats.MonadError
import cats.effect.concurrent.Ref
import cats.syntax.option._
import cats.syntax.functor._
import com.bot4s.telegram.models.Location
import model.{ChatId, PlaceType, SearchRequest}

class InMemorySearchRepository[F[_] : MonadError[*[_], Throwable]](val requests: Ref[F, Map[ChatId, SearchRequest]])
  extends SearchRequestRepository[F] {

  override def clearRequest(chatId: ChatId): F[Unit] = requests.update(map => map - chatId)

  override def savePlace(chatId: ChatId, placeType: PlaceType): F[Unit] =
    requests.update(map => map + (chatId -> SearchRequest(placeType)))

  override def saveLocation(chatId: ChatId, location: Location): F[Option[SearchRequest]] =
    updateSearchRequest(chatId) { searchRequest: SearchRequest =>
      searchRequest.copy(location = location.some)
    }

  override def loadRequest(chatId: ChatId): F[Option[SearchRequest]] =
    requests.get.map(_.get(chatId))

  override def saveDistance(chatId: ChatId, distance: Double): F[Option[SearchRequest]] =
    updateSearchRequest(chatId) { searchRequest: SearchRequest =>
      searchRequest.copy(radius = distance)
    }

  def updateSearchRequest(chatId: ChatId)(transform: SearchRequest => SearchRequest): F[Option[SearchRequest]] =
    requests.modify { allRequests: Map[ChatId, SearchRequest] =>
      val updatedRequestOpt = allRequests.get(chatId).map(transform)
      val newRequests = allRequests ++ updatedRequestOpt.map(chatId -> _)
      (newRequests, updatedRequestOpt)
    }
}
