(defproject cloogling "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [expectations "1.4.52"]
                 [org.clojure/data.json "0.2.6"]
                 [com.cemerick/url "0.1.1"]
                 [clj-http "2.2.0"]
                 [org.clojure/tools.cli "0.3.5"]]
  :plugins [[lein-autoexpect "1.0"]]
  :main cloogling.core)
