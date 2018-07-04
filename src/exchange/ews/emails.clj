(ns exchange.ews.emails
  (:require [exchange.ews.authentication :refer [service-instance]])
  (:import (microsoft.exchange.webservices.data.core.service.item EmailMessage)
           (microsoft.exchange.webservices.data.property.complex MessageBody)))

(defn send-email
  "Send email via Exchange API"
  [to subject body]
  (let [message (doto (EmailMessage. @service-instance)
                  (.setSubject subject)
                  (.setBody (MessageBody/getMessageBodyFromText body)))]
    (.add (.getToRecipients message) to)
    (.send message)))
