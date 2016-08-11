(ns cloogling.core
  (:require [clojure.data.json :as json]))

(defrecord entry [url title snippet])


(defn create-entry
  "create an entry record"
  [x]
  (try
    (->entry (x "Url" ) (x "Title") (x "Description"))
    (catch IllegalArgumentException e nil)
    )
  )

(defn get-entries
  "Get results from json data retrieved from engine"
  [x]
  (remove nil? (map create-entry (try
                                   (-> x
                                       json/read-str
                                       (get-in ["d" "results"]))
                                   (catch Exception e (empty nil))
                                   )
                    ))
  )



