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
    override val name: String = "Grocery\uD83C\uDF4E"

    override def category: String = "grocery_or_supermarket"
  }

  final object Zoo extends PlaceType {
    override val name: String = "Zoo\uD83D\uDC08"

    override def category: String = "zoo"
  }

  final object Pharmacy extends PlaceType {
    override val name: String = "Pharmacy\uD83D\uDC8A"

    override def category: String = "pharmacy"
  }

  final object TouristAttraction extends PlaceType {
    override val name: String = "Tourism\uD83C\uDF8E"

    override def category: String = "tourist_attraction"
  }

  final object CarRental extends PlaceType {
    override val name: String = "Car Rental\uD83D\uDE97"

    override def category: String = "car_rental"
  }

  val places = List(Restaurant, Cafe, Bar, Gym, Atm, Bank, SubwayStation, TrainStation, Museum,
    BookStore, Supermarket, Doctor, Pharmacy, Zoo, TouristAttraction, CarRental)

  private val mapPlaces = places.map(place => place.name -> place).toMap

  def parse(text: Option[String]): Option[PlaceType] = text.flatMap(mapPlaces.get)
}
