package places.api

import cats.effect.Sync
import model.Credentials._
import model.GooglePlacesResponseModel.SearchResponse
import model.PlacesRequestModel._
import org.http4s.client.Client
import org.http4s.Uri
import org.http4s.circe.CirceEntityDecoder._

class GooglePlacesApi[F[_]: Sync](httpClient: Client[F],
                                  credentials: BotKeys)
  extends PlacesAPI[F] {

  import util.GooglePlacesAPI._

  override def explorePlaces(searchRequest: SearchPlacesRequest): F[SearchResponse] = {
    val uri = buildNearByPlaceUri(searchRequest)
    httpClient.expect[SearchResponse](uri)
  }

  private def buildNearByPlaceUri(searchRequest: SearchPlacesRequest): Uri =
    nearBySearchUri =? Map(
      QueryParams.apiKey -> List(credentials.placesAPIKey.key),
      QueryParams.category -> List(searchRequest.place.category),
      QueryParams.location -> List(QueryParams.location(searchRequest.location.lat, searchRequest.location.lng)),
      QueryParams.radius -> List(searchRequest.radius.toString)
    )
}
