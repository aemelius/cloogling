(ns cloogling.core
  (:require [clojure.data.json :as json]))

(defn get-entries
  "Get results from json data retrieved from engine"
  [x]
  (-> x
      json/read-str
      (get-in ["d" "results"])))
