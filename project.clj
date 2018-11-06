(defproject com.temify/ews-clojure-api "0.0.5"
  :description "Utility library for accessing Microsoft Exchange"
  :url "https://wwww.bizziapp.com"

  :plugins [[lein-codox "0.10.4"]]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-time "0.14.4"]
                 [environ "1.1.0"]
                 [com.microsoft.ews-java-api/ews-java-api "2.0"]
                 [com.taoensso/timbre "4.10.0"]])
