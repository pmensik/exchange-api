(defproject pmensik/ews-clojure "0.1.0"
  :description "Utility library for accessing Microsoft Exchange"
  :url "https://github.com/pmensik/ews-clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "Same as Clojure"}
  :output-path "codox"

  :plugins [[lein-codox "0.10.4"]]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-time "0.14.4"]
                 [environ "1.1.0"]
                 [com.microsoft.ews-java-api/ews-java-api "2.0"]
                 [com.taoensso/timbre "4.10.0"]])
