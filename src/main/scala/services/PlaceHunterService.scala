package services

import com.bot4s.telegram.models.Location
import model.{ChatId, SearchRequest}

trait PlaceHunterService[F[_]] {

  def savePlace(chatId: ChatId, msgText: Option[String]): F[Unit]

  def saveLocation(chatId: ChatId, location: Location): F[SearchRequest]
}
