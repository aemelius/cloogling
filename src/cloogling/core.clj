(ns cloogling.core
  (:require [clojure.data.json :as json]))

(defrecord entry [url title snippet])

(defn get-entries
  "Get results from json data retrieved from engine"
  [x]
  (map (fn [x] (->entry (x "Url" ) (x "Title") (x "Description"))) (-> x
                                                     json/read-str
                                                     (get-in ["d" "results"]))))
