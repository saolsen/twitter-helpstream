(ns twitter-helpstream.main
  (:use [twitter-helpstream.core :only [startStream printMap]]
        [twitter-helpstream.web :only [handler]]
        [ring.adapter.jetty])
  (:gen-class))

;; Need to get the username and password from a twitter account somehow, I keep
;; them as ENV variables.
(def u (System/getenv "TWITTER_UNAME"))
(def p (System/getenv "TWITTER_PASSWORD"))

(defn -main
  "Main entry point"
  []
  (let [consumer (future (startStream u p))
        streamer (future (printMap))
        port (Integer/parseInt (System/getenv "PORT"))]
    (do
      (println "Beginning Server")
      (println "Username:" u \newline "Password:" p)
      (run-jetty handler {:port port}))))