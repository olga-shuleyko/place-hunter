package services

import com.bot4s.telegram.models.Location
import model.GooglePlacesResponseModel.{Response, Result}
import model.ChatId

trait PlaceHunterService[F[_]] {

  def savePlace(chatId: ChatId, msgText: Option[String]): F[Unit]

  def saveDistance(chatId: ChatId, msgText: Option[String]): F[Unit]

  def searchForPlaces(chatId: ChatId, location: Location): F[Response]

  def searchForPlaces(chatId: ChatId, from: Int, until: Int): F[Option[Response]]

  def stopSearch(chatId: ChatId, likes: Option[Int]): F[Option[Result]]

  def clearStorage(chatId: ChatId): F[Unit]
}
