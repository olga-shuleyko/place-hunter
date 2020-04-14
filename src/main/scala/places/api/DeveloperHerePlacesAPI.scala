package places.api

import cats.effect.Sync
import com.bot4s.telegram.models.Location
import model.Credentials._
import model.ResponseModel.SearchResponse
import model.{ChatId, SearchRequest}
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Uri}
import util.DeveloperHerePlacesAPI._

class DeveloperHerePlacesAPI[F[_]: Sync : EntityDecoder[*[_], SearchResponse]](httpClient: Client[F],
                                                                               credentials: BotKeys)
  extends PlacesAPI[F] {
  override def explorePlaces(chatId: ChatId, searchRequest: SearchRequest): F[SearchResponse] = {
    val uri = buildExplorePlaceUri(searchRequest.location.get, searchRequest.radius, searchRequest.place.category)

    httpClient.expect[SearchResponse](uri)
  }

  private def buildExplorePlaceUri(location: Location, radius: Double, placeCategory: String): Uri =
    exploreUri =? Map(
      QueryParams.appId -> List(credentials.placesAPIApp.app),
      QueryParams.apiKey -> List(credentials.placesAPIKey.key),
      QueryParams.category -> List(placeCategory),
      QueryParams.locationInCircle -> List(QueryParams.location(location.latitude, location.longitude, radius))
    )
}

