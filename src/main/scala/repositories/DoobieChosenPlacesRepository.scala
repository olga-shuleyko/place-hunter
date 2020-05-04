package repositories

import cats.effect.Sync
import doobie.util.transactor.Transactor
import model.GooglePlacesResponseModel.{Geometry, Result, ResultLocation}
import model.ChatId
import doobie._
import doobie.implicits._
import doobie.util.fragment.Fragment

class DoobieChosenPlacesRepository[F[_]: Sync](tx: Transactor[F]) extends ChosenPlacesRepository[F] {
  override def savePlace(chatId: ChatId, place: Result): F[Int] =
    (for {
      _ <- insertPlaceSql(place).update.run
      v <- insertChatPlaceSql(chatId.id, place.placeId).update.run
    } yield v).transact(tx)

  override def loadPlaces(chatId: ChatId): F[List[Result]] =
    queryPlaceByChatIdSql(chatId.id).query[Result].to[List].transact(tx)

  private def insertPlaceSql(place: Result) =
    sql"""
         |INSERT INTO chosen_place (place_id, place_name, lat, lng)
         |VALUES (${place.placeId}, ${place.name}, ${place.geometry.location.lat}, ${place.geometry.location.lng})
         |ON DUPLICATE KEY UPDATE
         |place_name=${place.name}, lat=${place.geometry.location.lat}, lng=${place.geometry.location.lng};
         |""".stripMargin

  private def insertChatPlaceSql(chatId: Long, placeId: String) =
    sql"INSERT INTO chat_chosen_place (chat_id, place_id) VALUES($chatId, $placeId);"

  private val queryPlaces = Fragment.const(
    """SELECT CP.chat_id,CP.place_id,P.place_name,P.lat,P.lng
      |FROM chat_chosen_place CP
      |JOIN chosen_place P on CP.place_id = P.place_id
      |""".stripMargin)

  private def queryPlaceByChatIdSql(chatId: Long) =
    queryPlaces ++
      fr"""WHERE CP.chat_id = $chatId
          |GROUP BY CP.chat_id,CP.place_id,P.place_name,P.lat,P.lng
          |ORDER BY MAX(CP.created_timestamp) DESC;""".stripMargin

  type ChosenPlace = (Long, String, String, Double, Double)

  implicit val readResult: Read[Result] =
    Read[ChosenPlace].map { case (_, placeId, placeName, lat, lng) =>
      Result(
        Geometry(ResultLocation(lat, lng)),
        id = None,
        name = placeName,
        placeId = placeId,
        openingHours = None,
        priceLevel = None,
        rating = None,
        userRatingsTotal = None,
        vicinity = None
      )
    }
}
