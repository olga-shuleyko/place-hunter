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
                          vicinity: String)

  final case class SearchResponse(status: Status, results: List[Result], nextPageToken: Option[String])

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

  implicit val showResult: Show[Result] = Show.show { res =>
    val rating = res.rating.fold("without rating")(value => "with rating " + value + "⭐️" * Math.round(value).toInt)
    val reviews = res.userRatingsTotal.fold("without reviews")(value => s"with $value reviews")
    val priceLevel = res.priceLevel.fold("")(value => "The Price level is " + "\uD83D\uDCB5" * value + ".")
    val isOpened = res.openingHours.fold("") { value =>
      val status = if (value.openNow) "open." else "closed."
      "The Place is " + status
    }
    s"""
       |*${res.name}* $rating and $reviews! $priceLevel
       |The address is ${res.vicinity}
       |$isOpened
       |""".stripMargin
  }

  implicit val showSearchResponse: Show[SearchResponse] = Show.show { response =>
    "\n" +
      response.results
        .take(5)
        .map(_.show)
        .mkString("\uD83D\uDD3B" * 10 + "\n")
  }
}
