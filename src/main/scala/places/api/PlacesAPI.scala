package places.api

import model.GooglePlacesResponseModel.SearchResponse
import model.PlacesRequestModel.SearchPlacesRequest

trait PlacesAPI[F[_]] {
  def explorePlaces(searchRequest: SearchPlacesRequest): F[SearchResponse]
}
