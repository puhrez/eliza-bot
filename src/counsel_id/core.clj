(ns counsel-id.core
  (:require [clj-slack.rtm :refer [start]]
            [clj-slack.users :as users]
            [gniazdo.core :as ws]
            [cheshire.core :refer [generate-string parse-string]])
  (:gen-class))

(def slack-api-url {:api-url "https://slack.com/api"})
(def connection (atom nil))

(def pairs [
            [#"(?i)I need (.*)"
                 ["Why do you need %s?"
                  "Would it really help you to get %s?"
                  "Are you sure you need %s?"]]

            [#"(?i)Why don't you (.*)"
                   ["Do you really think I don't %s?"
                    "Perhaps eventually I will %s."
                    "Do you really want me to %s?"]]

            [#"(?i)Why can't I (.*)"
                   ["Do you think you should be able to %s?"
                    "If you could %s, what would you do?"
                    "I don't know -- why can't you %s?"
                    "Have you really tried?"]]

            [#"(?i)I can't (.*)"
                 ["How do you know you can't %s?"
                  "Perhaps you could %s if you tried."
                  "What would it take for you to %s?"]]

            [#"(?i)I am (.*)"
                 ["Did you come to me because you are %s?"
                  "How long have you been %s?"
                  "How do you feel about being %s?"]]

            [#"(?i)I'm (.*)"
                    ["How does being %s make you feel?"
                     "Do you enjoy being %s?"
                     "Why do you tell me you're %s?"
                     "Why do you think you're %s?"]]

            [#"(?i)Are you (.*)"
                   ["Why does it matter whether I am %s?"
                    "Would you prefer it if I were not %s?"
                    "Perhaps you believe I am %s."
                    "I may be %s -- what do you think?"]]

            [#"(?i)What (.*)"
                    ["Why do you ask?"
                     "How would an answer to that help you?"
                     "What do you think?"]]

            [#"(?i)How (.*)"
                   ["How do you suppose?"
                    "Perhaps you can answer your own question."
                    "What is it you're really asking?"]]

            [#"(?i)Because (.*)"
                       ["Is that the real reason?"
                        "What other reasons come to mind?"
                        "Does that reason apply to anything else?"
                        "If %s, what else must be true?"]]

            [#"(?i)(.*) sorry (.*)"
              ["There are many times when no apology is needed."
               "What feelings do you have when you apologize?"]]

            [#"(?i)(Hello|Hey|Hi|Sup)(.*)"
                    ["Hello... I'm glad you could drop by today."
                     "Hi there... how are you today?"
                     "Hello, how are you feeling today?"]]

            [#"(?i)I think (.*)"
                 ["Do you doubt %s?"
                  "Do you really think so?"
                  "But you're not sure %s?"]]

            [#"(?i)(.*) friend (.*)"
              ["Tell me more about your friends."
               "When you think of a friend, what comes to mind?"
               "Why don't you tell me about a childhood friend?"]]

            [#"(?i)Yes"
             ["You seem quite sure."
              "OK, but can you elaborate a bit?"]]

            [#"(?i)(.*) computer(.*)"
              ["Are you really talking about me?"
               "Does it seem strange to talk to a computer?"
               "How do computers make you feel?"
               "Do you feel threatened by computers?"]]

            [#"(?i)Is it (.*)"
                  ["Do you think it is %s?"
                   "Perhaps it's %s -- what do you think?"
                   "If it were %s, what would you do?"
                   "It could well be that %s."]]

            [#"(?i)It is (.*)"
                  ["You seem very certain."
                   "If I told you that it probably isn't %s, what would you feel?"]]

            [#"(?i)Can you (.*)"
                   ["What makes you think I can't %s?"
                    "If I could %s, then what?"
                    "Why do you ask if I can %s?"]]

            [#"(?i)Can I (.*)"
                   ["Perhaps you don't want to %s."
                    "Do you want to be able to %s?"
                    "If you could %s, would you?"]]

            [#"(?i)You are (.*)"
                   ["Why do you think I am %s?"
                    "Does it please you to think that I'm %s?"
                    "Perhaps you would like me to be %s."
                    "Perhaps you're really talking about yourself?"]]

            [#"(?i)You're (.*)"
                       ["Why do you say I am %s?"
                        "Why do you think I am %s?"
                        "Are we talking about you, or me?"]]

            [#"(?i)I don't (.*)"
                 ["Don't you really %s?"
                  "Why don't you %s?"
                  "Do you want to %s?"]]

            [#"(?i)I feel (.*)"
                 ["Good, tell me more about these feelings."
                  "Do you often feel %s?"
                  "When do you usually feel %s?"
                  "When you feel %s, what do you do?"]]

            [#"(?i)I have (.*)"
                 ["Why do you tell me that you've %s?"
                  "Have you really %s?"
                  "Now that you have %s, what will you do next?"]]

            [#"(?i)I would (.*)"
                 ["Could you explain why you would %s?"
                  "Why would you %s?"
                  "Who else knows that you would %s?"]]

            [#"(?i)Is there (.*)"
                  ["Do you think there is %s?"
                   "It's likely that there is %s."
                   "Would you like there to be %s?"]]

            [#"(?i)My (.*)"
                  ["I see, your %s."
                   "Why do you say that your %s?"
                   "When your %s, how do you feel?"]]

            [#"(?i)You (.*)"
                   ["We should be discussing you, not me."
                    "Why do you say that about me?"
                    "Why do you care whether I %s?"]]

            [#"(?i)Why (.*)"
                   ["Why don't you tell me the reason why %s?"
                    "Why do you think %s?" ]]

            [#"(?i)I want (.*)"
                 ["What would it mean to you if you got %s?"
                  "Why do you want %s?"
                  "What would you do if you got %s?"
                  "If you got %s, then what would you do?"]]

            [#"(?i)(.*) mother(.*)"
              ["Tell me more about your mother."
               "What was your relationship with your mother like?"
               "How do you feel about your mother?"
               "How does this relate to your feelings today?"
               "Good family relations are important."]]

            [#"(?i)(.*) father(.*)"
              ["Tell me more about your father."
               "How did your father make you feel?"
               "How do you feel about your father?"
               "Does your relationship with your father relate to your feelings today?"
               "Do you have trouble showing affection with your family?"]]

            [#"(?i)(.*) child(.*)"
              ["Did you have close friends as a child?"
               "What is your favorite childhood memory?"
               "Do you remember any dreams or nightmares from childhood?"
               "Did the other children sometimes tease you?"
               "How do you think your childhood experiences relate to your feelings today?"]]

            [#"(?i)(.*)\?"
              ["Why do you ask that?"
               "Please consider whether you can answer your own question."
               "Perhaps the answer lies within yourself?"
               "Why don't you tell me?"]]

            [#"(?i)quit"
             ["Thank you for talking with me."
              "Good-bye."
              "Thank you, that will be $150.  Have a good day!"]]

            [#"(?i)(puhrez|perez|michael|poorez)(.*)"
             ["Oh! That guy's great!"]]

            [#"(?i)(.*)"
              ["Please tell me more."
               "Let's change focus a bit... Tell me about your family."
               "Can you elaborate on that?"
               "Why do you say that %s?"
               "I see."
               "Very interesting."
               "%s..."
               "I see.  And what does that tell you?"
               "How does that make you feel?"
               "How do you feel when you say that?"]]])

(defn take-random
  [xs]
  (nth xs (rand-int (count xs))))

(defn get-resp
  [text]
  (let [matching-pair (first (filter #(re-matches (first %) text) pairs))
        regex (first matching-pair)
        phrases (second matching-pair)
        resp (take-random phrases)
        phrase (second (re-matches regex text))]
    (format resp phrase)))

(defn send-msg
  [con channel text]
  (let [msg {:type "message" :text text :channel channel}
        json (generate-string msg)]
    (ws/send-msg con json)))

(defmulti react :type)
(defmethod react :default [event]
  event)

(defn prettify-msg-log
  [channel user text]
  (str "Just got a message on " channel " from " user " saying, " text))

(defmethod react "message" [event]
  (if-let [con @connection]
    (let [channel (:channel event)
          text (:text event)
          user (:user event)
          resp (get-resp text)]
      (send-msg con channel resp)
      (prettify-msg-log channel user text))
    (str "Shit... not connection")))

(def parse-n-react (comp react #(parse-string % true)))

(defn close-slack
  [con]
  (ws/close con))

(defn connect-to-slack
  [auth-key]
  (let [ws-url (:url (start (merge slack-api-url {:token auth-key})))
        con (ws/connect ws-url :on-receive (comp prn parse-n-react))]
    (when @connection
      (close-slack @connection))
    (reset! connection con)))

(defn -main
  [auth-key]
  (println (str "connecting to " auth-key))
  (connect-to-slack auth-key)
  (println "connected"))
