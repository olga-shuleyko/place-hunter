package services

import com.bot4s.telegram.models.Location
import model.GooglePlacesResponseModel.SearchResponse
import model.ChatId

trait PlaceHunterService[F[_]] {

  def savePlace(chatId: ChatId, msgText: Option[String]): F[Unit]

  def saveDistance(chatId: ChatId, msgText: Option[String]): F[Unit]

  def searchForPlaces(chatId: ChatId, location: Location): F[SearchResponse]
}
