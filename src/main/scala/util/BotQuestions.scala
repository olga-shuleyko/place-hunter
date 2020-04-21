package util

object BotQuestions {

  val place = "What are you looking for?"

  val distance =
    """How far are you ready to walk?
      |The input format is "up to *.*km" or "up to *.*m".
      |Search radius limit is 50km.""".stripMargin

  val location = "Can you please send the location to start search?"

  val recommends = "Best places go first:"

  val nothingToRecommend =
    """Bot can't find anything at this location and distance.
      |Please start a new search with /search.""".stripMargin

  val selectResult = "Which result do you like?"

  val finishSearch = "You have chosen: \n"

  val newSearch = "\nPlease start a new search with /search."

  val dislikeSearch =
    """I'm so sorry to disappoint you!
      |Please start a new search with /search.""".stripMargin
}
