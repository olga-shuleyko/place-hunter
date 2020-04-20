package services

import cats.MonadError
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.bot4s.telegram.models.Location
import model.ClientError.{DistanceIsIncorrect, ParseError, PlaceTypeIsIncorrect}
import model.GooglePlacesResponseModel.Response
import model.PlacesRequestModel.SearchPlacesRequest
import model.RepositoryError.SearchRecordIsMissing
import model.{ChatId, Distance, PlaceType, SearchRequest}
import places.api.PlacesAPI
import repositories.SearchRequestRepository
import util.GooglePlacesAPI

class PlaceHunterServiceImpl[F[_]: MonadError[*[_], Throwable]](requestRepository: SearchRequestRepository[F],
                                                                placesApi: PlacesAPI[F])
  extends PlaceHunterService[F] {

  override def savePlace(chatId: ChatId, msgText: Option[String]): F[Unit] = {
    PlaceType.parse(msgText)
      .map(placeType => requestRepository.savePlace(chatId, placeType))
      .fold(PlaceTypeIsIncorrect(chatId).raiseError[F, Unit])(identity)
  }

  override def searchForPlaces(chatId: ChatId, location: Location): F[Response] = {
    for {
      searchRequestOpt <- requestRepository.saveLocation(chatId, location)
      searchRequest <- searchRequestOpt.fold(raiseMissingRecord[SearchRequest](chatId))(_.pure)
      placesRequest <- SearchPlacesRequest.of(searchRequest).fold(raiseParseError, _.pure)
      response <- placesApi.explorePlaces(placesRequest)
      _ <- requestRepository.clearRequest(chatId)
    } yield {
      val result = response.sortedByRating
      val buttons = result.results.zipWithIndex.map {
        case (result, idx) =>
          (idx + 1, GooglePlacesAPI.linkToRoute(searchRequest.location.get, result.geometry.location, result.placeId))
      }
      Response(result, buttons)
    }
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
