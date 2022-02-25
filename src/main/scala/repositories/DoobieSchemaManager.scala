package repositories

import cats.effect.Sync
import cats.syntax.flatMap._
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor

class DoobieSchemaManager[F[_]: Sync](tx: Transactor[F]) extends SchemaManager[F] {
  override def createSchema(): F[Int] = {
    val placesFr = Fragment.const(SchemaQueries.createChosenPlacesTable)
    val chatPlacesFr = Fragment.const(SchemaQueries.createChatChosenPlacesTable)
    //(SchemaQueries.dropChosenPlacesTable.update.run >>
    (placesFr.update.run
      >> chatPlacesFr.update.run).transact(tx)
  }
}
