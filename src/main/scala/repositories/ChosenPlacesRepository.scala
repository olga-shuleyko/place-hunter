package repositories

import model.ChatId
import model.GooglePlacesResponseModel.Result

trait ChosenPlacesRepository[F[_]] {
  def savePlace(chatId: ChatId, place: Result): F[Int]

  def loadPlaces(chatId: ChatId): F[List[Result]]
}
