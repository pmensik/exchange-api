(ns exchange.ews.streaming-notifications
  (:require [clojure.string :as str]
            [clojure.set :as clj-set]
            [exchange.ews.authentication :refer [service-instance]]
            [taoensso.timbre :as log])
  (:import (microsoft.exchange.webservices.data.core.enumeration.notification EventType)
           (microsoft.exchange.webservices.data.notification StreamingSubscription)
           (microsoft.exchange.webservices.data.notification StreamingSubscriptionConnection)
           (microsoft.exchange.webservices.data.notification StreamingSubscriptionConnection$INotificationEventDelegate)
           (microsoft.exchange.webservices.data.notification StreamingSubscriptionConnection$ISubscriptionErrorDelegate)
           (microsoft.exchange.webservices.data.property.complex FolderId)))

(def ^{:doc "Event types supported by EWS"} event-types-map
  (into {}
        (map #(hash-map (-> (.name %) str/lower-case keyword)
                        %) (EventType/values))))

(def ^{:doc "Default lifetime of notificator (maximum allowed by EWS API)"} default-lifetime 30)

(def ^{:doc "Default on disconnect function implementation - automatically opens the connection again"} on-disconnect
  (reify StreamingSubscriptionConnection$ISubscriptionErrorDelegate
    (subscriptionErrorDelegate [_ sender error-args]
      (try
        (.open sender)
        (log/info "Exchange connection reopened")
        (catch Exception ex
          (log/error "Error while reopening Exchange connection" ex))))))

(def ^{:doc "Default on event function implementation - just prints incomming id"} on-notification-event
  (reify StreamingSubscriptionConnection$INotificationEventDelegate
    (notificationEventDelegate [_ sender event-args]
      (run! (fn [event]
              (log/info "Event type" (.name (.getEventType event)))
              (log/info "Item id" (.getUniqueId (.getItemId event)))) (.getEvents event-args)))))

(defn create-streaming-notification-on-all-folders
  "Creates an instances of a streaming notificator for all folders"
  ([event-types on-event-fn]
   (create-streaming-notification-on-all-folders event-types on-event-fn on-disconnect default-lifetime))
  ([event-types on-event-fn on-disconnect-fn]
   (create-streaming-notification-on-all-folders event-types on-event-fn on-disconnect-fn default-lifetime))
  ([event-types on-event-fn on-disconnect-fn lifetime]
   {:pre [(clj-set/subset? event-types (into #{} (keys event-types-map)))
          (and (>= lifetime 1) (<= lifetime 30))]}
   (let [events (vals (select-keys event-types-map event-types))
         subscription (.subscribeToStreamingNotificationsOnAllFolders @service-instance (into-array events))
         conn (StreamingSubscriptionConnection. @service-instance lifetime)]
     (doto conn
       (.addSubscription subscription)
       (.addOnNotificationEvent on-event-fn)
       (.addOnDisconnect on-disconnect-fn)
       (.open)))))

(defn create-streaming-notification
  "Creates an instances of a streaming notificator specified folders"
  ([event-types folder-ids on-event-fn]
   (create-streaming-notification event-types folder-ids on-event-fn on-disconnect default-lifetime))
  ([event-types folder-ids on-event-fn on-disconnect-fn]
   (create-streaming-notification event-types folder-ids on-event-fn on-disconnect-fn default-lifetime))
  ([event-types folder-ids on-event-fn on-disconnect-fn lifetime]
   {:pre [(clj-set/subset? event-types (into #{} (keys event-types-map)))
          (every? #(instance? FolderId %) folder-ids)
          (and (>= lifetime 1) (<= lifetime 30))]}
   (let [events (vals (select-keys event-types-map event-types))
         subscription (.subscribeToStreamingNotifications @service-instance
                                                          folder-ids
                                                          (into-array events))
         conn (StreamingSubscriptionConnection. @service-instance lifetime)]
     (doto conn
       (.addSubscription subscription)
       (.addOnNotificationEvent on-event-fn)
       (.addOnDisconnect on-disconnect-fn)
       (.open)))))
