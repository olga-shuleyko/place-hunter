package model

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
    override val name: String = "ATM\uD83C\uDFE2"

    override def category: String = "atm"
  }

  final object Bank extends PlaceType {
    override val name: String = "Bank\uD83C\uDFE6"

    override def category: String = "bank"
  }

  final object SubwayStation extends PlaceType {
    override val name: String = "Subway\uD83D\uDE8A"

    override def category: String = "subway_station"
  }

  final object TrainStation extends PlaceType {
    override val name: String = "Trains\uD83D\uDE82"

    override def category: String = "train_station"
  }

  final object BookStore extends PlaceType {
    override val name: String = "Books\uD83D\uDCDA"

    override def category: String = "book_store"
  }

  final object Museum extends PlaceType {
    override val name: String = "Museum"

    override def category: String = "museum"
  }

  final object Doctor extends PlaceType {
    override val name: String = "Doctor⚕️"

    override def category: String = "doctor"
  }

  final object Supermarket extends PlaceType {
    override val name: String = "Supermarket\uD83D\uDED2"

    override def category: String = "supermarket"
  }

  final object Zoo extends PlaceType {
    override val name: String = "Pets\uD83D\uDC08"

    override def category: String = "pet_store"
  }

  final object Pharmacy extends PlaceType {
    override val name: String = "Pharmacy\uD83D\uDC8A"

    override def category: String = "pharmacy"
  }

  final object HairCare extends PlaceType {
    override val name: String = "Hair\uD83D\uDC87\u200D️"

    override def category: String = "hair_care"
  }

  final object CarRental extends PlaceType {
    override val name: String = "Car Rental\uD83D\uDE97"

    override def category: String = "car_rental"
  }

  final object GasStation extends PlaceType {
    override val name: String = "Gas Station⛽️"

    override def category: String = "gas_station"
  }

  final object Cinema extends PlaceType {
    override val name: String = "Cinema\uD83C\uDF7F️"

    override def category: String = "movie_theater"
  }

  val places = List(Restaurant, Cafe, Bar, Gym, Atm, Bank, SubwayStation, TrainStation, Museum,
    BookStore, Supermarket, HairCare, Pharmacy, Zoo, Doctor, CarRental, GasStation, Cinema)

  private val mapPlaces = places.map(place => place.name -> place).toMap

  def parse(text: Option[String]): Option[PlaceType] = text.flatMap(mapPlaces.get)
}
