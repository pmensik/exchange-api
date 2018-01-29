(ns ipex.core
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [taoensso.timbre :as timbre]
            [cheshire.core :as json])
  (:import (clojure.lang ExceptionInfo)))

(def ^{:doc "Configuration and credentials for the IPEX API"} api-config
  {:url (str "https://" (:ipex-url env) "/api")
   :key (:ipex-api-key env)
   :secret (:ipex-api-secret env)})

(def ^{:doc "IPEX HTTP client configuration"} ipex-http-client-config
  {:basic-auth [(:key api-config) (:secret api-config)]
   :as :json
   :content-type :json
   :accept :json})

(def ^{:doc "Call timeout"} call-timeout-settings
  {:conn-timeout 300000
   :socket-timeout 30000})

(defn get-token
  "Returns auth token for further API communication. Token expires in 60 minutes"
  [user-email]
  (:body (client/post (str (:url api-config) "/auth/token")
                      (merge ipex-http-client-config
                             call-timeout-settings
                             {:form-params {:identityType "email"
                                            :identityValue user-email}}))))

(defn make-call
  "Issues new call to a number authenticated with user email"
  [number user-email]
  (:body (client/post (str (:url api-config) "/calls")
                      (merge ipex-http-client-config
                             call-timeout-settings
                             {:form-params {:identityType "login"
                                            :identityValue user-email
                                            :to number}}))))
       ; (catch ExceptionInfo ex
       ;   (ex-info "Problem connection to the IPEX" ex))))

(defn get-call-history
  "Returns call history for given number"
  [number]
  (try (:body (client/get (str (:url api-config) "/calls")
                          (assoc ipex-http-client-config
                                 :query-params {:dstNumber number
                                                :startTime "2001-01-01"})));TODO remove time
       (catch ExceptionInfo ex
         (ex-info "Problem connection to the IPEX" ex))))

(defn get-ipex-users
  "Returns al IPEX users for provided credentials"
  []
  (try (:body (client/get (str (api-config :url) "/members")
                          ipex-http-client-config))
       (catch ExceptionInfo ex
         (ex-info "Problem connection to the IPEX" ex))))
