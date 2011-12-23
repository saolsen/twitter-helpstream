(ns twitter-helpstream.main
  (:use [twitter-helpstream.core :only [startStream printMap]])
  (:gen-class))

;; Need to get the username and password from a twitter account somehow, I keep
;; them as ENV variables.
(def u (System/getenv "TWITTER_UNAME"))
(def p (System/getenv "TWITTER_PASSWORD"))

(defn -main
  "Main entry point"
  []
  (let [stream (future (startStream u p))
        prints (future (printMap 15))]
    (loop [] () (recur))))