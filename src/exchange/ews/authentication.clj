(ns exchange.ews.authentication
  (:require [clojure.string :as str]
            [environ.core :refer [env]])
  (:import (java.net URI)
           (microsoft.exchange.webservices.data.autodiscover IAutodiscoverRedirectionUrl)
           (microsoft.exchange.webservices.data.core ExchangeService)
           (microsoft.exchange.webservices.data.core.enumeration.misc ExchangeVersion
                                                                      ConnectingIdType)
           (microsoft.exchange.webservices.data.credential WebCredentials)
           (microsoft.exchange.webservices.data.misc ImpersonatedUserId)))

(def ^{:doc "Exchange version supported by EWS"} exchange-versions
  {:ex-2007-SP1 "Exchange2007_SP1" :ex-2010 "Exchange2010" :ex-2010-SP1 "Exchange2010_SP1" :ex-2010-SP2 "Exchange2010_SP2"})

(def ^{:doc "Default version to connect to"} default-version :ex-2010-SP2)

(def ^{:doc "Impersonation types supported by API"} impersonation-types
  {:smtp "SmtpAddress" :sid "SID" :principal "PrincipalName"})

(def ^{:doc "Callback class called after automatic URL autodiscovery"} autodiscover-callback
  (reify IAutodiscoverRedirectionUrl
    (autodiscoverRedirectionUrlValidationCallback [this redirectionUrl]
      (-> redirectionUrl
          str/lower-case
          (str/starts-with? "https://")))))

(def ^{:doc "Atom which holds instance of ExchangeService class used for API calls"} service-instance (atom nil))

(defn connect-with-url
  "Connect to Exchange API via URL - user, password and url parameters has to be provided"
  ([]
   (connect-with-url
    (:exchange-user env) (:exchange-pass env) (:exchange-url env)
    (or (:exchange-version env) default-version)))
  ([user password url]
   (connect-with-url user password url :ex-2010-SP2))
  ([user password url version]
   (let [service (ExchangeService. (ExchangeVersion/valueOf (version exchange-versions)))]
     (doto service
       (.setCredentials (WebCredentials. user password))
       (.setUrl (URI. url)))
     (reset! service-instance service))))

(defn connect-with-autodiscover
  "Connect to Exchange API via autodiscover mode - user, password parameters has to be provided"
  ([]
   (connect-with-autodiscover
    (:exchange-user env) (:exchange-pass env) (or (:exchange-version env) default-version)))
  ([user password]
   (connect-with-url user password :ex-2010-SP2))
  ([user password version]
   (let [service (ExchangeService. (ExchangeVersion/valueOf (version exchange-versions)))]
     (doto service
       (.setCredentials (WebCredentials. user password))
       (.autodiscoverUrl user))
     (reset! service-instance service))))

(defn impersonate-user
  "Impersonates target user via "
  ([email-address]
   (impersonate-user email-address :smtp))
  ([user-id impersonation-type]
   {:pre [(contains? impersonation-types impersonation-type)]}
   (let [service @service-instance
         imp-type (ConnectingIdType/valueOf (impersonation-type impersonation-types))]
     (->> (ImpersonatedUserId. imp-type user-id)
          (.setImpersonatedUserId service))
     (reset! service-instance service))))
