package service

import cats.instances.try_._
import cats.syntax.option._
import model.PlacesRequestModel.SearchPlacesRequest
import model.{PlaceType, SearchRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import places.api.PlacesAPI
import repositories.{ChosenPlacesRepository, SearchRequestRepository, SearchResponseRepository}
import services.PlaceHunterServiceImpl
import util.Instances

import scala.util.{Success, Try}

class PlaceHunterServiceImplSpec
  extends AnyFlatSpec
    with Matchers
    with MockFactory
    with OptionValues
    with TryValues {

  val requestRepository: SearchRequestRepository[Try] = stub[SearchRequestRepository[Try]]
  val responseRepository: SearchResponseRepository[Try] = stub[SearchResponseRepository[Try]]
  val chosenPlacesRepository: ChosenPlacesRepository[Try] = stub[ChosenPlacesRepository[Try]]
  val placesApi: PlacesAPI[Try] = stub[PlacesAPI[Try]]
  val sut = new PlaceHunterServiceImpl[Try](requestRepository, responseRepository, chosenPlacesRepository, placesApi)

  behavior of "Place Hunter Service Implementation"

  "Save Place" should "save a correct place" in {
    val chatId = Instances.genChatID()

    (requestRepository.savePlace _).when(chatId, PlaceType.Restaurant).returns(Success(()))

    sut.savePlace(chatId, PlaceType.Restaurant)
      .success
      .value shouldBe ()
  }

  "Save Distance" should "save a correct distance" in {
    val chatId = Instances.genChatID()
    val location = Instances.genLocation()
    val searchRequest = SearchRequest(PlaceType.Restaurant, location.some).some

    (requestRepository.saveDistance _).when(chatId, 2000).returns(Success(searchRequest))
    sut.saveDistance(chatId, 2000).success.value shouldBe ()
  }

  it should "throw Search records is missing exception when save a distance without place type" in {
    val chatId = Instances.genChatID()

    (requestRepository.saveDistance _).when(chatId, 2000).returns(Success(None))

    sut.saveDistance(chatId, 2000)
      .failure
      .exception should have message s"Search Record is missing for $chatId."
  }

  "Search for a place" should "find a correct place" in {
    val chatId = Instances.genChatID()
    Instances.googleResultObject should be('right)
    val response = Instances.googleResultObject.right.get
    val searchResponse = Success(response)
    val location = Instances.genLocation()
    val searchRequest = SearchRequest(PlaceType.Restaurant, location.some)
    val searchPlacesRequest = SearchPlacesRequest.of(searchRequest).right.get

    (requestRepository.saveLocation _).when(chatId, location).returns(Success(searchRequest.some))
    (placesApi.explorePlaces _).when(searchPlacesRequest).returns(searchResponse)
    (responseRepository.saveSearchResponse _).when(chatId, response.sortedByRating).returns(Success(()))

    val result = sut.searchForPlaces(chatId, location)
    result
      .success
      .value
      .searchResponse shouldBe response
  }

  it should "not search if save of the location fails" in {
    val chatId = Instances.genChatID()
    Instances.googleResultObject should be('right)
    val location = Instances.genLocation()

    (requestRepository.saveLocation _).when(chatId, location).returns(Success(None))

    sut.searchForPlaces(chatId, location)
      .failure
      .exception should have message s"Search Record is missing for $chatId."

    (placesApi.explorePlaces _).verify(*).never()
    (requestRepository.clearRequest _).verify(*).never()
  }

  it should "not search without location in the request" in {
    val chatId = Instances.genChatID()
    Instances.googleResultObject should be('right)
    val location = Instances.genLocation()
    val searchRequest = SearchRequest(PlaceType.Restaurant, None)
    (requestRepository.saveLocation _).when(chatId, location).returns(Success(searchRequest.some))

    sut.searchForPlaces(chatId, location)
      .failure
      .exception should have message s"Missing Location."

    (placesApi.explorePlaces _).when(*).never()
    (requestRepository.clearRequest _).verify(*).never()
  }

  "Search for the next places" should "find a correct place" in {
    val chatId = Instances.genChatID()
    Instances.googleResultObject should be('right)
    val response = Instances.googleResultObject.right.get
    val searchResponse = Success((response, 1).some)
    val location = Instances.genLocation()
    val searchRequest = SearchRequest(PlaceType.Restaurant, location.some)

    (responseRepository.loadResponse _).when(chatId, 6, 10).returns(searchResponse)
    (requestRepository.loadRequest _).when(chatId).returns(Success(searchRequest.some))

    val result = sut.searchForPlaces(chatId, 6, 10)
    result
      .success
      .value
      .value
      .searchResponse shouldBe response
  }
}
