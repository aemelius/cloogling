(ns cloogling.core
  (:require [clojure.data.json :as json]
            [cemerick.url :refer (url url-encode)]
            [clj-http.client :as http]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]))

(use 'clojure.data)
(use 'clojure.java.io)


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
      (clojure.string/replace-first #"^https" "http")
      (clojure.string/replace-first #"^http://www." "http://")
      )
  )


(-> "https://"
    (clojure.string/replace-first #"^https" "http")
    (clojure.string/replace-first #"^http://www." "http://")
    )
(clojure.string/replace-first "http://www." #"^https" "http")


(defn get-common-urls
  [one two]
  (for [ item two
         :when (contains? (clojure.set/intersection (set (map get-url-for-comparison one)) (set (map get-url-for-comparison two))) (get-url-for-comparison item) )]
    item)
  )


(defn get-aggregated-result
  [one two]

  (concat one
          (for [item two
                :when (not-any? (fn [x] (=   ( get-url-for-comparison item) (get-url-for-comparison x) )) one )]
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

;;(uber-query "Miles Davis")


(defn usage [options-summary]
  (->> ["Welcome to the Cloogling help message!"
        ""
        "Usage (with leiningen run): lein run --quote-arguments search-term"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))



(defn print-entry [x]
  (str "" (:url x) "\n\tTitle: \"" (:title x) "\"\n\tSnippet: \"" (:snippet x) "\"\n")
  )


(defn get-printable-result [entries]
  (->> (map print-entry entries)
       (string/join \newline)
       )
  )


(def cli-options
  [["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]

    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors))
      (not= (count arguments) 1) (exit 1 (usage summary))
      (= (first arguments) "") (exit 1 (usage summary))
      )
    ;; Execute program with options
    (let [ uber-query-result  (uber-query (first arguments))
           ]



      (println  (get-printable-result (first uber-query-result))
                "\n-------------------------\n\n"
                "The following results were duplicated in Bing:\n\n"
                (get-printable-result (second uber-query-result))



                )

      )
    )
  )



