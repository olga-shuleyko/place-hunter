package model

trait RepositoryError extends Exception

object RepositoryError {
  final case class SearchRecordIsMissing(chatId: ChatId) extends RepositoryError {
    override def getMessage: String = s"Search Record is missing for $chatId."
  }
}
