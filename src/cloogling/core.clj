(ns cloogling.core
  (:require [clojure.data.json :as json]
            [cemerick.url :refer (url url-encode)]
            [clj-http.client :as http]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]))

(use 'clojure.data)
(use 'clojure.java.io)

;; record used to store data about a single entry returned within a search
(defrecord entry [url title snippet engine])

(defn create-entry
  "Given a json snippet representing an entry returned by a query,
  I create an entry record. This supports both google and bing formats."
  [x]
  (let [bing (->entry (x "Url" ) (x "Title") (x "Description") "bing")
        google (->entry (x "link" ) (x "title") (x "snippet") "google") ]

    (if (and (:url bing) (:title bing) (:snippet bing))
      bing
      (if (and (:url google) (:title google) (:snippet google))
        google
        nil)
      )

    )
  )


(defn get-entries
  "Given the body of a search (in json format), returns a list of entry records.
  This works for both google and bing."
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
                    ))

  )

(defn get-google-query
  "Return the url to be used to issue a query to the google api"
  [engine-id api-key search-key]
  (if (or (= search-key nil) (= search-key ""))
    (throw (Exception. "Null search keys confuse me."))
    (str "https://www.googleapis.com/customsearch/v1?num=10&cx="
         engine-id
         "&key="
         api-key
         "&q="
         (url-encode search-key)
         )
    )
  )

(defn get-bing-query
  "Return the url to be used to issue a query to the bing api"
  [search-key]
  (if (or (= search-key nil) (= search-key ""))
    (throw (Exception. "Null search keys confuse me."))
    (str "https://api.datamarket.azure.com/Bing/Search/Web?$top=10&Query='"
         (url-encode search-key)
         "'&$format=json"
         )
    )
  )

(defn get-url-for-comparison
  "When comparig the results, I ignore differences due to http/https protocol, or omission of www prefix."
  [entry]
  (-> entry
      :url
      (clojure.string/replace-first #"^https" "http")
      (clojure.string/replace-first #"^http://www." "http://")
      )
  )


(defn get-common-urls
  "Given two lists of entries, I return a list of duplicated entries, as found in the second list"
  [one two]
  (for [ item two
         :when (contains? (clojure.set/intersection (set (map get-url-for-comparison one)) (set (map get-url-for-comparison two))) (get-url-for-comparison item) )]
    item)
  )


(defn get-aggregated-result
  "I aggregate the results in the two list of entries.
  Precedence is given to results in the first list (the last result of the first list is presented before the first result of the second).
  Urls in both lists are presented only as results of the first list."
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
  "Issue a query to google"
  [engine-id api-key search-key]
  (-> (get-google-query engine-id api-key search-key)
      http/get
      (:body)
      get-entries)
  )


(defn search-bing
  "Issue a query to bing"
  [username password x]
  (-> (get-bing-query x)
      (http/get {:basic-auth [username password]} )
      (:body)
      get-entries))



(defn simple-similarity-metric
  "Given two entries lists, computes a very simple similarity metric as the number of shared results divided by the number of entries in the first list."
  [x y]
  (try (str (format "%3f" (* 100 (float (/ (count x) (count y))))) "%")
    (catch ArithmeticException e "0.0%" )
    )
  )


(defn uber-query
  "Issues both queries to google and bing, and returns the aggregated result,
  the entries returned by both queries, and a simple similarity matric."
  [x]
  (let [google-result (search-google (read-property "config.json" "google" "engine-id")
                                     (read-property "config.json" "google" "api-key")
                                     x)
        bing-result (search-bing (read-property "config.json" "bing" "username")
                                 (read-property "config.json" "bing" "password")
                                 x)
        duplicated-in-bing (get-common-urls google-result bing-result)

        similarity-metric (simple-similarity-metric duplicated-in-bing google-result)
        ]

    [(get-aggregated-result google-result bing-result) duplicated-in-bing similarity-metric ]

    )
  )



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



(defn print-entry
  "Returns a string representation of an entry record. Supposed to be human readable."
  [x]
  (str "" (:url x) "\n\tTitle: \"" (:title x) "\"\n\tSnippet: \"" (:snippet x) "\"\n\tfrom: " (:engine x) "\n")
  )


(defn get-printable-result
  "Returns a string representation of a list of entries. Supposed to be human readable."
  [entries]
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

    (let [ uber-query-result  (uber-query (first arguments))
           ]



      (println  (get-printable-result (first uber-query-result))
                "\n-------------------------\n\n"
                "The following results were duplicated in Bing:\n\n"
                (get-printable-result (second uber-query-result))
                "\n-------------------------\n\n"
                "A simple similarity metric (does not consider results order): "
                (nth uber-query-result 2)



                )

      )
    )
  )



