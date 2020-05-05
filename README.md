# Place hunter

Place Hunter is a Telegram Bot that helps to find a nice place nearby. 

## Steps to Run

* Get a token for your bot using BotFather - https://core.telegram.org/bots#6-botfather. Please set the token for your bot in
    * PLACE_HUNTER_BOT_TOKEN env variable
* If you inject MockGooglePlacesAPI, the bot is going to use prepared response for every request.
* If you inject GooglePlacesApi, the bot is asking Google Places API. Thus it needs APP ID and API Key. Please set them to following env variables:
    * GOOGLE_API_KEY
* Run Launcher
* Enjoy talking to your bot!

## Bot

* @EGPlaceHunterBot helps to find a nice place in some radius:
    * start a new search with /search command
    * bot asks questions
        * place type
        * distance
        * send location
    * bot lists up to 20 places
        * places are sorted by rating and reviews
        * shows 5 places and allows to show next 5
        * liked/chosen places are stored
    * last 10 chosen places can be listed with /chosen_locations

## Technologies

* bot4s
* http4s client
* circe
* tagless final
* cats, cats effects
* log4cats
* doobie, mysql, hikari CP

## Difficulties

* versions of libraries
* deal with monad stack F[Option[...]] -> monad transformers(OptionT) helped
* how to delay and wrap side effects properly
