(ns exchange.api.util
  (:require [exchange.api.authentication :refer [service-instance]])
  (:import (microsoft.exchange.webservices.data.core PropertySet)
           (microsoft.exchange.webservices.data.core.enumeration.property BasePropertySet
                                                                          BodyType
                                                                          WellKnownFolderName)))

(def ^{:doc "Default property set used for fetching data"} default-property-set
  (doto (PropertySet. (BasePropertySet/FirstClassProperties))
    (.setRequestedBodyType (BodyType/Text))))

(defn load-property-set
  "Loads property used during fetching for target find item result"
  ([results]
   (load-property-set default-property-set results))
  ([property-set results]
   (when-not (empty? (.getItems results))
     (.loadPropertiesForItems @service-instance results property-set))))

(defn enum-id-cond
  "Conditions used to determine which multimethod to call with for correct type"
  [value]
  (condp instance? value
    String :folder-id
    WellKnownFolderName :name))

(defmacro do-while
  "Provides do while loop for list-all-items"
  [test & body]
  `(loop []
     ~@body
     (when ~test
       (recur))))
