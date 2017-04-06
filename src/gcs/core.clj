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

(defn new-bucket-info [bucket-info]
  (-> (BucketInfo/newBuilder (:name bucket-info))
      (.setAcl (:acl bucket-info))
      (.setCors (:cors bucket-info))
      (.setDefaultAcl (:default-acl bucket-info))
      (.setDeleteRules (:delete-rules bucket-info))
      (.setIndexPage (:index-page bucket-info))
      (.setLocation (:location bucket-info))
      (.setNotFoundPage (:not-found-page bucket-info))
      (.setStorageClass (:storage-class bucket-info))
      (.setVersioningEnabled (:versioning-enabled bucket-info))
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

(defn create-bucket [arg]
  (let [bucket-info (if (string? arg) (.build (BucketInfo/newBuilder arg)) (new-bucket-info arg))]
    (.create @service bucket-info (empty-varargs Storage$BucketTargetOption))))
