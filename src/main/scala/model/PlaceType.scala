package model

import cats.syntax.option._

sealed trait PlaceType {
  def name: String

  def category: String

  override def toString: String = name
}
object PlaceType {
  final object Restaurant extends PlaceType {
    override val name: String = "Restaurant\uD83C\uDF73"

    override def category: String = "restaurant"
  }

  final object Cafe extends PlaceType {
    override val name: String = "Cafe\uD83C\uDF70☕️️"

    override def category: String = "cafe"
  }

  final object Gym extends PlaceType {
    override val name: String = "Gym\uD83E\uDD4A️"

    override def category: String = "gym"
  }

  final object Bar extends PlaceType {
    override val name: String = "Bar\uD83C\uDF78"

    override def category: String = "bar"
  }

  final object Atm extends PlaceType {
    override val name: String = "Atm\uD83C\uDFE2"

    override def category: String = "atm"
  }

  final object SubwayStation extends PlaceType {
    override val name: String = "Subway station\uD83D\uDE8A"

    override def category: String = "subway_station"
  }

  val places = List(Restaurant, Cafe, Gym, Bar, Atm, SubwayStation)

  def parse(text: Option[String]): Option[PlaceType] = text.flatMap {
    case Restaurant.name => Restaurant.some
    case Cafe.name => Cafe.some
    case Gym.name => Gym.some
    case Bar.name => Bar.some
    case Atm.name => Atm.some
    case SubwayStation.name => SubwayStation.some
    case _ => none
  }
}
