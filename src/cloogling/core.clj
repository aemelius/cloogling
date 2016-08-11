(ns cloogling.core
  (:require [clojure.data.json :as json]))

(defrecord entry [url])

(defn get-entries
  "Get results from json data retrieved from engine"
  [x]
  (map (fn [x] (->entry (x "Url" ))) (-> x
      json/read-str
      (get-in ["d" "results"]))))