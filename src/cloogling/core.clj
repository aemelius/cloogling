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

(defn- access-to-data-google
  [x]
  (get-in x ["items"])
  )

(defn- access-to-data-bing
  [x]
  (get-in x  ["d" "results"])
  )

(defn get-entries
  (
    [x]
    (remove nil? (map create-entry (try
                                     (if (-> x
                                             json/read-str
                                             access-to-data-google)
                                       (-> x
                                           json/read-str
                                           access-to-data-google)
                                       (-> x
                                           json/read-str
                                           access-to-data-bing))
                                     (catch Exception e (empty nil))
                                     )
                      )))

  )









