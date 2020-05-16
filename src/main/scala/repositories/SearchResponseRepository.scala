package repositories

import model.ChatId
import model.GooglePlacesResponseModel.SearchResponse

trait SearchResponseRepository[F[_]] {
  def saveSearchResponse(chatId: ChatId, response: SearchResponse): F[Unit]

  def loadResponse(chatId: ChatId, from: Int, until: Int): F[Option[(SearchResponse, Int)]]

  def loadResult(chatId: ChatId, idx: Int): F[Option[SearchResponse]]

  def clearResponse(chatId: ChatId): F[Unit]
}
