(ns cloogling.core
  (:require [clojure.data.json :as json]))

(defrecord entry [url title snippet])


(defn create-entry
(  [x]
  (try
    (->entry (x "Url" ) (x "Title") (x "Description"))
    (catch IllegalArgumentException e nil)
    )
)

(  [x y]
  (try
    (->entry (x "link" ) (x "title") (x "snippet"))
    (catch IllegalArgumentException e nil)
    )
)




  )

(defn get-entries
  (
  [x]
  (remove nil? (map create-entry (try
                                   (-> x
                                       json/read-str
                                       (get-in ["d" "results"]))
                                   (catch Exception e (empty nil))
                                   )
                    )))
  (
  [x y]
  (remove nil? (map create-entry (try
                                   (-> x
                                       json/read-str
                                       (get-in ["items"]))
                                   (catch Exception e (empty nil))
                                   )
                    )))
  )



