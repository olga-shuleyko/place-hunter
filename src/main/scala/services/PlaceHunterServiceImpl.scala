package services

import cats.MonadError
import com.bot4s.telegram.models.Location
import model.{ChatId, Distance, PlaceType, SearchRequest}
import repositories.SearchRequestRepository
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import model.ClientError.{DistanceIsIncorrect, ParseError, PlaceTypeIsIncorrect}
import model.GooglePlacesResponseModel.SearchResponse
import model.PlacesRequestModel.SearchPlacesRequest
import model.RepositoryError.SearchRecordIsMissing
import places.api.PlacesAPI

class PlaceHunterServiceImpl[F[_]: MonadError[*[_], Throwable]](requestRepository: SearchRequestRepository[F],
                                                                placesApi: PlacesAPI[F])
  extends PlaceHunterService[F] {

  override def savePlace(chatId: ChatId, msgText: Option[String]): F[Unit] = {
    PlaceType.parse(msgText)
      .map(placeType => requestRepository.savePlace(chatId, placeType))
      .fold(PlaceTypeIsIncorrect(chatId).raiseError[F, Unit])(identity)
  }

  override def searchForPlaces(chatId: ChatId, location: Location): F[SearchResponse] = {
    for {
      searchRequestOpt <- requestRepository.saveLocation(chatId, location)
      searchRequest <- searchRequestOpt.fold(raiseMissingRecord[SearchRequest](chatId))(_.pure)
      placesRequest <- SearchPlacesRequest.of(searchRequest).fold(raiseParseError, _.pure)
      response <- placesApi.explorePlaces(placesRequest)
      _ <- requestRepository.clearRequest(chatId)
    } yield response.sortedByRating
  }

  override def saveDistance(chatId: ChatId, msgText: Option[String]): F[Unit] = {
    val distanceOpt = Distance.parse(msgText)
    distanceOpt.map { radius =>
      for {
        searchReqOpt <- requestRepository.saveDistance(chatId, radius)
        res <- searchReqOpt.fold(raiseMissingRecord[Unit](chatId))(_ => ().pure)
      } yield res
    }.fold(DistanceIsIncorrect(chatId).raiseError[F, Unit])(identity)
  }

  private def raiseMissingRecord[T](chatId: ChatId): F[T] =
    SearchRecordIsMissing(chatId).raiseError[F, T]

  private def raiseParseError(message: String): F[SearchPlacesRequest] =
    ParseError(message).raiseError[F, SearchPlacesRequest]
}
