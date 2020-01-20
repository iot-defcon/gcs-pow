(ns gcs.simple
  (:import [com.google.cloud.storage BlobId BlobInfo BucketInfo$LifecycleRule BucketInfo$LifecycleRule$LifecycleAction BucketInfo$LifecycleRule$LifecycleCondition Storage$BlobTargetOption Storage$BucketGetOption Storage$BucketTargetOption StorageOptions]
           [com.google.cloud.storage Acl Acl$Role Acl$User]))

(def ^:static bucket-name "spark-static-pow")
(def ^:static file-name "simple.html")

(def blob-id (BlobId/of bucket-name file-name))

(def storage (.getService (StorageOptions/getDefaultInstance)))

(def blob-info
  (-> (BlobInfo/newBuilder blob-id)
      (.setContentType "text/html")
      (.setAcl (list (Acl/of (Acl$User/ofAllUsers) Acl$Role/READER)))
      (.setCacheControl "public, max-age=10800")
      .build))

(defn set-lifecycle []
  (let [delete-rule (BucketInfo$LifecycleRule. (BucketInfo$LifecycleRule$LifecycleAction/newDeleteAction)
                                               (-> (BucketInfo$LifecycleRule$LifecycleCondition/newBuilder)
                                                   (.setAge (int 1))
                                                   .build))
        bucket (.get storage bucket-name (into-array Storage$BucketGetOption []))]
    (if (empty? (.getLifecycleRules bucket))
      (-> bucket
          .toBuilder
          (.setLifecycleRules (list delete-rule))
          .build
          (.update (into-array Storage$BucketTargetOption [])))
      (println "bucket has some lifecycle rules"))))

(defn -main [& _]
  (set-lifecycle)
  (.create storage
           blob-info
           (.getBytes "<!DOCTYPE html><html><body><h1>SIMPLE</h1></body></html>")
           (into-array Storage$BlobTargetOption [])))