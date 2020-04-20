package services

import cats.MonadError
import com.bot4s.telegram.models.Location
import model.{ChatId, Distance, PlaceType, SearchRequest}
import repositories.SearchRequestRepository
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicativeError._
import model.ClientError.{DistanceIsIncorrect, PlaceTypeIsIncorrect}
import model.GooglePlacesResponseModel.SearchResponse
import places.api.PlacesAPI


class PlaceHunterServiceImpl[F[_]: MonadError[*[_], Throwable]](requestRepository: SearchRequestRepository[F],
                                                                placesApi: PlacesAPI[F])
  extends PlaceHunterService[F] {

  override def savePlace(chatId: ChatId, msgText: Option[String]): F[Unit] = {
    PlaceType.parse(msgText)
      .map(placeType => requestRepository.savePlace(chatId, placeType))
      .fold(PlaceTypeIsIncorrect(chatId).raiseError[F, Unit])(identity)
  }

  override def saveLocation(chatId: ChatId, location: Location): F[SearchRequest] =
    for {
      _ <- requestRepository.saveLocation(chatId, location)
      searchRequest <- requestRepository.loadRequest(chatId)
    } yield searchRequest

  override def searchForPlaces(chatId: ChatId, searchRequest: SearchRequest): F[SearchResponse] = {
    for {
      response <- placesApi.explorePlaces(chatId, searchRequest)
      _ <- requestRepository.clearRequest(chatId)
    } yield response.sortedByRating
  }

  override def saveDistance(chatId: ChatId, msgText: Option[String]): F[Unit] = {
    Distance.parse(msgText)
      .map(radius => requestRepository.saveDistance(chatId, radius))
      .fold(DistanceIsIncorrect(chatId).raiseError[F, Unit])(identity)
  }
}
