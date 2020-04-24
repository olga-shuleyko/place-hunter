package services

import cats.MonadError
import cats.Traverse
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.syntax.apply._
import com.bot4s.telegram.models.Location
import model.ClientError.{DistanceIsIncorrect, ParseError, PlaceTypeIsIncorrect}
import model.GooglePlacesResponseModel.{Response, Result}
import model.PlacesRequestModel.SearchPlacesRequest
import model.RepositoryError.SearchRecordIsMissing
import model.{ChatId, Distance, PlaceType}
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
      .getOrElse(PlaceTypeIsIncorrect(chatId).raiseError[F, Unit])
  }

  override def searchForPlaces(chatId: ChatId, location: Location): F[Response] = {
    for {
      searchRequestOpt <- requestRepository.saveLocation(chatId, location)
      searchRequest <- searchRequestOpt.liftTo[F](SearchRecordIsMissing(chatId))
      placesRequest <- SearchPlacesRequest.of(searchRequest).leftMap(m => ParseError(m)).liftTo[F]
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
    Distance.parse(msgText).map { radius =>
      for {
        searchReqOpt <- requestRepository.saveDistance(chatId, radius)
        _ <- searchReqOpt.liftTo[F](SearchRecordIsMissing(chatId))
      } yield ()
    }.getOrElse(DistanceIsIncorrect(chatId).raiseError[F, Unit])
  }

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
