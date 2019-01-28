(ns exchange.ews.calendar
  (:require [clj-time.core :as clj-time]
            [clojure.string :as str]
            [exchange.ews.authentication :refer [service-instance]]
            [exchange.ews.util :refer [load-property-set]])
  (:import (java.util Date)
           (microsoft.exchange.webservices.data.core.service.folder CalendarFolder)
           (microsoft.exchange.webservices.data.core.enumeration.service DeleteMode)
           (microsoft.exchange.webservices.data.core.service.item Appointment)
           (microsoft.exchange.webservices.data.core.enumeration.property WellKnownFolderName)
           (microsoft.exchange.webservices.data.property.complex MessageBody
                                                                 StringList
                                                                 ItemId)
           (microsoft.exchange.webservices.data.search CalendarView)))

(defn transform-appointments
  "Transforms appointments result into vector of Clojure maps"
  [appointments]
  (map #(hash-map :id (.getUniqueId (.getId %))
                  :subject (.getSubject %)
                  :body (-> (.getBody %)
                            MessageBody/getStringFromMessageBody)
                  :importance (-> (.getImportance %)
                                  str/lower-case
                                  keyword)
                  :categories (-> (.getCategories %)
                                  (.getIterator)
                                  iterator-seq)
                  :start-date (.getStart %)
                  :end-date (.getEnd %)
                  :duration (.toString (.getDuration %))) appointments))

(defn get-all-appointments
  "Returns all appointments between range of dates. Defaults to beginning and end of the current month"
  ([]
   (let [now (clj-time/now)]
     (get-all-appointments (.toDate (clj-time/first-day-of-the-month now))
                           (.toDate (clj-time/last-day-of-the-month now)))))
  ([from to]
   (let [calendar (CalendarFolder/bind @service-instance WellKnownFolderName/Calendar)
         view (CalendarView. from to)
         results (.findAppointments calendar view)]
     (load-property-set results)
     (.getItems results))))

(defn create-appointment
  "Creates new appointment and saves it"
  [subject ^Date from ^Date to & {:keys [body location categories]}]
  (doto (doto (Appointment. @service-instance)
        (.setSubject subject)
        (.setStart from)
        (.setEnd to)
        (.setBody (MessageBody/getMessageBodyFromText body))
        (.setLocation location)
        (.setCategories (StringList. categories)))
      (.save)))

(defn delete-appointment
  "Deletes appointment - default mode defaults to HardDelete. Otherwise accepts value from DeleteMode enum"
  ([id]
   (delete-appointment id DeleteMode/HardDelete))
  ([id delete-mode]
   (-> (Appointment/bind @service-instance (ItemId/getItemIdFromString id))
       (.delete delete-mode))))
