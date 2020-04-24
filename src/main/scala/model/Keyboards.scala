package model

import com.bot4s.telegram.models.{KeyboardButton, ReplyKeyboardMarkup, ReplyKeyboardRemove}
import cats.syntax.option._
import model.PlaceType._

import scala.util.matching.Regex

object Keyboards {
  val placeRegex: Regex = places.mkString("|").r
  val placeTypes = keyboard(
    places
      .map(place => KeyboardButton(place.name))
      .grouped(3)
      .toList
  )
  val shareLocation = keyboard(List(List(KeyboardButton("Send my current location", requestLocation = true.some))))
  val removeKeyBoard = ReplyKeyboardRemove().some

  val distances = List("Up to 0.5km", "Up to 1km", "Up to 2km")
  val distancesRegex: Regex = "Up to ([0-9.]+)k?+m".r
  val distance = keyboard(
    distances.map(distance => List(KeyboardButton(distance)))
  )

  def keyboard(buttons: List[List[KeyboardButton]]): Option[ReplyKeyboardMarkup] =
    ReplyKeyboardMarkup(buttons).some
}
