(ns ipex.core
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [taoensso.timbre :as timbre]
            [cheshire.core :as json]))

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
  (try (:body (client/post (str (:url api-config) "/auth/token") (merge ipex-http-client-config
                                                                        {:form-params {:identityType "email"
                                                                                       :identityValue user-email}})))
       (catch Exception ex
         (throw (ex-info "Problem connecting to the IPEX" {:cause :ipex-api} ex)))))

(defn make-call
  "Issues new call to a number authenticated with user email"
  [number user-email]
  (try (:body (client/post (str (:url api-config) "/calls")
                           (merge ipex-http-client-config
                                  call-timeout-settings
                                  {:form-params {:identityType "email"
                                                 :identityValue user-email
                                                 :to number}})))
       (catch Exception ex
         (throw (ex-info "Problem connecting to the IPEX" {:cause :ipex-api} ex)))))

(defn get-call-history
  "Returns call history for given number"
  [number]
  (try (:body (client/get (str (:url api-config) "/calls")
                          (assoc ipex-http-client-config
                                 :query-params {:dstNumber number})))
       (catch Exception ex
         (throw (ex-info "Problem connecting to the IPEX" {:cause :ipex-api} ex)))))

(defn get-ipex-users
  "Returns al IPEX users for provided credentials"
  []
  (try (:body (client/get (str (api-config :url) "/members")
                          ipex-http-client-config))
       (catch Exception ex
         (throw (ex-info "Problem connecting to the IPEX" {:cause :ipex-api} ex)))))

(defn log-out-ipex-user
  "Logs out IPEX user from the queue"
  [username]
  (try (:body (client/put (str (:url api-config) "/members/" username "/logout")
                          ipex-http-client-config))
       (catch Exception ex
         (throw (ex-info "Problem connecting to the IPEX" {:cause :ipex-api} ex)))))

(defn log-in-ipex-user
  "Logs in IPEX user to the queue"
  [username]
  (try (:body (client/put (str (:url api-config) "/members/" username "/login")
                          ipex-http-client-config))
       (catch Exception ex
         (throw (ex-info "Problem connecting to the IPEX" {:cause :ipex-api} ex)))))
