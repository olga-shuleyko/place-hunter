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