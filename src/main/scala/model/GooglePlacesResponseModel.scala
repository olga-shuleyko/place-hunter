package model

import cats.Show
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras._
import io.circe.generic.extras.semiauto._
import cats.syntax.show._

object GooglePlacesResponseModel {

  sealed trait Status

  object Status {

    final case object OK extends Status

    final case object ZERO_RESULTS extends Status

    final case object OVER_QUERY_LIMIT extends Status

    final case object REQUEST_DENIED extends Status

    final case object INVALID_REQUEST extends Status

    final case object UNKNOWN_ERROR extends Status

  }

  final case class ResultLocation(lat: Double, lng: Double)

  final case class Geometry(location: ResultLocation)

  final case class OpeningHours(openNow: Boolean)

  final case class Result(geometry: Geometry,
                          id: String,
                          name: String,
                          placeId: String,
                          openingHours: Option[OpeningHours],
                          priceLevel: Option[Int],
                          rating: Option[Double],
                          types: List[String],
                          userRatingsTotal: Option[Int],
                          vicinity: String) {

    lazy val extractRating: Option[(Double, Int)] =
      for {
        xRating <- this.rating
        xReview <- this.userRatingsTotal
      } yield (xRating, xReview)

    // Bot's rating of the place is calculated from the rating and number of reviews
    lazy val coefficient: Option[Double] =
      this.extractRating.map {
        case (xRating, xReviews) => Math.pow(xRating / 3, 2) * Math.log10(xReviews)
      }
  }

  final case class SearchResponse(status: Status, results: List[Result], nextPageToken: Option[String] = None) {
    def sortedByRating: SearchResponse = this.copy(results = this.results.sortBy(_.coefficient)(OptionDoubleOrdering))
  }

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val statusDecoder: Decoder[Status] = deriveEnumerationDecoder[Status]
  implicit val statusEncoder: Encoder[Status] = deriveEnumerationEncoder[Status]

  implicit val ResultLocationDecoder: Decoder[ResultLocation] = deriveDecoder[ResultLocation]
  implicit val ResultLocationEncoder: Encoder[ResultLocation] = deriveEncoder[ResultLocation]

  implicit val GeometryDecoder: Decoder[Geometry] = deriveDecoder[Geometry]
  implicit val GeometryEncoder: Encoder[Geometry] = deriveEncoder[Geometry]

  implicit val OpeningHoursDecoder: Decoder[OpeningHours] = deriveDecoder[OpeningHours]
  implicit val OpeningHoursEncoder: Encoder[OpeningHours] = deriveEncoder[OpeningHours]

  implicit val ResultDecoder: Decoder[Result] = deriveDecoder[Result]
  implicit val ResultEncoder: Encoder[Result] = deriveEncoder[Result]

  implicit val SearchResponseDecoder: Decoder[SearchResponse] = deriveDecoder[SearchResponse]
  implicit val SearchResponseEncoder: Encoder[SearchResponse] = deriveEncoder[SearchResponse]

  private val placeIsOpen = "\nOpen now"
  private val placeIsClosed = "\nClosed now"
  private val reviews = " reviews"
  private val without = "no "
  private val money = "\uD83D\uDCB0"
  private val star = "⭐️"

  implicit val showResult: Show[Result] = Show.show { res =>
    val rating = res.rating.getOrElse(without) + star
    val review = res.userRatingsTotal.getOrElse(0) + reviews
    val priceLevel = res.priceLevel.fold("")(value => money * value)
    val isOpened = res.openingHours.fold("")(value => if (value.openNow) placeIsOpen else placeIsClosed)
    s"""|*${res.name}* $rating($review)$priceLevel
        |_${res.vicinity}${isOpened}_
        |""".stripMargin
  }

  implicit val showSearchResponse: Show[SearchResponse] = Show.show { response =>
    "\n" +
      response
        .results
        .zipWithIndex
        .map { case (entry, idx) => s"${idx + 1}. ${entry.show}\n" }
        .mkString
  }

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
