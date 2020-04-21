package model

import kaleidoscope._
import cats.syntax.option._
import util.Util

object Distance {
  def parse(msgText: Option[String]): Option[Double] =  {
    val radius = msgText.flatMap { value =>
      value.trim.toLowerCase match {
        case r"up to ${value}@([0-9.]+)km" => Util.makeDouble(value).map(_ * 1000)
        case r"up to ${value}@([0-9.]+)m" => Util.makeDouble(value)
        case _ => none
      }
    }
    radius.flatMap(value => if (value > 50000 || value < 0) none else value.some)
  }
}

