(ns cloogling.core
  (:require [clojure.data.json :as json]))

(defrecord entry [url title snippet])


(defn create-entry
 [x]
  (try
    (->entry (x "Url" ) (x "Title") (x "Description"))
    (catch IllegalArgumentException e nil)
    )
)

(defn create-entry-google
 [x]
  (try
    (->entry (x "link" ) (x "title") (x "snippet"))
    (catch IllegalArgumentException e nil)
    )
)


(defn g
  [x]
  (get-in x ["items"])
  )

(defn b
  [x]
  (get-in x  ["d" "results"])
  )

(defn get-entries-generic
  (
  [x f create]
  (remove nil? (map create (try
                                   (-> x
                                       json/read-str
                                       f)
                                   (catch Exception e (empty nil))
                                   )
                    )))

  )









