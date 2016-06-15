(defproject counsel-id "0.1.0-SNAPSHOT"
  :description "A ELIZA slackbot to for unobstrusive, automated team counseling"
  :url "https://github.com/puhrez/eliza-bot"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.2.0"]
                 [cheshire "5.6.1"]
                 [compojure "1.5.0"]
                 [stylefruits/gniazdo "1.0.0"]
                 [org.julienxx/clj-slack "0.5.4"]]
  :main ^:skip-aot counsel-id.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
