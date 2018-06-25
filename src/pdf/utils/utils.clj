(ns pdf.utils.utils
  (:require [clojure.java.io :as io])
  (:import (java.io InputStream
                    File
                    FileInputStream)
           (javax.activation DataSource)
           (org.apache.pdfbox.multipdf PDFMergerUtility)
           (org.apache.pdfbox.preflight.parser PreflightParser)))

(def ^:const pdf-content-type "application/pdf")

(defn merge-pdfs
  "Merge multiple PDFs into output file. Input parameter is expected as a vector of file paths in string or InputStreams"
  [input output]
  (let [merger (PDFMergerUtility.)]
    (doseq [f input]
      (let [stream (if (instance? InputStream f)
                     f
                     (FileInputStream. (File. f)))]
        (.addSource merger stream)))
    (.setDestinationFileName merger output)
    (.mergeDocuments merger)))

(defn pdf-data-source
  "Creates PDF DataSource used by a PDFBox parser"
  [input]
  (reify javax.activation.DataSource

    (getContentType [_]
      "application/pdf")

    (getName [_]
      "pdf-data-source")

    (getOutputStream [_]
      nil)

    (getInputStream [_]
      input)))

(defn get-pdf-template
  "Returns PDF template parsed from file path"
  [path]
  (let [parser (-> (io/resource path)
                   io/input-stream
                   pdf-data-source
                   PreflightParser.)]
    (.parse parser)
    (.getPreflightDocument parser)))

(defn get-file-template-path
  "Returns full path of PDF template"
  [file]
  (when file
    (io/input-stream (io/resource file))))

(defn create-file-map
  "Creates a map for save-file function which saves files to the DB"
  [file-path]
  (let [file (io/file file-path)]
    {:content-type pdf-content-type
     :size (.length file)
     :tempfile file
     :filename (.getName file)}))
