package services

import cats.MonadError
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.bot4s.telegram.models.Location
import model.ClientError.{DistanceIsIncorrect, ParseError, PlaceTypeIsIncorrect}
import model.GooglePlacesResponseModel.SearchResponse
import model.PlacesRequestModel.SearchPlacesRequest
import model.RepositoryError.SearchRecordIsMissing
import model.{ChatId, Distance, PlaceType}
import places.api.PlacesAPI
import repositories.SearchRequestRepository

class PlaceHunterServiceImpl[F[_]: MonadError[*[_], Throwable]](requestRepository: SearchRequestRepository[F],
                                                                placesApi: PlacesAPI[F])
  extends PlaceHunterService[F] {

  override def savePlace(chatId: ChatId, msgText: Option[String]): F[Unit] = {
    PlaceType.parse(msgText)
      .map(placeType => requestRepository.savePlace(chatId, placeType))
      .getOrElse(PlaceTypeIsIncorrect(chatId).raiseError[F, Unit])
  }

  override def searchForPlaces(chatId: ChatId, location: Location): F[SearchResponse] = {
    for {
      searchRequestOpt <- requestRepository.saveLocation(chatId, location)
      searchRequest <- searchRequestOpt.liftTo[F](SearchRecordIsMissing(chatId))
      placesRequest <- SearchPlacesRequest.of(searchRequest).leftMap(m => ParseError(m)).liftTo[F]
      response <- placesApi.explorePlaces(placesRequest)
      _ <- requestRepository.clearRequest(chatId)
    } yield response.sortedByRating
  }

  override def saveDistance(chatId: ChatId, msgText: Option[String]): F[Unit] = {
    Distance.parse(msgText).map { radius =>
      for {
        searchReqOpt <- requestRepository.saveDistance(chatId, radius)
        _ <- searchReqOpt.liftTo[F](SearchRecordIsMissing(chatId))
      } yield ()
    }.getOrElse(DistanceIsIncorrect(chatId).raiseError[F, Unit])
  }
}
