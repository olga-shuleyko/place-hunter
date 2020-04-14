package model

import com.bot4s.telegram.models.{KeyboardButton, ReplyKeyboardMarkup, ReplyKeyboardRemove}
import cats.syntax.option._
import model.PlaceType._

import scala.util.matching.Regex

object Keyboards {
  val placeRegex: Regex = List(Cafe.name, Coffee.name, FastFood.name).mkString("|").r
  val placeTypes = keyboard(KeyboardButton(Cafe.name), KeyboardButton(Coffee.name), KeyboardButton(FastFood.name))
  val shareLocation = keyboard(KeyboardButton("Send my current location", requestLocation = true.some))
  val removeKeyBoard = ReplyKeyboardRemove().some

  def keyboard(buttons: KeyboardButton*): Option[ReplyKeyboardMarkup] =
    ReplyKeyboardMarkup(Seq(buttons.toSeq)).some
}
