(ns gcs.core
  (:require [clojure.java.io :as io])
  (:import [com.google.cloud.storage BlobId BlobInfo BucketInfo Storage Storage$BlobTargetOption Storage$BlobWriteOption StorageOptions]
           com.google.auth.oauth2.ServiceAccountCredentials
           java.util.zip.GZIPOutputStream))

(def service (atom nil))

(defn empty-varargs [klass]
  (into-array klass []))

(defn gzip-compress [content]
  (let [baos (java.io.ByteArrayOutputStream.)
        gzip (GZIPOutputStream. baos)]
    (.write gzip (.getBytes content "UTF-8"))
    (.close gzip)
    (.toByteArray baos)))

(defn initialize [project-id credentials-filename]
  (let [creds (ServiceAccountCredentials/fromStream (io/input-stream (io/resource credentials-filename)))
        new-service (-> (StorageOptions/newBuilder)
                        (.setProjectId project-id)
                        (.setCredentials creds)
                        (.build)
                        (.getService))]
    (reset! service new-service)
    true))

(defn get-blob [bucket blob-id]
  (.get @service (BlobId/of bucket blob-id)))

(defn create-blob [bucket blob-id content]
  (let [blob-info (.build (BlobInfo/newBuilder bucket blob-id))]
    (.create @service
             blob-info
             (if (string? content) (.getBytes content) content)
             (empty-varargs (if (string? content) Storage$BlobTargetOption Storage$BlobWriteOption)))))

(defn create-gzipped-blob [bucket blob-id content content-type]
  (let [blob-info (-> (BlobInfo/newBuilder bucket blob-id)
                      (.setContentEncoding "gzip")
                      (.setContentType content-type)
                      .build)
        gzipped-content (gzip-compress (if (string? content) content (slurp content)))]
    (.create @service
             blob-info
             gzipped-content
             (empty-varargs Storage$BlobTargetOption))))
