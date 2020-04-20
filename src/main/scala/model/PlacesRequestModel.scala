package model

object PlacesRequestModel {

  sealed abstract case class RequestLocation private (lat: Double, lng: Double)

  object RequestLocation {

    def of(lat: Double, lng: Double): Either[String, RequestLocation] =
      for {
        latValid <- isInRange(lat)
        lngValid <- isInRange(lng)
      } yield new RequestLocation(latValid, lngValid) {}

    private def isInRange(v: Double): Either[String, Double] =
      Either.cond(-180.0 <= v && v <= 180.0, v, s"The value $v is not in range from -180 to 180.")
  }

  sealed abstract case class SearchPlacesRequest private (place: PlaceType, location: RequestLocation, radius: Double)

  object SearchPlacesRequest {
    def of(searchRequest: SearchRequest): Either[String, SearchPlacesRequest] = {
      val location = Either.cond(searchRequest.location.isDefined, searchRequest.location.get, "Missing Location.")
      location.flatMap { location =>
        for {
          locationValid <- RequestLocation.of(location.latitude, location.longitude)
          radiusValid <- positive(searchRequest.radius)
        } yield new SearchPlacesRequest(searchRequest.place, locationValid, radiusValid) {}
      }
    }

    private def positive(v: Double): Either[String, Double] =
      Either.cond(v >= 0 && v <= 50000, v, s"The radius $v must be positive and less than 50km.")
  }
}
