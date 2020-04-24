package repositories

import com.bot4s.telegram.models.Location
import model.{ChatId, PlaceType, SearchRequest}

trait SearchRequestRepository[F[_]] {
  def clearRequest(chatId: ChatId): F[Unit]

  def savePlace(chatId: ChatId, placeType: PlaceType): F[Unit]

  def saveDistance(chatId: ChatId, distance: Double): F[Option[SearchRequest]]

  def saveLocation(chatId: ChatId, location: Location): F[Option[SearchRequest]]

  def loadRequest(chatId: ChatId): F[Option[SearchRequest]]
}
