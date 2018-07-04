(ns exchange.ews.search
  (:require [exchange.ews.authentication :refer [service-instance]])
  (:import (microsoft.exchange.webservices.data.core.service.folder Folder)
           (microsoft.exchange.webservices.data.core.service.item Item)
           (microsoft.exchange.webservices.data.core.enumeration.property BasePropertySet
                                                                          BodyType
                                                                          WellKnownFolderName)
           (microsoft.exchange.webservices.data.property.complex FolderId)
           (microsoft.exchange.webservices.data.search FindItemsResults
                                                       ItemView)))

(defn list-paginated-items
  "Get page of items defined by offset (defaults to 0). Folder id defaults to Inbox"
  ([limit]
   (list-paginated-items WellKnownFolderName/Inbox limit 0))
  ([limit offset]
   (list-paginated-items WellKnownFolderName/Inbox limit offset))
  ([folder-id limit offset]
   (let [view (ItemView. limit offset)]
     (.getItems (.findItems @service-instance folder-id view)))))

(defn list-all-items
  "List all items in folder without pagination. Folder id can be both string id or enumeration of well know name,
  defaults to Inbox"
  ([]
   (list-all-items WellKnownFolderName/Inbox))
  ([folder-id]
   (let [page-size (atom 50)
         results (atom nil)
         view (ItemView. @page-size)
         items (transient [])]
     (conj! items (reset! results (.findItems @service-instance folder-id view)))
     (while (.isMoreAvailable @results)
       (.setOffset view (swap! page-size inc))
       (conj! items (reset! results (.findItems @service-instance folder-id view))))
     (persistent! items))))
