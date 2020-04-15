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

  def keyboard(buttons: List[List[KeyboardButton]]): Option[ReplyKeyboardMarkup] =
    ReplyKeyboardMarkup(buttons).some
}
