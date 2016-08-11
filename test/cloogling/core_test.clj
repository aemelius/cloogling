(ns cloogling.core-test
  (:require [cloogling.core :refer :all]
            [expectations :refer :all]))

;; starting with parsing Bing-style results, with non-edge cases

;; setup: load a standard json response from Bing, with 10 entries
(def miles_davis_data (slurp "test/cloogling/miles_davis_bing.json"))

;; Counting entries in standard test data
(expect 10 (count (get-entries miles_davis_data)))

;; test on the first entry's url
(expect "https://en.wikipedia.org/wiki/Miles_Davis"
        (-> miles_davis_data
            get-entries
            first
            (:url)
            )
        )

;; test on the first entry's title
(expect "Miles Davis - Wikipedia, the free encyclopedia"
        (-> miles_davis_data
            get-entries
            first
            (:title))
        )

;; test on the first entry's snippet
(expect "Miles Dewey Davis III (May 26, 1926 – September 28, 1991) was an American jazz trumpeter, bandleader, and composer. He is among the most influential and acclaimed ..."
        (-> miles_davis_data
            get-entries
            first
            (:snippet))
        )

;; now some slightly edgier cases

;; if the data is not a valid json, I expect an empty list to be returned
(expect [] (get-entries "this is not a valid bing like json"))

;; if the data is valid json, but is in a different structure than what I expect from Bing, I will return an empty list
(expect [] (get-entries (slurp "test/cloogling/bad_data_bing_d_or_results.json")))

