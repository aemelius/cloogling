(ns cloogling.core
  (:require [clojure.data.json :as json]
            [cemerick.url :refer (url url-encode)]))

(defrecord entry [url title snippet])
(defn create-entry
  [x]
  (let [bing (->entry (x "Url" ) (x "Title") (x "Description"))
        google (->entry (x "link" ) (x "title") (x "snippet") ) ]

    (if (and (:url bing) (:title bing) (:snippet bing))
      bing
      (if (and (:url google) (:title google) (:snippet google))
      google
      nil)
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

(defn get-google-query
  [engine-id api-key x]
  (if (or (= x nil) (= x ""))
    (throw (Exception. "Null search keys confuse me."))
  (str "https://www.googleapis.com/customsearch/v1?num=10&cx="
       engine-id
       "&key="
       api-key
       "&q="
       (url-encode x)
  )
  )
)









