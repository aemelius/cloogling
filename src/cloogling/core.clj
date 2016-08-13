(ns cloogling.core
  (:require [clojure.data.json :as json]
            [cemerick.url :refer (url url-encode)]))

(use 'clojure.data)

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

(defn get-bing-query
  [x]
  (if (or (= x nil) (= x ""))
    (throw (Exception. "Null search keys confuse me."))
    (str "https://api.datamarket.azure.com/Bing/Search/Web?$top=10&Query="
         (url-encode x)
         "&$format=json"
         )
    )
  )

(defn get-url
  [x]
  (:url x))

(defn get-common-urls
  [one two]
  (remove nil?   (concat (map get-url one)
                         (-> (diff (map get-url one) (map get-url two))
                             (nth 2) ;; urls only in two
                             )))
  )

(defn get-aggregated-result
  [one two]

  (concat one
          (for [item two
                :when (not-any? (fn [x] (= (:url item) (:url x) )) one )]
            item )
          )
  )














