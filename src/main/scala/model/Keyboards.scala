package model

import com.bot4s.telegram.models.{InlineKeyboardButton, InlineKeyboardMarkup, KeyboardButton, ReplyKeyboardMarkup, ReplyKeyboardRemove}
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

  val likesRegex: Regex = "👍 \\d+".r
  val dislike = "👎"
  val dislikeRegex: Regex = dislike.r
  val nextResultsRegex: Regex = "Next results \\d+-?+\\d+".r

  def inlineKeyboardButtons(buttons: List[(Int, String)]) =
    InlineKeyboardMarkup(
      List(buttons.map { case (idx, link) => InlineKeyboardButton(text = idx.toString, url = link.some) })
    ).some

  def likesKeyboard(buttons: List[Int], next: Option[(Int, Int)]) = {
    val likeButtons = buttons
      .map { idx => KeyboardButton(s"👍 ${idx.toString}")}
      .grouped(5)
      .toList
    val nextResultButton = next.map { case (from, to) =>
      val start = from + 1
      val resultRange = if (start == to) start else s"$start-$to"
      List(KeyboardButton(s"Next results $resultRange"))
    }
    keyboard(
      likeButtons ++
        nextResultButton ++
        List(List(KeyboardButton(dislike)))
    )
  }

  def keyboard(buttons: List[List[KeyboardButton]]): Option[ReplyKeyboardMarkup] =
    ReplyKeyboardMarkup(buttons).some
}
