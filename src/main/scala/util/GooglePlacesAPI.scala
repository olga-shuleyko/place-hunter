package util

import com.bot4s.telegram.models.Location
import model.GooglePlacesResponseModel.ResultLocation
import org.http4s.Uri
import org.http4s._
import java.net.URLEncoder

import scala.util.Try

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

  val destinationApi = "https://www.google.com/maps/dir/?api=1&travelmode=walking"

  def linkToRoute(origin: Location, destination: ResultLocation, placeID: String): String = {
    val originLoc = origin.latitude + "," + origin.longitude
    val destinationLocation = destination.lat + "," + destination.lng
    s"$destinationApi&origin=$originLoc&destination=$destinationLocation&destination_place_id=$placeID"
  }

  def linkToPlace(placeId: String, placeName: String) = {
    val encodedName = Try(URLEncoder.encode(placeName, "UTF-8"))
    val queryParam = encodedName.fold(_ => "whatever", identity)
    s"https://www.google.com/maps/search/?api=1&query_place_id=$placeId&query=$queryParam"
  }
}
