(ns exchange.ews.authentication
  (:require [clojure.string :as str])
  (:import (java.net URI)
           (microsoft.exchange.webservices.data.autodiscover IAutodiscoverRedirectionUrl)
           (microsoft.exchange.webservices.data.core ExchangeService)
           (microsoft.exchange.webservices.data.core.enumeration.misc ExchangeVersion
                                                                      ConnectingIdType)
           (microsoft.exchange.webservices.data.credential WebCredentials)
           (microsoft.exchange.webservices.data.misc ImpersonatedUserId)))

(def ^{:doc "Exchange version supported by EWS"} exchange-versions
  {:ex-2007-SP1 "Exchange2007_SP1" :ex-2010 "Exchange2010" :ex-2010-SP1 "Exchange2010_SP1" :ex-2010-SP2 "Exchange2010_SP2"})

(def ^{:doc "Impersonation types supported by API"} impersonation-types
  {:smtp "SmtpAddress" :sid "SID" :principal "PrincipalName"})

(def ^{:doc "Callback class called after automatic URL autodiscovery"} autodiscover-callback
  (reify IAutodiscoverRedirectionUrl
    (autodiscoverRedirectionUrlValidationCallback [this redirectionUrl]
      (-> redirectionUrl
          str/lower-case
          (str/starts-with? "https://")))))

(defn connect-to-exchange
  "Connects to Exchange and returns instance of ExchangeService"
  [& {:keys [user password url version]
      :or {version :ex-2010-SP2}}]
  {:pre [(contains? exchange-versions version)]}
  (doto (ExchangeService. (ExchangeVersion/valueOf (version exchange-versions)))
    (.setCredentials (WebCredentials. user password))
    (.setUrl (URI. url))))

(defn impersonate-user
  "Impersonates target user via "
  ([service email-address]
   (impersonate-user service email-address :smtp))
  ([service user-id impersonation-type]
   {:pre [contains? (impersonation-type impersonation-types)]}
   (->> (ImpersonatedUserId. (ConnectingIdType/valueOf (impersonation-type impersonation-types)) user-id)
        (.setImpersonatedUserId service))))
