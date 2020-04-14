package places.api

import model.ResponseModel.SearchResponse
import model.{ChatId, SearchRequest}

trait PlacesAPI[F[_]] {
  def explorePlaces(chatId: ChatId, searchRequest: SearchRequest): F[SearchResponse]
}
