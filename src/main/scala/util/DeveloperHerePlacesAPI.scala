package util

import org.http4s.Uri
import org.http4s._

object DeveloperHerePlacesAPI {

  val apiUri = uri"https://places.sit.ls.hereapi.com/places/v1"

  val exploreUri: Uri = apiUri / "discover" / "explore"

  object QueryParams {
    val appId = "app_id"
    val apiKey = "apiKey"

    val locationInCircle = "in"
    def location(latitude: Double, longitude: Double, radius: Double) = s"$latitude,$longitude;r=$radius"

    val category = "cat"
  }
}
