(ns gcs.core
  (:require [clojure.java.io :as io]
            [gcs.acl :as acl]
            [gcs.utils :as utils])
  (:import [com.google.cloud.storage Acl Acl$User Acl$Role BlobId BlobInfo BucketInfo Storage Storage$BlobTargetOption Storage$BlobWriteOption Storage$BucketTargetOption StorageOptions]
           com.google.auth.oauth2.ServiceAccountCredentials))

(def service (atom nil))

(defn empty-varargs [klass]
  (into-array klass []))

(defn new-blob-info [blob-info]
  (-> (BlobInfo/newBuilder (:bucket blob-info) (:blob-id blob-info))
      (.setAcl (:acl blob-info))
      (.setCacheControl (:cache-control blob-info))
      (.setContentDisposition (:content-disposition blob-info))
      (.setContentEncoding (:content-encoding blob-info))
      (.setContentLanguage (:content-language blob-info))
      (.setContentType (:content-type blob-info))
      (.setCrc32c (:crc32c blob-info))
      (.setMd5 (:md5 blob-info))
      (.setMetadata (:metadata blob-info))
      (.setStorageClass (:storage-class blob-info))
      .build))

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

(defn create-object
  ([bucket object-name content]
   (create-object (.build (BlobInfo/newBuilder bucket object-name)) content))

  ([blob-info content]
   (let [content (if (string? content) (.getBytes content) content)
         options-class (if (or (string? content) (utils/bytes? content)) Storage$BlobTargetOption Storage$BlobWriteOption)]
     (.create
      @service
      (new-blob-info blob-info)
      content
      (empty-varargs options-class)))))

(defn create-public-bucket [bucket-name]
  (let [public-acl acl/public-read
        bucket-info (-> (BucketInfo/newBuilder bucket-name)
                        (.setDefaultAcl [public-acl])
                        .build)]
    (.create @service bucket-info (empty-varargs Storage$BucketTargetOption))))
