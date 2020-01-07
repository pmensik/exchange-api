(ns exchange.api.folders
  (:require [exchange.api.authentication :refer [service-instance]]
            [exchange.api.util :refer [enum-id-cond]])
  (:import (microsoft.exchange.webservices.data.core.service.folder Folder)
           (microsoft.exchange.webservices.data.core.enumeration.property WellKnownFolderName)
           (microsoft.exchange.webservices.data.property.complex FolderId)
           (microsoft.exchange.webservices.data.search FolderView)))

(defmulti get-folder
  "Returns folder either by provided id (of String type) or for by WellKnownFolderName enum value"
  enum-id-cond)

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

(defn- find-childs
  "Returns all childs of folder"
  [value]
  (->> (.findFolders @service-instance value (FolderView. Integer/MAX_VALUE))
       (.getFolders)))

(defmulti get-child-folders
  "Returns child folders either by provided id (of String type) or for by WellKnownFolderName enum value"
  enum-id-cond)

(defmethod get-child-folders :folder-id
  [folder-id]
  (find-childs (FolderId. folder-id)))

(defmethod get-child-folders :name
  [known-name]
  (find-childs known-name))

(defn- new-folder
  "Creates new folder"
  [folder-name parent]
  (doto (Folder. @service-instance)
    (.setDisplayName folder-name)
    (.save parent)))

(defmulti create-folder
  "Creates folder either by provided id (of String type) or for by WellKnownFolderName enum value"
  enum-id-cond)

(defmethod create-folder :folder-id
  [folder-name parent-folder-id]
  (new-folder folder-name parent-folder-id))

(defmethod create-folder :name
  [folder-name parent-name]
  (new-folder folder-name parent-name))
