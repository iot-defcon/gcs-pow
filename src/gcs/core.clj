(ns gcs.core
  (:require [clojure.java.io :as io]
            [gcs.acl :as acl]
            [gcs.utils :as utils])
  (:import [com.google.cloud.storage Acl Acl$User Acl$Role BlobId BlobInfo BucketInfo Storage Storage$BlobTargetOption Storage$BlobWriteOption Storage$BucketTargetOption StorageOptions]
           com.google.auth.oauth2.ServiceAccountCredentials))

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

(defn get-object [bucket object-name]
  (.get @service (BlobId/of bucket object-name)))

(defn create-object [bucket object-name content]
  (let [blob-info (.build (BlobInfo/newBuilder bucket object-name))]
    (.create @service
             blob-info
             (if (string? content) (.getBytes content) content)
             (empty-varargs (if (string? content) Storage$BlobTargetOption Storage$BlobWriteOption)))))

(defn create-gzipped-object [bucket object-name content content-type]
  (let [blob-info (-> (BlobInfo/newBuilder bucket object-name)
                      (.setContentEncoding "gzip")
                      (.setContentType content-type)
                      .build)
        gzipped-content (utils/gzip-compress (if (string? content) content (slurp content)))]
    (.create @service
             blob-info
             gzipped-content
             (empty-varargs Storage$BlobTargetOption))))

(defn create-public-bucket [bucket-name]
  (let [public-acl acl/public-read
        bucket-info (-> (BucketInfo/newBuilder bucket-name)
                        (.setDefaultAcl [public-acl])
                        .build)]
    (.create @service bucket-info (empty-varargs Storage$BucketTargetOption))))
