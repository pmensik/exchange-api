(ns pdf.utils.form
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [pdf.utils.utils :as util])
  (:import (java.io File)
           (org.apache.pdfbox.cos COSName)
           (org.apache.pdfbox.pdmodel PDResources)
           (org.apache.pdfbox.pdmodel.font PDType0Font)
           (org.apache.pdfbox.pdmodel.interactive.form PDTextField)))

(def ^{:doc "Relative path to the GentiumPlus font"} font-path 
  "templates/GentiumPlus-R.ttf")

(defn- get-resources-with-font
  "Returns PDResources object with loaded GentiumPlus font"
  [doc]
  (let [font-file (util/get-file-template-path font-path)]
    (doto (PDResources.)
      (.add (PDType0Font/load doc font-file)))))

(defn- set-field
  "Sets single field to the form"
  [acro-form field-name field-value]
  (let [field (.getField acro-form field-name)
        dictionary (.getCOSObject field)]
    (when (instance? PDTextField field)
      (.setString dictionary COSName/DA "/F1 10 Tf 0 g") ;Sets GentiumPlus font
      (.setValue field field-value))))

(defn set-fields
  "Accepts map of fields and their values to be set. All fields are set with GentiumPlus font which supports national characters."
  [input-pdf output-pdf fields]
  (let [acro-form (-> (.getDocumentCatalog input-pdf)
                      .getAcroForm)]
    (.setDefaultResources acro-form (get-resources-with-font input-pdf))
    (run! #(set-field acro-form (first %) (second %)) fields)
    (.save input-pdf output-pdf)
    (.close input-pdf)))
