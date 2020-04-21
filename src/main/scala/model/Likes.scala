package model

import cats.syntax.option.none
import kaleidoscope._
import util.Util

object Likes {
  def parse(msgText: Option[String]): Option[Int] = msgText.flatMap { value =>
    value.trim.toLowerCase match {
      case r"👍 ${value}@([0-9]+)" => Util.makeInt(value)
      case _ => none
    }
  }
}
