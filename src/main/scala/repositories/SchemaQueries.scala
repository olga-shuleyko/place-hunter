package repositories

import doobie.implicits._

object SchemaQueries {

  val createChosenPlacesTable =
    """CREATE TABLE IF NOT EXISTS chosen_place (
       |  place_id VARCHAR(100) NOT NULL,
       |  place_name TEXT NOT NULL,
       |  lat decimal NOT NULL,
       |  lng decimal NOT NULL,
       |  PRIMARY KEY(place_id));""".stripMargin

  val createChatChosenPlacesTable =
    """CREATE TABLE IF NOT EXISTS chat_chosen_place (
       |  id SERIAL NOT NULL,
       |  chat_id BIGINT NOT NULL,
       |  place_id VARCHAR(100) NOT NULL,
       |  created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       |  PRIMARY KEY (id),
       |  FOREIGN KEY (place_id) REFERENCES chosen_place(place_id));""".stripMargin

  val dropChosenPlacesTable = sql"DROP TABLE IF EXISTS chat_chosen_place, chosen_place;"
}
