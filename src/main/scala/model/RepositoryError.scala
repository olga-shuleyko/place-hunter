package model

trait RepositoryError extends Exception

object RepositoryError {
  final case class SearchRecordIsMissing(chatId: ChatId) extends RepositoryError
}
