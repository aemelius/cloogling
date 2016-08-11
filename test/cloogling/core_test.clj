(ns cloogling.core-test
  (:require [cloogling.core :refer :all]
            [expectations :refer :all]))

(expect 1 1)

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

(expect [] (get-entries "this is not a valid bing like json"))
