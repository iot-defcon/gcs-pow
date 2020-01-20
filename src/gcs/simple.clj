(ns gcs.simple
  (:import [com.google.cloud.storage BlobId BlobInfo Storage$BlobTargetOption StorageOptions]
           [com.google.cloud.storage Acl Acl$Role Acl$User]))

(def ^:static bucket-name "spark-static-pow")
(def ^:static file-name "simple.html")

(def blob-id (BlobId/of bucket-name file-name))

(def storage (.getService (StorageOptions/getDefaultInstance)))

(def blob-info
  (-> (BlobInfo/newBuilder blob-id)
      (.setContentType "text/html")
      (.setAcl (list (Acl/of (Acl$User/ofAllUsers) Acl$Role/READER)))
      .build))

(defn -main [& _]
  (.create storage
           blob-info
           (.getBytes "<!DOCTYPE html><html><body><h1>SIMPLE</h1></body></html>")
           (into-array Storage$BlobTargetOption [])))