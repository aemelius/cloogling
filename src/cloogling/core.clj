(ns cloogling.core
  (:require [clojure.data.json :as json]))

(defrecord entry [url title snippet])

(defn creator
  [x]
  (if (and (x "Url") (x "Title") (x "Description"))
    (try
      (->entry (x "Url" ) (x "Title") (x "Description"))
      (catch IllegalArgumentException e nil)
      )

    (try
      (->entry (x "link" ) (x "title") (x "snippet"))
      (catch IllegalArgumentException e nil)
      )


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

(defn get-entries
  (
    [x f]
    (remove nil? (map creator (try
                                (-> x
                                    json/read-str
                                    f)
                                (catch Exception e (empty nil))
                                )
                      )))

  )









