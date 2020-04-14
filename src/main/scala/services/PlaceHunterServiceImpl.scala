package services

import cats.MonadError
import com.bot4s.telegram.models.Location
import model.{ChatId, PlaceType, SearchRequest}
import repositories.SearchRequestRepository
import cats.syntax.flatMap._
import cats.syntax.functor._
import model.ClientError.PlaceTypeIsIncorrect
import model.ResponseModel.SearchResponse
import places.api.PlacesAPI

class PlaceHunterServiceImpl[F[_]: MonadError[*[_], Throwable]](requestRepository: SearchRequestRepository[F],
                                                                placesApi: PlacesAPI[F])
  extends PlaceHunterService[F] {

  override def savePlace(chatId: ChatId, msgText: Option[String]): F[Unit] = {
    val ME = MonadError[F, Throwable]

    PlaceType.parse(msgText)
      .map(placeType => requestRepository.savePlace(chatId, placeType))
      .fold(ME.raiseError[Unit](PlaceTypeIsIncorrect(chatId)))(identity)
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
    } yield response
  }
}
