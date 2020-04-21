package services

import cats.{MonadError, Traverse}
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.apply._
import com.bot4s.telegram.models.Location
import model.ClientError.{DistanceIsIncorrect, ParseError, PlaceTypeIsIncorrect}
import model.GooglePlacesResponseModel.{Response, Result, SearchResponse}
import model.PlacesRequestModel.SearchPlacesRequest
import model.RepositoryError.SearchRecordIsMissing
import model.{ChatId, Distance, PlaceType, SearchRequest}
import places.api.PlacesAPI
import repositories.{SearchRequestRepository, SearchResponseRepository}
import util.GooglePlacesAPI

class PlaceHunterServiceImpl[F[_]: MonadError[*[_], Throwable]](requestRepository: SearchRequestRepository[F],
                                                                responseRepository: SearchResponseRepository[F],
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
      sortedResponse = response.sortedByRating
      _ <- responseRepository.saveSearchResponse(chatId, sortedResponse)
    } yield {
      val buttons = sortedResponse.results.zipWithIndex.map {
        case (result, idx) =>
          (idx + 1, GooglePlacesAPI.linkToRoute(searchRequest.location.get, result.geometry.location, result.placeId))
      }
      Response(sortedResponse.copy(results = sortedResponse.results.take(5)), buttons.take(5), sortedResponse.results.size)
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

  override def stopSearch(chatId: ChatId, likes: Option[Int]): F[Option[Result]] = {
    import cats.instances.option._
    for {
      result <- Traverse[Option].flatTraverse(likes)(idx => responseRepository.loadResponse(chatId, idx))
      _ <- responseRepository.clearResponse(chatId)
    } yield result.flatMap(_.results.headOption)
  }

  override def clearStorage(chatId: ChatId): F[Unit] =
    responseRepository.clearResponse(chatId) >> requestRepository.clearRequest(chatId)

  override def searchForPlaces(chatId: ChatId, from: Int, until: Int): F[Option[Response]] =
    for {
      responses <- responseRepository.loadResponse(chatId, from, until)
      request <- requestRepository.loadRequest(chatId)
    } yield {
      import cats.instances.option._
      (responses, request).tupled.map { case ((response, size), request) =>
        val buttons = response.results.zipWithIndex.map {
          case (result, idx) =>
            (idx + 1 + from, GooglePlacesAPI.linkToRoute(request.location.get, result.geometry.location, result.placeId))
        }
        Response(response, buttons, size)
      }
    }
}
