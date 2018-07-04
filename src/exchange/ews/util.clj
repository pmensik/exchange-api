(ns exchange.ews.util
  (:require [exchange.ews.authentication :refer [service-instance]])
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
   (.loadPropertiesForItems @service-instance results property-set)))

(defn enum-id-cond
  "Conditions used to determine which multimethod to call with for correct type"
  [value]
  (condp instance? value
    String :folder-id
    WellKnownFolderName :name))
