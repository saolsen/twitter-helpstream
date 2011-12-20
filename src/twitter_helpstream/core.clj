(ns twitter-helpstream.core
  (:require [http.async.client :as c]
            [clojure.data.json :as js]
            [clojure.string :as s]))

;; Need to get the username and password from a twitter account somehow, I keep
;; them as ENV variables.
(def u (System/getenv "TWITTER_UNAME"))
(def p (System/getenv "TWITTER_PASSWORD"))

;; Lets make a map that we can store wordcounts in, we'll wrap it
;; in an agent so we can hit it from multiple threads.
(def words (agent hash-map))

(defn get-wordcounts
  "Gets the wordcounts of words in the sentence as a map"
  [sentence]
  ;; Now I don't want to bother with any funny business regaurding
  ;; unicode characters and @peeps and other hashtags (unless they are
  ;; mid sentence)
  (try
    (s/split sentence #"\s")
    (catch Exception e (vector))))

(defn get-tweet
  "Gets the tweet from the json"
  [json]
  (:text (js/read-json json true false "")))

(defn process-tweet
  "Main processing function called on every tweet"
  [tweet-json]
  ;; Reads the json and then prints the part I care about, the tweet!
  (let [tweet (get-tweet tweet-json)]
    (println (get-wordcounts tweet))))

(defn startStream
  "Connects to the twitter stream and processes tweets"
  [username password]
  (loop []
      (with-open [twitter (c/create-client)]
        (let [tweet (c/stream-seq
                     twitter
                     :post "https://stream.twitter.com/1/statuses/filter.json"
                     :query {"track" "#help"}
                     :auth {:user username :password password})]
          (doseq [s (c/string tweet)]
            (process-tweet s))))
      (recur)))
