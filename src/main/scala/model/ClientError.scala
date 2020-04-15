package model

trait ClientError extends Exception

object ClientError {
  final case class PlaceTypeIsIncorrect(chatId: ChatId) extends ClientError
  final case class ParseError(chatId: ChatId, message: String) extends ClientError {
    override def getMessage: String = message
  }
}