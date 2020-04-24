package model

trait ClientError extends Exception

object ClientError {

  final case class PlaceTypeIsIncorrect(chatId: ChatId) extends ClientError {
    override def getMessage: String = s"Place Type is incorrect for $chatId."
  }

  final case class DistanceIsIncorrect(chatId: ChatId) extends ClientError {
    override def getMessage: String = s"Distance is incorrect for $chatId."
  }

  final case class ParseError(message: String) extends ClientError {
    override def getMessage: String = message
  }

}