(ns gcs.core
  (:require [clojure.java.io :as io])
  (:import [com.google.cloud.storage BlobId BlobInfo BucketInfo Storage Storage$BlobTargetOption Storage$BlobWriteOption StorageOptions]
           [com.google.auth.oauth2 ServiceAccountCredentials]))

(def service (atom nil))

(defn empty-varargs [klass]
  (into-array klass []))

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
  (let [blob-info (-> (.build (BlobInfo/newBuilder bucket blob-id)))]
    (.create @service
     blob-info
     (if (string? content) (.getBytes content) content)
     (empty-varargs (if (string? content) Storage$BlobTargetOption Storage$BlobWriteOption)))))
