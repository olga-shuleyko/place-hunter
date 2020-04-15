package places.api

import cats.effect.Sync
import com.bot4s.telegram.models.Location
import model.Credentials._
import model.GooglePlacesResponseModel._
import model.{ChatId, SearchRequest}
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Uri}

class GooglePlacesApi[F[_]: Sync : EntityDecoder[*[_], SearchResponse]](httpClient: Client[F],
                                                                        credentials: BotKeys)
 extends PlacesAPI[F] {
  import util.GooglePlacesAPI._

  override def explorePlaces(chatId: ChatId, searchRequest: SearchRequest): F[SearchResponse] = {
    val uri = buildNearByPlaceUri(searchRequest.location.get, searchRequest.radius, searchRequest.place.category)

    httpClient.expect[SearchResponse](uri)
  }

  private def buildNearByPlaceUri(location: Location, radius: Double, placeCategory: String): Uri =
    nearBySearchUri =? Map(
      QueryParams.apiKey -> List(credentials.placesAPIKey.key),
      QueryParams.category -> List(placeCategory),
      QueryParams.location -> List(QueryParams.location(location.latitude, location.longitude)),
      QueryParams.radius -> List(radius.toString)
    )
}
