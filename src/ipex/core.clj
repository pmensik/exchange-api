(ns ipex.core
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [taoensso.timbre :as timbre]
            [cheshire.core :as json])
  (:import clojure.lang.ExceptionInfo))

(def ^{:doc "Configuration and credentials for the IPEX API"} api-config
  {:url (get :ipex-url env "https://vh1107.ipex.cz/api")
   :key (get :ipex-key env "be3c4902eb51baf3f1b97515")
   :secret (get :ipex-secret env "cf839e7e0be0/4b1699d0/1dbb6c3a22fc")})

(def ^{:doc "IPEX HTTP client configuration"} ipex-http-client-config
  {:basic-auth [(:key api-config) (:secret api-config)]
   :as :json
   :content-type :json
   :accept :json})

(defn check-ipex-api-connection []
  (and
   (not-empty (api-config :url))
   (not-empty (api-config :key))
   (not-empty (api-config :secret))))

(defn- get-timeouts
  [conn socket]
  {:conn-timeout conn
   :socket-timeout socket})

(defn make-new-call
  "Issues new call to a number with provided user email"
  [number user-email]
  (when (check-ipex-api-connection)
    (try
      (.start (Thread. (fn []
                         (:body (client/post (str (:url api-config) "/calls?startTime=2001-01-01&dstNumber=" number)
                                             (merge ipex-http-client-config
                                                    (get-timeouts 30000 30000)
                                                    {:body (json/generate-string
                                                            {:identityType :email
                                                             :identityValue user-email
                                                             :to number})}))))))
      (catch ExceptionInfo e (timbre/error "Problem connecting to the IPEX" e)))
    true))

(defn get-calls-history
  "Returns call history for given number"
  [number]
  (try
    (:body (client/get (str (:url api-config) "/calls?startTime=2001-01-01&dstNumber=" number)
                       (merge ipex-http-client-config
                              (get-timeouts 15000 15000))))
    (catch ExceptionInfo e (timbre/error "Problem connecting to the IPEX" e))))

(defn get-ipex-users 
  "Returns al IPEX users for provided credentials"
  []
  (try
    (:body (client/get (str (api-config :url) "/members")
                       (merge ipex-http-client-config
                              (get-timeouts 15000 15000))))
    (catch ExceptionInfo e (timbre/error "Problem connecting to the IPEX" e))))
