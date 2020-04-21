package service

import cats.instances.try_._
import cats.syntax.option._
import model.PlacesRequestModel.SearchPlacesRequest
import model.{PlaceType, SearchRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import places.api.PlacesAPI
import repositories.{SearchRequestRepository, SearchResponseRepository}
import services.PlaceHunterServiceImpl
import util.Instances

import scala.util.{Success, Try}

class PlaceHunterServiceImplSpec
  extends AnyFlatSpec
    with Matchers
    with MockFactory
    with TryValues {

  val requestRepository: SearchRequestRepository[Try] = stub[SearchRequestRepository[Try]]
  val responseRepository: SearchResponseRepository[Try] = stub[SearchResponseRepository[Try]]
  val placesApi: PlacesAPI[Try] = stub[PlacesAPI[Try]]
  val sut = new PlaceHunterServiceImpl[Try](requestRepository, responseRepository, placesApi)

  behavior of "Place Hunter Service Implementation"

  "Save Place" should "save a correct place" in {
    val chatId = Instances.genChatID()

    sut.savePlace(chatId, PlaceType.Restaurant.name.some)

    (requestRepository.savePlace _).verify(chatId, PlaceType.Restaurant)
  }

  it should "throw PlaceTypeIsIncorrect exception when save a wrong place" in {
    val chatId = Instances.genChatID()
    val text = Instances.genText()

    sut.savePlace(chatId, text.some)
      .failure
      .exception should have message s"Place Type is incorrect for $chatId."

    (requestRepository.savePlace _).verify(*, *).never()
  }

  "Save Distance" should "save a correct distance" in {
    val chatId = Instances.genChatID()
    val location = Instances.genLocation()
    val searchRequest = SearchRequest(PlaceType.Restaurant, location.some).some

    (requestRepository.saveDistance _).when(chatId, 2000).returns(Success(searchRequest))
    sut.saveDistance(chatId, "Up to 2km".some).success.value shouldBe ()
  }

  it should "throw Distance Is Incorrect exception when save a wrong place" in {
    val chatId = Instances.genChatID()

    sut.saveDistance(chatId, "Up to km".some)
      .failure
      .exception should have message s"Distance is incorrect for $chatId."

    (requestRepository.savePlace _).verify(*, *).never()
  }

  "Search for a place" should "find a correct place" in {
    val chatId = Instances.genChatID()
    Instances.googleResultObject should be('right)
    val searchResponse = Success(Instances.googleResultObject.right.get)
    val location = Instances.genLocation()
    val searchRequest = SearchRequest(PlaceType.Restaurant, location.some)
    val searchPlacesRequest = SearchPlacesRequest.of(searchRequest).right.get

    (requestRepository.saveLocation _).when(chatId, location).returns(Success(searchRequest.some))
    (placesApi.explorePlaces _).when(searchPlacesRequest).returns(searchResponse)
    (requestRepository.clearRequest _).when(chatId).returns(Success(()))

    sut.searchForPlaces(chatId, location) shouldBe searchResponse
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
}
