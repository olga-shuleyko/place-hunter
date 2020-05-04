package model

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.syntax.option._
import cats.syntax.show._
import model.GooglePlacesResponseModel.{OpeningHours, SearchResponse}
import model.GooglePlacesResponseModel.Status.OK
import util.Instances._

class RatingsSpec extends AnyFlatSpec with Matchers with OptionValues with EitherValues {

  "Result" should "extract rating" in {
    googleResultObject should be('right)
    val result = googleResultObject.right.get.results.headOption.value

    result.extractRating.value should be(5d, 2)
  }

  "SearchResponse" should "sort ratings correctly" in {
    googleResultObject should be('right)
    val result = googleResultObject.right.get.results.headOption.value
    val resultLess = result.copy(rating = 4d.some)
    val resultBigger = result.copy(rating = 6d.some)
    val response = SearchResponse(OK.some, List(result, resultLess, resultBigger))
    val expectedResult = List(resultBigger, result, resultLess)

    response.sortedByRating.results should contain theSameElementsInOrderAs expectedResult
  }

  "SearchResponse" should "sort ratings correctly without ratings and reviews" in {
    googleResultObject should be('right)
    val result = googleResultObject.right.get.results.headOption.value
    val resultLess = result.copy(rating = 4d.some)
    val resultBigger = result.copy(rating = 6d.some)
    val resultWoRating = result.copy(rating = 0d.some)
    val resultWoReviews = result.copy(userRatingsTotal = 0.some)
    val response = SearchResponse(OK.some, List(result, resultWoRating, resultWoReviews, resultLess, resultBigger))
    val expectedResult = List(resultBigger, result, resultLess, resultWoRating, resultWoReviews)

    response.sortedByRating.results should contain theSameElementsInOrderAs expectedResult
  }

  it should "show result correctly without working hours and price level" in {
    googleResultObject should be('right)
    val result = googleResultObject.right.get.results.headOption.value
    val expectedResult =
      """|*Ресторан* 5.0⭐️(2 reviews)
         |_Belarus_
         |""".stripMargin

    result.show shouldBe expectedResult
  }

  it should "show result correctly with working hours when open" in {
    googleResultObject should be('right)
    val result = googleResultObject.right.get.results.headOption.value.copy(openingHours = OpeningHours(true).some)
    val expectedResult =
      """|*Ресторан* 5.0⭐️(2 reviews)
         |_Belarus
         |Open now_
         |""".stripMargin

    result.show shouldBe expectedResult
  }

  it should "show result correctly with working hours when closed" in {
    googleResultObject should be('right)
    val result = googleResultObject.right.get.results.headOption.value.copy(openingHours = OpeningHours(false).some)
    val expectedResult =
      """|*Ресторан* 5.0⭐️(2 reviews)
         |_Belarus
         |Closed now_
         |""".stripMargin

    result.show shouldBe expectedResult
  }

  it should "show result correctly with price level 1" in {
    googleResultObject should be('right)
    val result = googleResultObject.right.get.results.headOption.value.copy(priceLevel = 1.some)
    val expectedResult =
      """|*Ресторан* 5.0⭐️(2 reviews)💰
         |_Belarus_
         |""".stripMargin

    result.show shouldBe expectedResult
  }

  it should "show result correctly with price level 5" in {
    googleResultObject should be('right)
    val result = googleResultObject.right.get.results.headOption.value.copy(priceLevel = 5.some)
    val expectedResult =
      """|*Ресторан* 5.0⭐️(2 reviews)💰💰💰💰💰
         |_Belarus_
         |""".stripMargin

    result.show shouldBe expectedResult
  }

  it should "show result correctly with 0 rating and reviews" in {
    googleResultObject should be('right)
    val result = googleResultObject.right.get.results.headOption.value.copy(rating = none, userRatingsTotal = 0.some)
    val expectedResult =
      """|*Ресторан* no ⭐️(0 reviews)
         |_Belarus_
         |""".stripMargin

    result.show shouldBe expectedResult
  }
}
