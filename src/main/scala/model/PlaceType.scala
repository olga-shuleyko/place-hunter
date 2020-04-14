package model

import cats.syntax.option._

sealed trait PlaceType {
  def name: String

  def category: String

  override def toString: String = name
}
object PlaceType {

  final object Cafe extends PlaceType {
    override val name: String = "Restaurant\uD83C\uDF73\uD83C\uDF69"

    override def category: String = "restaurant"
  }

  final object Coffee extends PlaceType {
    override val name: String = "Coffee☕️"

    override def category: String = "coffee-tea"
  }

  final object FastFood extends PlaceType {
    override val name: String = "Fast Food\uD83C\uDF54\uD83C\uDF5F\uD83C\uDF55️"

    override def category: String = "snacks-fast-food"
  }

  def parse(text: Option[String]): Option[PlaceType] = text.flatMap {
    case Cafe.name => Cafe.some
    case Coffee.name => Coffee.some
    case FastFood.name => FastFood.some
    case _ => none
  }
}
