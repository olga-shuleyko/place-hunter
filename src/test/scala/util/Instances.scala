package util

import com.bot4s.telegram.models.Location
import model.ChatId
import model.GooglePlacesResponseModel.SearchResponse
import io.circe.parser.decode
import scala.util.Random

object Instances {

  val random = new Random()
  def genChatID(): ChatId = {
    val chatID = random.nextInt()
    ChatId(chatID)
  }

  def genText(): String = {
    val size = Math.abs(random.nextInt(100))
    Random.alphanumeric.take(size).mkString
  }

  def genLocation(): Location = {
    Location(random.nextInt(180), random.nextInt(180))
  }

  val googleResult = """
    |{
    |  "html_attributions": [],
    |  "results": [
    |    {
    |      "geometry": {
    |        "location": {
    |          "lat": 53.9334384,
    |          "lng": 27.4778357
    |        },
    |        "viewport": {
    |          "northeast": {
    |            "lat": 53.93479148029149,
    |            "lng": 27.4791408302915
    |          },
    |          "southwest": {
    |            "lat": 53.93209351970849,
    |            "lng": 27.4764428697085
    |          }
    |        }
    |      },
    |      "icon": "https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png",
    |      "id": "d8369eb2b1989ba2709aa8ad78215e4569411d07",
    |      "name": "Ресторан",
    |      "photos": [
    |        {
    |          "height": 4032,
    |          "html_attributions": [
    |            "\u003ca href=\"https://maps.google.com/maps/contrib/114238967734616187746\"\u003eBorys Pratsiuk\u003c/a\u003e"
    |          ],
    |          "photo_reference": "CmRaAAAAXfXztE1HyUHKsaVsylFuwdtMZE1mEU994uDeypzl5p33H133JDn59noAltMS9YPJeO110M8K9-5cYi4NQEXn_FCXHbMgPsp6a4-rDVHBXSTviVvgSknnva4CO7NOspNBEhCgQt3mw5uR90xmXHZPb76FGhRuTKVIBfMTzzu-UvRrre1UMJMr-w",
    |          "width": 3024
    |        }
    |      ],
    |      "place_id": "ChIJWT4SbBzF20YR6kwRTxSNgsY",
    |      "plus_code": {
    |        "compound_code": "WFMH+94 Minsk, Belarus",
    |        "global_code": "9G59WFMH+94"
    |      },
    |      "rating": 5,
    |      "reference": "ChIJWT4SbBzF20YR6kwRTxSNgsY",
    |      "scope": "GOOGLE",
    |      "types": [
    |        "restaurant",
    |        "food",
    |        "point_of_interest",
    |        "establishment"
    |      ],
    |      "user_ratings_total": 2,
    |      "vicinity": "Belarus"
    |    }
    |  ],
    |  "status": "OK"
    |}
    |""".stripMargin

  val googleResultObject = decode[SearchResponse](googleResult)
}
