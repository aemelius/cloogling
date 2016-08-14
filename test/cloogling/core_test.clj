(ns cloogling.core-test
  (:require [cloogling.core :refer :all]
            [expectations :refer :all]))

(require '[clojure.string :as str])


;; setup: load a standard json response from Bing, with 10 entries
(def miles_davis_data (slurp "test/cloogling/miles_davis_bing.json"))

;; setup: load a standard json response from Google, with 10 entries
(def miles_davis_data_google (slurp "test/cloogling/miles_davis_google.json"))


;; starting with parsing Bing-style results, with non-edge cases


;; Counting entries in standard test data
(expect 10 (count (get-entries    miles_davis_data)))

;; test on the first entry's url
(expect "https://en.wikipedia.org/wiki/Miles_Davis"
        (-> miles_davis_data
            get-entries
            first
            (:url)))

;; test on the first entry's title
(expect "Miles Davis - Wikipedia, the free encyclopedia"
        (-> miles_davis_data
            get-entries
            first
            (:title)))

;; test on the first entry's snippet
(expect "Miles Dewey Davis III (May 26, 1926 – September 28, 1991) was an American jazz trumpeter, bandleader, and composer. He is among the most influential and acclaimed ..."
        (-> miles_davis_data
            get-entries
            first
            (:snippet)))

;; now some slightly edgier cases for Bing

;; if the data is not a valid json, I expect an empty list to be returned
(expect [] (get-entries    "this is not a valid bing like json"))

;; if the data is valid json, but is in a different structure than what I expect from Bing, I will return an empty list
(expect [] (get-entries    (slurp "test/cloogling/bad_data_bing_d_or_results.json")))

;; valid json, but does not contain a list of entries
(expect [] (get-entries    (slurp "test/cloogling/bad_data_bing_2.json")))

;; valid json, it contains a list of entries in the right place, but the keys are not what expected from Bing
(expect [] (get-entries    (slurp "test/cloogling/bad_data_bing_3.json")))

;; valid json, it contains a list of entries in the right place, but the keys are not what expected from Bing - case 2
(expect [] (get-entries    (slurp "test/cloogling/bad_data_bing_4.json")))

;; now some Google stuff


;; Counting entries in standard test data
(expect 10 (count (get-entries    miles_davis_data_google)))

;; test on the first entry's url
(expect "https://en.wikipedia.org/wiki/Miles_Davis"
        (-> miles_davis_data_google
            get-entries
            first
            (:url)))

;; test on the first entry's title
(expect "Miles Davis - Wikipedia, the free encyclopedia"
        (-> miles_davis_data_google
            get-entries
            first
            (:title)))

;; test on the first entry's snippet
(expect "Miles Dewey Davis III (May 26, 1926 – September 28, 1991) was an American \njazz trumpeter, bandleader, and composer. He is among the most influential and\n ..."
        (-> miles_davis_data_google
            get-entries
            first
            (:snippet)))

;; testing the generation of the url to query the google api
(expect "https://www.googleapis.com/customsearch/v1?num=10&cx=abcd&key=1234&q=Miles%20Davis"
        (get-google-query "abcd" "1234" "Miles Davis"))

;; if you query nothing, I am not going to go on
(try
  (and (get-google-query "abcd" "1234" nil) (expect "An exception should have been raised in case of nil search key" "No exception raised" ))
  (catch Exception e nil)
  )

;; an empty string is like querying nothing
(try
  (and (get-google-query "abcd" "1234" "") (expect "An exception should have been raised in case of empty search key" "No exception raised" ))
  (catch Exception e nil)
  )


;; testing the generation of the url to query the google api
(expect "https://api.datamarket.azure.com/Bing/Search/Web?$top=10&Query='Miles%20Davis'&$format=json"
        (get-bing-query "Miles Davis"))

;; Againg, if you query nothing or an empty string, I am not going to go on
(try
  (and (get-bing-query nil) (expect "An exception should have been raised in case of nil search key" "No exception raised" ))
  (catch Exception e nil)
  )

(try
  (and (get-bing-query "") (expect "An exception should have been raised in case of empty search key" "No exception raised" ))
  (catch Exception e nil)
  )

;; testing the comparison of results from google and bing


;; let's begin with a simple case; duplicated entries are removed from the bing result;
;; google results come first
(let [ google [(->entry "a" "a" "a" "engine") (->entry "b" "x" "sdfsdf" "engine") (->entry "d" "sdf" "dasd"  "engine")]
       bing [(->entry "a" "as" "a"  "engine") (->entry "b" "ds" "d" "engine") (->entry "c" "c" "c" "engine")]]


  (expect [ "a" "b" "d" "c"]
          (map (fn [x] (:url x ))(get-aggregated-result google bing))
          )

  )
;; let's get edgy...

(let [ google [(->entry "a" "x" "a" "engine") (->entry "b" "5" "b" "engine") (->entry "d" "d" "d" "engine")]
       bing []]

  (expect [ "a" "b" "d"]
          (map (fn [x] (:url x ))(get-aggregated-result google bing))
          )
  )


(let [ google []
       bing []]

  (expect [ ]
          (map (fn [x] (:url x ))(get-aggregated-result google bing))
          )
  )

(let [ google nil
       bing nil]

  (expect [ ]
          (map (fn [x] (:url x ))(get-aggregated-result google bing))
          )
  )

(let [ google [(->entry "a" "a" "a" "engine") (->entry "b" "b" "b" "engine") (->entry "d" "d" "d" "engine")]
       bing []]

  (expect [ "a" "b" "d"]
          (map (fn [x] (:url x ))(get-aggregated-result google bing))
          )
  )


;; double-check that result from google is privileged in case of duplication
(let [ google [(->entry "a" "from google" "a" "google") (->entry "b" "from google" "b" "google")]
       bing [(->entry "a" "from bing" "a" "bing") (->entry "b" "from bing" "b" "bing") (->entry "c" "from bing" "b" "bing")]
       aggregated_result (get-aggregated-result google bing)]

  (expect 3 (count aggregated_result)  )
  (expect "google"(:engine (nth aggregated_result 0)))
  (expect "google"(:engine (nth aggregated_result 1)))
  (expect "bing"  (:engine (nth aggregated_result 2)))
  )

;; reading properties from a config file
(expect "abcd"(read-property "config.json.example" "google" "api-key"
                             ))
(expect "1234:abcd"(read-property "config.json.example" "google" "engine-id"
                                  ))
(expect "bing-username"(read-property "config.json.example" "bing" "username"
                                      ))
(expect "bing-password"(read-property "config.json.example" "bing" "password"
                                      ))



(let [ google [(->entry "https://en.wikipedia.org/wiki/Miles_Davis" "x" "a"  "engine") (->entry "https://www.milesdavis.com/" "5" "b" "engine") (->entry "d" "d" "d" "engine")]
       bing [(->entry "https://en.wikipedia.org/wiki/Miles_Davis" "x" "a" "engine") (->entry "https://www.milesdavis.com/" "5" "b" "engine") (->entry "c" "c" "c" "engine")]]


  (get-printable-result google)
  (expect [ "https://en.wikipedia.org/wiki/Miles_Davis"  "https://www.milesdavis.com/" ]
          (map (fn [x] (:url x)) (get-common-urls google bing))
          )
  )


( expect ["https://en.wikipedia.org/wiki/Miles_Davis"
          "http://www.milesdavis.com/"
          "http://www.allmusic.com/artist/miles-davis-mn0000423829"
          "http://www.biography.com/people/miles-davis-9267992" ]
  (map (fn [x] (:url x))(get-common-urls (-> miles_davis_data_google
                       get-entries
                       )
                   (-> miles_davis_data
                       get-entries)
  ))
)

(expect (get-url-for-comparison (->entry "http://abc.com" "jadksdhak" "aksjdhaks" "engine"))
        (get-url-for-comparison (->entry "https://abc.com" "abc" "dfsdf" "engine") )
        )
(expect (get-url-for-comparison (->entry "http://www.johncoltrane.com/" "jadksdhak" "aksjdhaks" "engine"))
        (get-url-for-comparison (->entry "http://johncoltrane.com/" "abc" "dfsdf" "engine") )
        )

(expect (get-url-for-comparison (->entry "https://www.abc.com" "jadksdhak" "aksjdhaks" "engine"))
        (get-url-for-comparison (->entry "http://abc.com" "abc" "dfsdf" "engine") )
        )






