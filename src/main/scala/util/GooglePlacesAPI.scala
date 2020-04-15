package util

import org.http4s.Uri
import org.http4s._

object GooglePlacesAPI {

  val apiUri = uri"https://maps.googleapis.com/maps/api/place"

  val output = "json"

  val nearBySearchUri: Uri = apiUri / "nearbysearch" / output

  object QueryParams {
    val apiKey = "key"

    val location = "location"

    def location(latitude: Double, longitude: Double) = s"$latitude,$longitude"

    val radius = "radius"

    val category = "type"
  }
}
