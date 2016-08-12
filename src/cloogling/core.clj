(ns cloogling.core
  (:require [clojure.data.json :as json]))

(defrecord entry [url title snippet])

(defn create-entry
  [x]
  (let [bing (->entry (x "Url" ) (x "Title") (x "Description"))
        google (->entry (x "link" ) (x "title") (x "snippet") ) ]

    (if (and (:url bing) (:title bing) (:snippet bing))
      bing
      google
      )

    )
  )


(defn get-entries
  (
    [x]
    (remove nil? (map create-entry (try
                                     (or
                                       (-> x
                                           json/read-str
                                           (get-in  ["d" "results"]))
                                       (-> x
                                           json/read-str
                                           (get-in ["items"])))
                                     (catch Exception e (empty nil))
                                     )
                      )))

  )









