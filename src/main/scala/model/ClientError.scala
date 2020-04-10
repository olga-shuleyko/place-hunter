package model

trait ClientError extends Exception

object ClientError {
  final case class PlaceTypeIsIncorrect(chatId: ChatId) extends ClientError
}