package model

import cats.instances.option._
import cats.syntax.option._
import cats.syntax.apply._
import util.Util
import kaleidoscope._

object NextResults {
  def parse(msgText: Option[String]): Option[(Int, Int)] = msgText.flatMap { value =>
    value.trim.toLowerCase match {
      case r"next results ${from}@([0-9]+)-${until}@([0-9]+)" => (Util.makeInt(from), Util.makeInt(until)).tupled
      case r"next results ${from}@([0-9]+)" => (Util.makeInt(from), Util.makeInt(from)).tupled
      case _ => none
    }
  }
}
