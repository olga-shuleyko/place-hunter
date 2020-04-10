package model

sealed trait PlaceType {
  def name: String

  override def toString: String = name
}
object PlaceType {
  final object Cafe extends PlaceType {
    override val name: String = "Cafe\uD83C\uDF55\uD83C\uDF73\uD83C\uDF69"
  }
  final object Coffee extends PlaceType {
    override val name: String = "Coffee☕️"
  }

  def parse(text: Option[String]): Option[PlaceType] = text.map {
    case Cafe.name => Cafe
    case Coffee.name => Coffee
  }
}
