package places.api

import model.GooglePlacesResponseModel.SearchResponse
import model.{ChatId, SearchRequest}

trait PlacesAPI[F[_]] {
  def explorePlaces(chatId: ChatId, searchRequest: SearchRequest): F[SearchResponse]
}
