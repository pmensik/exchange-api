(ns exchange.ews.folders
  (:require [exchange.ews.authentication :refer [service-instance]])
  (:import (microsoft.exchange.webservices.data.core.service.folder Folder)
           (microsoft.exchange.webservices.data.core.enumeration.property WellKnownFolderName)
           (microsoft.exchange.webservices.data.property.complex FolderId)))

(defmulti get-folder
  "Returns folder either by provided id (of String type) or for by WellKnownFolderName enum value"
  (fn [value]
    (condp instance? value
      String :folder-id
      WellKnownFolderName :name)))

(defmethod get-folder :folder-id
  [folder-id]
  (Folder/bind @service-instance (FolderId. folder-id)))

(defmethod get-folder :name
  [known-name]
  (Folder/bind @service-instance known-name))

(defn get-inbox
  "Returns inbox folder"
  []
  (get-folder WellKnownFolderName/Inbox))

(defn- new-folder
  "Creates new folder"
  [folder-name parent]
  (doto (Folder. @service-instance)
    (.setDisplayName folder-name)
    (.save parent)))

(defmulti create-folder
  "Creates folder either by provided id (of String type) or for by WellKnownFolderName enum value"
  (fn [folder-name value]
    (condp instance? value
      String :folder-id
      WellKnownFolderName :name)))

(defmethod create-folder :folder-id
  [folder-name parent-folder-id]
  (new-folder folder-name parent-folder-id))

(defmethod create-folder :name
  [folder-name parent-name]
  (new-folder folder-name parent-name))
