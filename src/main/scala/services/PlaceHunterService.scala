package services

import com.bot4s.telegram.models.Location
import model.GooglePlacesResponseModel.{Response, Result}
import model.{ChatId, PlaceType}

trait PlaceHunterService[F[_]] {

  def savePlace(chatId: ChatId, placeType: PlaceType): F[Unit]

  def saveDistance(chatId: ChatId, msgText: Double): F[Unit]

  def searchForPlaces(chatId: ChatId, location: Location): F[Response]

  def searchForPlaces(chatId: ChatId, from: Int, until: Int): F[Option[Response]]

  def stopSearch(chatId: ChatId, likes: Option[Int]): F[Option[Result]]

  def clearStorage(chatId: ChatId): F[Unit]

  def loadChosenPlaces(chatId: ChatId): F[List[Result]]
}
