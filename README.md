# ELIZA-bot

A simple slack bot based off [ELIZA](https://en.wikipedia.org/wiki/ELIZA).

## Usage

Check out the [slack bot users documentation](https://api.slack.com/bot-users)

    # from the repo's directory
    $ lein uberjar
    $ java -jar target/uberjar/counsel-id-0.1.0-standalone.jar [SLACK-API-TOKEN]

## Options

* `slack-api-token`: The api-token for you bot to talk through
