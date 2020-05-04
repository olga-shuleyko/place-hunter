package model

import cats.Show
import util.GooglePlacesAPI._
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras._
import io.circe.generic.extras.semiauto._
import cats.syntax.show._

object GooglePlacesResponseModel {

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  sealed trait Status

  object Status {

    final case object OK extends Status

    final case object ZERO_RESULTS extends Status

    final case object OVER_QUERY_LIMIT extends Status

    final case object REQUEST_DENIED extends Status

    final case object INVALID_REQUEST extends Status

    final case object UNKNOWN_ERROR extends Status

    implicit val statusDecoder: Decoder[Status] = deriveEnumerationDecoder[Status]
    implicit val statusEncoder: Encoder[Status] = deriveEnumerationEncoder[Status]
  }

  final case class ResultLocation(lat: Double, lng: Double)

  object ResultLocation {
    implicit val ResultLocationDecoder: Decoder[ResultLocation] = deriveDecoder[ResultLocation]
    implicit val ResultLocationEncoder: Encoder[ResultLocation] = deriveEncoder[ResultLocation]
  }

  final case class Geometry(location: ResultLocation)

  object Geometry {
    implicit val GeometryDecoder: Decoder[Geometry] = deriveDecoder[Geometry]
    implicit val GeometryEncoder: Encoder[Geometry] = deriveEncoder[Geometry]
  }

  final case class OpeningHours(openNow: Boolean)

  object OpeningHours {
    implicit val OpeningHoursDecoder: Decoder[OpeningHours] = deriveDecoder[OpeningHours]
    implicit val OpeningHoursEncoder: Encoder[OpeningHours] = deriveEncoder[OpeningHours]

  }

  final case class Result(geometry: Geometry,
                          id: Option[String],
                          name: String,
                          placeId: String,
                          openingHours: Option[OpeningHours],
                          priceLevel: Option[Int],
                          rating: Option[Double],
                          userRatingsTotal: Option[Int],
                          vicinity: Option[String]) {

    val extractRating: Option[(Double, Int)] =
      for {
        xRating <- this.rating
        xReview <- this.userRatingsTotal
      } yield (xRating, xReview)

    // Bot's rating of the place is calculated from the rating and number of reviews
    val coefficient: Option[Double] =
      this.extractRating.map {
        case (xRating, xReviews) => Math.pow(xRating / 3, 2) * Math.log10(xReviews)
      }
  }

  object Result {
    implicit val ResultDecoder: Decoder[Result] = deriveDecoder[Result]
    implicit val ResultEncoder: Encoder[Result] = deriveEncoder[Result]

    implicit val showResult: Show[Result] = Show.show { res =>
      val rating = res.rating.getOrElse(without) + star
      val review = res.userRatingsTotal.getOrElse(0) + reviews
      val priceLevel = res.priceLevel.fold("")(value => money * value)
      val isOpened = res.openingHours.fold("")(value => if (value.openNow) placeIsOpen else placeIsClosed)
      val link = linkToPlace(res.placeId, res.name)
    s"""|[${res.name}]($link) $rating($review) $priceLevel
        |_${res.vicinity}${isOpened}_
        |""".stripMargin}
  }

  final case class SearchResponse(status: Status, results: List[Result], nextPageToken: Option[String] = None) {
    def sortedByRating: SearchResponse = this.copy(results = this.results.sortBy(_.coefficient)(OptionDoubleOrdering))
  }

  final case class FromIndex(value: Int) extends AnyVal

  object SearchResponse {
    implicit val SearchResponseDecoder: Decoder[SearchResponse] = deriveDecoder[SearchResponse]
    implicit val SearchResponseEncoder: Encoder[SearchResponse] = deriveEncoder[SearchResponse]

    implicit def showSearchResponse(implicit from: FromIndex): Show[SearchResponse] = Show.show { response =>
      "\n" +
        response
          .results
          .zipWithIndex
          .map { case (entry, idx) => s"${idx + 1 + from.value}. ${entry.show}\n" }
          .mkString
    }
  }

  final case class Response(searchResponse: SearchResponse, buttons: List[(Int, String)], size: Int)

  private val placeIsOpen = "\nOpen now"
  private val placeIsClosed = "\nClosed now"
  private val reviews = " reviews"
  private val without = "no "
  private val money = "\uD83D\uDCB0"
  private val star = "⭐️"

  object OptionDoubleOrdering extends Ordering[Option[Double]] {
    def optionOrdering = Ordering.Double.reverse

    def compare(x: Option[Double], y: Option[Double]) = (x, y) match {
      case (None, None) => 0
      case (None, _) => 1
      case (_, None) => -1
      case (Some(x), Some(y)) => optionOrdering.compare(x, y)
    }
  }

}
