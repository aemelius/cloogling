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
(expect "https://api.datamarket.azure.com/Bing/Search/Web?$top=10&Query=Miles%20Davis&$format=json"
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
(let [ google [(->entry "a" "a" "a") (->entry "b" "b" "b") (->entry "d" "d" "d")]
       bing [(->entry "a" "a" "a") (->entry "b" "b" "b") (->entry "c" "c" "c")]]

   (expect [ "a" "b" "d" "c"]
           (map (fn [x] (:url x ))(get-aggregated-result google bing))
    )

)










