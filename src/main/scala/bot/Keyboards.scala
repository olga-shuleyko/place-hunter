package bot

import com.bot4s.telegram.models.{KeyboardButton, ReplyKeyboardMarkup, ReplyKeyboardRemove}
import cats.syntax.option._

import scala.util.matching.Regex

object Keyboards {
  val cafe = "Cafe\uD83C\uDF55\uD83C\uDF73\uD83C\uDF69"
  val coffee = "Coffee☕️"
  val placeRegex: Regex = (cafe + "|" + coffee).r
  val placeTypes = keyboard(KeyboardButton(cafe), KeyboardButton(coffee))
  val shareLocation = keyboard(KeyboardButton("Send my current location", requestLocation = true.some))
  val removeKeyBoard = ReplyKeyboardRemove().some

  def keyboard(buttons: KeyboardButton*): Option[ReplyKeyboardMarkup] =
    ReplyKeyboardMarkup(Seq(buttons.toSeq)).some
}
