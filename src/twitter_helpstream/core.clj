(ns twitter-helpstream.core
  (:require [http.async.client :as c]
            [clojure.data.json :as js]
            [clojure.string :as s]))

;; Need to get the username and password from a twitter account somehow, I keep
;; them as ENV variables.
(def u (System/getenv "TWITTER_UNAME"))
(def p (System/getenv "TWITTER_PASSWORD"))

;; Couple sets that I use for filtering things
(def valid-characters #{\a \b \c \d \e \f \g \h \i \j \k \l \m \n \o \p \q \r
                        \s \t \u \v \w \x \y \z \. \, \? \! \# \@ \' \"})
(def punctuation #{\. \, \? \! \@ \# \"})

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
      ())))

(defn process-tweet
  "Main processing function called on every tweet"
  [tweet-json]
  ;; Adds the wordcounts in each tweet to words
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