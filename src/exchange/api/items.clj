(ns exchange.api.items
  (:require [exchange.api.authentication :refer [service-instance]])
  (:import (microsoft.exchange.webservices.data.core.service.item Item)
           (microsoft.exchange.webservices.data.core.enumeration.property Importance)
           (microsoft.exchange.webservices.data.core.enumeration.service ConflictResolutionMode)
           (microsoft.exchange.webservices.data.property.complex ItemId)))

(def ^{:doc "Exhange importance set"} importance-set
  {:low (Importance/Low) :normal (Importance/Normal) :high (Importance/High)})

(defn set-item-importance
  "Sets item importance. Default importance is :normal"
  [id importance]
  {:pre [(contains? importance-set importance)]}
  (let [item (Item/bind @service-instance (ItemId/getItemIdFromString id))]
    (.setImportance item (importance importance-set))
    (.update item ConflictResolutionMode/AutoResolve)))

(defn add-category
  "Adds category to an item"
  [id category]
  (let [item (Item/bind @service-instance (ItemId/getItemIdFromString id))]
    (-> (.getCategories item)
        (.add category))
    (.update item ConflictResolutionMode/AutoResolve)))

(defn add-categories
  "Adds categories provided as a vector of strings to an item"
  [id categories]
  (let [item (Item/bind @service-instance (ItemId/getItemIdFromString id))
        item-cats (.getCategories item)]
    (run! #(.add item-cats %) categories)
    (.update item ConflictResolutionMode/AutoResolve)))

(defn remove-category
  "Removes category provided as a string from an item"
  [id category]
  (let [item (Item/bind @service-instance (ItemId/getItemIdFromString id))]
    (-> (.getCategories item)
        (.remove category))
    (.update item ConflictResolutionMode/AutoResolve)))

(defn remove-categories
  "Removes categories provided as a vector of strings from an item"
  [id categories]
  (let [item (Item/bind @service-instance (ItemId/getItemIdFromString id))
        item-cats (.getCategories item)]
    (run! #(.remove item-cats %) categories)
    (.update item ConflictResolutionMode/AutoResolve)))
