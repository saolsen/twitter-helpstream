(ns twitter-helpstream.core
  (:require [http.async.client :as c]
            [clojure.data.json :as js]
            [clojure.string :as s]))

;; Couple sets that I use for filtering things
(def valid-characters #{\a \b \c \d \e \f \g \h \i \j \k \l \m \n \o \p \q \r
                        \s \t \u \v \w \x \y \z \. \, \? \! \#  \' \"})
(def punctuation #{\. \, \? \! \# \"})

;; Stores the map of wordcounts
(def words (atom {}))

;; Helper functions
(defn is-valid-word?
  "Used for filtering out weird unicode and other sillyness"
  [word]
  (reduce #(and %1 %2)
          (map #(contains? valid-characters %) word)))

(defn strip-punc
  "Strips the 'punctuation' out of the word"
  [word]
  (filter #(not (contains? punctuation %)) word))

(defn to-words
  "Takes the list of 'words' and filters out the crap."
  [wordlist]
  (let [low_words (map s/lower-case wordlist)
        val_words (filter is-valid-word? low_words)
        no_punc (map strip-punc val_words)]
    (vec (map #(apply str %) no_punc))))

(defn count-list
  "Takes a list of strings and returns a map with the strings as keys and counts"
  [wordlist]
  (let [keyPlusOne (fn [m s]
                     (if (contains? m s)
                       (assoc m s (+ (get m s) 1))
                       (assoc m s 1)))]
    (reduce keyPlusOne {} wordlist)))

(defn get-wordcounts
  "Gets the wordcounts of words in the sentence as a map"
  [sentence]
  ;; Now I don't want to bother with any funny business regaurding
  ;; unicode characters and @peeps and other hashtags (unless they are
  ;; mid sentence)
  (if (not= nil sentence)
    (count-list (to-words (s/split sentence #"\s")))
    (hash-map)))

(defn get-tweet
  "Gets the tweet from the json"
  ;;Always returns a string or nil
  [json]
  (try
    (:text (js/read-json json true false {}))
    (catch Exception e
      nil)))

(defn process-tweet
  "Main processing function called on every tweet"
  [tweet-json]
  ;; Adds the wordcounts in each tweet to words
  (let [tweet (get-tweet tweet-json)
        wordcounts (get-wordcounts tweet)]
      (swap! words #(merge-with + % wordcounts))))

;; Startstream will be run in one future, it will watch the stream and update
;; the wordcounts
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

;; This is for me to be able to see that it is indeed running. It will run in
;; another future and print to standard out. (will not run in production)
(defn printMap
  "Prints the map every n seconds"
  [n]
  (let [secs (* 1000 n)]
    (loop []
      (do
        (println @words)
        (. java.lang.Thread sleep secs))
      (recur))))