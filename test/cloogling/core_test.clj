(ns cloogling.core-test
  (:require [cloogling.core :refer :all]
            [expectations :refer :all]))

(expect 1 1)

(def miles_davis_data (slurp "test/cloogling/miles_davis_bing.json"))

;; Parsing entries in bing json format to a list
(expect 10 (count (get-entries miles_davis_data)))
