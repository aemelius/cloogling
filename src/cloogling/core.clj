(ns cloogling.core
  (:require [clojure.data.json :as json]
            [cemerick.url :refer (url url-encode)]
            [clj-http.client :as http]))

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
    (str "https://api.datamarket.azure.com/Bing/Search/Web?$top=10&Query='"
         (url-encode x)
         "'&$format=json"
         )
    )
  )

(defn get-url-for-comparison
  [x]
  (-> x
     :url
    (clojure.string/replace-first "https" "http")
    )
)



(defn get-common-urls
  [one two]
  (for [ item one
  :when (contains? (clojure.set/intersection (set (map get-url-for-comparison one)) (set (map get-url-for-comparison two))) (get-url-for-comparison item) )]
    (:url item)))


(defn get-aggregated-result
  [one two]

  (concat one
          (for [item two
                :when (not-any? (fn [x] (= (:url item) (:url x) )) one )]
            item )
          )
  )

(defn read-property
  [file scope property-name]
  (-> file
    slurp
    json/read-str
    (get-in [scope, property-name])))

(defn search-google
  [engine-id api-key x]
  (-> (get-google-query engine-id api-key x)
      http/get
      (:body)
      get-entries)
  )

;;(search-google (read-property "config.json" "google" "engine-id")
;;               (read-property "config.json" "google" "api-key")
;;               "Miles Davis")

(defn search-bing
 [username password x]
  (-> (get-bing-query x)
      (http/get {:basic-auth [username password]} )
      (:body)
      get-entries))


;;(search-bing (read-property "config.json" "bing" "username")
;;             (read-property "config.json" "bing" "password")
;;             "Miles Davis")

(defn uber-query
  [x]
  (let [google-result (search-google (read-property "config.json" "google" "engine-id")
               (read-property "config.json" "google" "api-key")
               x)
        bing-result (search-bing (read-property "config.json" "bing" "username")
             (read-property "config.json" "bing" "password")
             x)]

    [(get-aggregated-result google-result bing-result) (get-common-urls google-result bing-result)]

    )
  )

;;(-> (uber-query "Miles Davis")
;;    second)

(clojure.string/replace "https://en.wikipedia.org/wiki/Miles_Davis"  #"^http.://" "")














