package util

import scala.util.control.Exception.allCatch

object Util {

  val numberOfReplies = 5

  def makeInt(s: String): Option[Int] = allCatch.opt(s.toInt)

  def makeDouble(s: String): Option[Double] = allCatch.opt(s.toDouble)
}
