(ns exchange.api.search
  (:require [clojure.string :as str]
            [exchange.api.authentication :refer [service-instance]]
            [exchange.api.util :refer [load-property-set default-property-set do-while]])
  (:import (clojure.lang Reflector)
           (microsoft.exchange.webservices.data.core.enumeration.property WellKnownFolderName)
           (microsoft.exchange.webservices.data.core.enumeration.search LogicalOperator)
           (microsoft.exchange.webservices.data.core.service.schema EmailMessageSchema ItemSchema)
           (microsoft.exchange.webservices.data.core.service.item EmailMessage)
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
                  :from (when (instance? EmailMessage %)
                          (-> (cast EmailMessage %)
                              .getFrom .getAddress))
                  :date-received (.getDateTimeReceived %)
                  :importance (-> (.getImportance %)
                                  str/lower-case
                                  keyword)
                  :categories (-> (.getCategories %)
                                  (.getIterator)
                                  iterator-seq)) items))

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
  specify folder to search in, result limit and offset. Functions called without any arguments will return all items in
  Inbox folder"
  ([]
   (get-items-with-filter nil))
  ([filters]
   (get-items-with-filter filters Integer/MAX_VALUE))
  ([filters limit]
   (get-items-with-filter filters limit 0))
  ([filters limit offset]
   (get-items-with-filter filters limit offset WellKnownFolderName/Inbox))
  ([filters limit offset folder]
   (let [view (ItemView. limit offset)
         result (.findItems @service-instance folder filters view)]
     (load-property-set result)
     (.getItems result))))
