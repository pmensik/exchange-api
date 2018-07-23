(ns exchange.ews.search
  (:require [clojure.string :as str]
            [exchange.ews.authentication :refer [service-instance]]
            [exchange.ews.util :refer [load-property-set default-property-set do-while]])
  (:import (clojure.lang Reflector)
           (microsoft.exchange.webservices.data.core.enumeration.property WellKnownFolderName)
           (microsoft.exchange.webservices.data.core.enumeration.search LogicalOperator)
           (microsoft.exchange.webservices.data.core.service.schema EmailMessageSchema ItemSchema)
           (microsoft.exchange.webservices.data.property.complex MessageBody)
           (microsoft.exchange.webservices.data.search ItemView)
           (microsoft.exchange.webservices.data.search.filter SearchFilter
                                                              SearchFilter$Exists
                                                              SearchFilter$SearchFilterCollection
                                                              SearchFilter$ContainsSubstring
                                                              SearchFilter$IsEqualTo
                                                              SearchFilter$IsGreaterThan
                                                              SearchFilter$IsGreaterThanOrEqualTo
                                                              SearchFilter$IsLessThan
                                                              SearchFilter$IsLessThanOrEqualTo
                                                              SearchFilter$IsNotEqualTo)))

(def ^{:doc "Search filters available in EWS API"} search-filters
  {:contains-substring SearchFilter$ContainsSubstring
   :exists SearchFilter$Exists
   :is-equal SearchFilter$IsEqualTo
   :not-equal SearchFilter$IsNotEqualTo
   :is-greater-than SearchFilter$IsGreaterThan
   :is-greater-or-equal-than SearchFilter$IsGreaterThanOrEqualTo
   :is-less-than SearchFilter$IsLessThan
   :is-less-or-equal-than SearchFilter$IsLessThanOrEqualTo})

(def ^{:doc "Logical operators available for filtering"} operators
  {:or LogicalOperator/Or
   :and LogicalOperator/And})

(defn transform-search-result
  "Transforms search result into vector of Clojure maps"
  [items]
  (map #(hash-map :id (.getUniqueId (.getId %))
                  :subject (.getSubject %)
                  :body (-> (.getBody %)
                            MessageBody/getStringFromMessageBody)
                  :date-received (.getDateTimeReceived %)
                  :importance (-> (.getImportance %)
                                  str/lower-case
                                  keyword)
                  :categories (-> (.getCategories %)
                                  (.getIterator)
                                  iterator-seq)) items))

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

(defn create-filter-collection
  "Creates filter collection, operator value can be :and or :or. Filters should be seq of filters created via
  create-search-filter function"
  [operator ^Iterable filters]
  {:pre? [(contains? operators operator)]}
  (SearchFilter$SearchFilterCollection. (get operators operator) filters))

(defn create-search-filter
  "Filter type has to be one of filters defined in search-filters map. Search field has to be value from either ItemSchema
  or EmailMessageSchema enum. Returns instance SearchFilter implementation"
  [filter-type search-field search-value]
  {:pre [(contains? search-filters filter-type)]}
  (let [ews-filter (get search-filters filter-type)]
    (Reflector/invokeConstructor ews-filter (object-array [search-field search-value]))))

(defn get-items-with-filter
  "Search for items with filters created by `create-search-filter` and `create-filter-collection` functions. You can optionally
  specify folder to search in and result limit"
  ([filters]
   (get-items-with-filter filters WellKnownFolderName/Inbox Integer/MAX_VALUE))
  ([filters folder]
   (get-items-with-filter filters folder Integer/MAX_VALUE))
  ([filters folder limit]
   (let [view (ItemView. limit)
         result (.findItems @service-instance folder filters view)]
     (load-property-set result)
     (.getItems result))))
