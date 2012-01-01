(ns twitter-helpstream.web
  (:use [twitter-helpstream.core :only [words]]
        [ring.adapter.jetty]))

;; A little web frontend, for now it will let you view the current
;; wordcount map and then later maybe past days, it will also show
;; whatever it is I want to do with this data eventually.
(defn get-wordmap
  "Reads the current wordmap"
  []
  (str @words))

(defn handler [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (get-wordmap)})
