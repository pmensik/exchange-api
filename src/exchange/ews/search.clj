(ns exchange.ews.search
  (:require [exchange.ews.authentication :refer [service-instance]]
            [exchange.ews.util :refer [load-property-set]])
  (:import (microsoft.exchange.webservices.data.core PropertySet)
           (microsoft.exchange.webservices.data.core.enumeration.property WellKnownFolderName)
           (microsoft.exchange.webservices.data.core.service.schema ItemSchema)
           (microsoft.exchange.webservices.data.property.complex MessageBody)
           (microsoft.exchange.webservices.data.search ItemView)))

(def schema-keyword-mapping
  {:subject ItemSchema/})

(defmacro do-while
  "Provides do while loop for list-all-items"
  [test & body]
  `(loop []
     ~@body
     (when ~test
       (recur))))

(defn transform-search-result
  "Transforms search result into vector of Clojure maps"
  [items]
  (map #(hash-map :id (.getUniqueId (.getId %))
                  :subject (.getSubject %)
                  :body (-> (.getBody %)
                            MessageBody/getStringFromMessageBody)) items))

(defn list-paginated-items
  "Get page of items defined by offset (defaults to 0). Folder id defaults to Inbox"
  ([limit]
   (list-paginated-items WellKnownFolderName/Inbox limit 0))
  ([limit offset]
   (list-paginated-items WellKnownFolderName/Inbox limit offset))
  ([folder-id limit offset]
   (let [view (ItemView. limit offset)
         result (.findItems @service-instance folder-id view)]
     (load-property-set result)
     (.getItems result))))

(defn list-all-items
  "List all items in folder without pagination. Folder id can be both string id or enumeration of well know name,
  defaults to Inbox"
  ([]
   (list-all-items WellKnownFolderName/Inbox))
  ([folder-id]
   (let [view (ItemView. Integer/MAX_VALUE)
         result (.findItems @service-instance folder-id view)]
     (load-property-set result)
     (.getItems result))))

(defn)
