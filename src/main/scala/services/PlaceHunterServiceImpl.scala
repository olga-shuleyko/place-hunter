package services

import cats.data.OptionT
import cats.{MonadError, Traverse}
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.bot4s.telegram.models.Location
import model.ClientError.ParseError
import model.GooglePlacesResponseModel.{Response, Result, SearchResponse}
import model.PlacesRequestModel.SearchPlacesRequest
import model.RepositoryError.SearchRecordIsMissing
import model.{ChatId, PlaceType, SearchRequest}
import places.api.PlacesAPI
import repositories.{ChosenPlacesRepository, SearchRequestRepository, SearchResponseRepository}
import util.{GooglePlacesAPI, Util}

class PlaceHunterServiceImpl[F[_]: MonadError[*[_], Throwable]](requestRepository: SearchRequestRepository[F],
                                                                responseRepository: SearchResponseRepository[F],
                                                                chosenPlacesRepository: ChosenPlacesRepository[F],
                                                                placesApi: PlacesAPI[F])
  extends PlaceHunterService[F] {

  override def savePlace(chatId: ChatId, placeType: PlaceType): F[Unit] = {
    requestRepository.savePlace(chatId, placeType)
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
      Response(sortedResponse.copy(
        results = sortedResponse.results.take(Util.numberOfReplies)),
        calculateButtons(sortedResponse, searchRequest).take(Util.numberOfReplies),
        sortedResponse.results.size
      )
    }
  }

  override def saveDistance(chatId: ChatId, radius: Double): F[Unit] = {
    for {
      searchReqOpt <- requestRepository.saveDistance(chatId, radius)
      _ <- searchReqOpt.liftTo[F](SearchRecordIsMissing(chatId))
    } yield ()
  }

  override def stopSearch(chatId: ChatId, likes: Option[Int]): F[Option[Result]] = {
    import cats.instances.option._
    import cats.syntax.traverse._
    (for {
      searchResp <- OptionT(likes.flatTraverse(idx => responseRepository.loadResult(chatId, idx)))
      resultOpt = searchResp.results.headOption
      _ <- OptionT(resultOpt.traverse(result => chosenPlacesRepository.savePlace(chatId, result)))
      _ <- OptionT.liftF(clearStorage(chatId))
    } yield resultOpt).value.map(_.flatten)
  }

  override def clearStorage(chatId: ChatId): F[Unit] =
    (responseRepository.clearResponse(chatId), requestRepository.clearRequest(chatId)).mapN((_, _) => ())

  override def searchForPlaces(chatId: ChatId, from: Int, until: Int): F[Option[Response]] =
    for {
      responses <- responseRepository.loadResponse(chatId, from, until)
      request <- requestRepository.loadRequest(chatId)
    } yield {
      import cats.instances.option._
      (responses, request).mapN { case ((response, size), request) =>
        val buttons = calculateButtons(response, request, from)
        Response(response, buttons, size)
      }
    }

  private def calculateButtons(searchResponse: SearchResponse, searchRequest: SearchRequest, from: Int = 0) = {
    searchResponse.results.zipWithIndex.map {
      case (result, idx) =>
        (idx + 1 + from, GooglePlacesAPI.linkToRoute(searchRequest.location.get, result.geometry.location, result.placeId))
    }
  }

  override def loadChosenPlaces(chatId: ChatId): F[List[Result]] =
    chosenPlacesRepository.loadPlaces(chatId)
}
