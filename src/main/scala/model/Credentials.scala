package model

object Credentials {
  final case class BotToken(token: String) extends AnyVal

  final case class PlacesAPIKey(key: String) extends AnyVal

  final case class BotKeys(botToken: BotToken, placesAPIKey: PlacesAPIKey)
}
