(ns cloogling.core-test
  (:require [cloogling.core :refer :all]
            [expectations :refer :all]))

;; starting with parsing Bing-style results, with non-edge cases

;; setup: load a standard json response from Bing, with 10 entries
(def miles_davis_data (slurp "test/cloogling/miles_davis_bing.json"))

;; Counting entries in standard test data
(expect 10 (count (get-entries-generic miles_davis_data b create-entry)))

;; test on the first entry's url
(expect "https://en.wikipedia.org/wiki/Miles_Davis"
        (-> miles_davis_data
            (get-entries-generic b  create-entry)
            first
            (:url)
            )
        )

;; test on the first entry's title
(expect "Miles Davis - Wikipedia, the free encyclopedia"
        (-> miles_davis_data
            (get-entries-generic b  create-entry)
            first
            (:title))
        )

;; test on the first entry's snippet
(expect "Miles Dewey Davis III (May 26, 1926 – September 28, 1991) was an American jazz trumpeter, bandleader, and composer. He is among the most influential and acclaimed ..."
        (-> miles_davis_data
            (get-entries-generic b  create-entry)
            first
            (:snippet))
        )

;; now some slightly edgier cases for Bing

;; if the data is not a valid json, I expect an empty list to be returned
(expect [] (get-entries-generic "this is not a valid bing like json" b  create-entry) )

;; if the data is valid json, but is in a different structure than what I expect from Bing, I will return an empty list
(expect [] (get-entries-generic (slurp "test/cloogling/bad_data_bing_d_or_results.json") b  create-entry))

;; valid json, but does not contain a list of entries
(expect [] (get-entries-generic (slurp "test/cloogling/bad_data_bing_2.json") b  create-entry ))

;; valid json, it contains a list of entries in the right place, but the keys are not what expected from Bing
(expect [] (get-entries-generic (slurp "test/cloogling/bad_data_bing_3.json") b  create-entry))


;; now some Google stuff

;; setup: load a standard json response from Google, with 10 entries
(def miles_davis_data_google (slurp "test/cloogling/miles_davis_google.json"))

;; Counting entries in standard test data
(expect 10 (count (get-entries-generic miles_davis_data_google g  create-entry-google)))

;; test on the first entry's url
(expect "https://en.wikipedia.org/wiki/Miles_Davis"
        (-> miles_davis_data_google
            (get-entries-generic g  create-entry-google)
            first
            (:url)
            )
        )


;; test on the first entry's title
(expect "Miles Davis - Wikipedia, the free encyclopedia"
        (-> miles_davis_data_google
           (get-entries-generic g create-entry-google)
            first
            (:title))
        )

;; test on the first entry's snippet
(expect "Miles Dewey Davis III (May 26, 1926 – September 28, 1991) was an American \njazz trumpeter, bandleader, and composer. He is among the most influential and\n ..."
        (-> miles_davis_data_google
            (get-entries-generic g create-entry-google)
            first
            (:snippet))
        )

