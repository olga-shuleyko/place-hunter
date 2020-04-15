package service

import cats.instances.try_._
import cats.syntax.option._
import model.RepositoryError.SearchRecordIsMissing
import model.{PlaceType, SearchRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import places.api.PlacesAPI
import repositories.SearchRequestRepository
import services.PlaceHunterServiceImpl
import util.Instances

import scala.util.{Failure, Success, Try}

class PlaceHunterServiceImplSpec
  extends AnyFlatSpec
    with Matchers
    with MockFactory
    with TryValues {

  val requestRepository: SearchRequestRepository[Try] = stub[SearchRequestRepository[Try]]
  val placesApi: PlacesAPI[Try] = stub[PlacesAPI[Try]]
  val sut = new PlaceHunterServiceImpl[Try](requestRepository, placesApi)

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

  "Save Location" should "save a location" in {
    val chatId = Instances.genChatID()
    val location = Instances.genLocation()
    val searchRequest = SearchRequest(PlaceType.Restaurant, location.some)

    (requestRepository.saveLocation _).when(chatId, location).returns(Success(()))
    (requestRepository.loadRequest _).when(chatId).returns(Success(searchRequest))

    sut.saveLocation(chatId, location).success.value shouldEqual searchRequest
  }

  it should "not return location if save of the location fails" in {
    val chatId = Instances.genChatID()
    val location = Instances.genLocation()
    (requestRepository.saveLocation _).when(chatId, location).returns(Failure(SearchRecordIsMissing(chatId)))

    sut.saveLocation(chatId, location)
      .failure
      .exception should have message s"Search Record is missing for $chatId."

    (requestRepository.loadRequest _).when(*).never()
  }

  it should "not return location if load request fails" in {
    val chatId = Instances.genChatID()
    val location = Instances.genLocation()
    (requestRepository.saveLocation _).when(chatId, location).returns(Success(()))
    (requestRepository.loadRequest _).when(chatId).returns(Failure(SearchRecordIsMissing(chatId)))

    sut.saveLocation(chatId, location)
      .failure
      .exception should have message s"Search Record is missing for $chatId."
  }
}
