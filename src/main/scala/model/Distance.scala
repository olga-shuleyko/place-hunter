package model

import kaleidoscope._
import cats.syntax.option._
import scala.util.control.Exception._

object Distance {
  def parse(msgText: Option[String]): Option[Double] = msgText.flatMap { value =>
    value.trim.toLowerCase match {
      case r"up to ${value}@([0-9.]+)km" => makeInt(value).map(_ * 1000)
      case r"up to ${value}@([0-9.]+)m" => makeInt(value)
      case _ => none
    }
  }

  private def makeInt(s: String): Option[Double] = allCatch.opt(s.toDouble)
}

