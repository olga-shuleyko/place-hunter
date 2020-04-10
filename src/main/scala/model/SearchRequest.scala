package model

import com.bot4s.telegram.models.Location

final case class SearchRequest(place: PlaceType, location: Option[Location] = None, radius: Long = 1000)
