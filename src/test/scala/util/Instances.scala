package util

import com.bot4s.telegram.models.Location
import model.ChatId

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
    Location(Math.abs(random.nextDouble()), Math.abs(random.nextDouble()))
  }
}
