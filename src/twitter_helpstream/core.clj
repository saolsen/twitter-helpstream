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
(def words (atom {}))

(defn foldingFn
  "takes a map and adds one to the key"
  [_map _string]
  (if (contains? _map _string)
    (assoc _map _string (+ (get _map _string) 1))
    (assoc _map _string 1)))

(defn count-list
  "Takes a list of strings and returns a map with the strings as keys and counts"
  [wordlist]
  (reduce foldingFn {} wordlist))

(defn get-wordcounts
  "Gets the wordcounts of words in the sentence as a map"
  [sentence]
  ;; Now I don't want to bother with any funny business regaurding
  ;; unicode characters and @peeps and other hashtags (unless they are
  ;; mid sentence)
  (if (not= nil sentence)
    (count-list (s/split sentence #"\s"))
    (hash-map)))

(defn get-tweet
  "Gets the tweet from the json"
  ;;Always returns a string or nil
  [json]
  (try 
    (:text (js/read-json json true false {}))
    (catch Exception e
      ())))

(defn process-tweet
  "Main processing function called on every tweet"
  [tweet-json]
  ;; Reads the json and then prints the wordcount
  (let [tweet (get-tweet tweet-json)
        wordcounts (get-wordcounts tweet)]
    (swap! words (fn [_map] (merge-with + _map wordcounts)))))

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

;; Finially we start the stream processing in a background thread (so that I can
;; still work with words and other slime things while it runs)
;; I shall be able to spin up many stream listening threads at once in this manner.
(future startStream u p)