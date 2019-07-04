(ns gcs.simple)

(import com.google.auth.oauth2.ServiceAccountCredentials
      [com.google.cloud.storage BlobId BlobInfo BucketInfo Storage Storage$BlobTargetOption StorageOptions])

(def blob-id (BlobId/of bucket-name "x"))

(def storage (.getService (StorageOptions/getDefaultInstance)))

(def blob-info (-> (BlobInfo/newBuilder blob-id) (.setContentType "text/plain") .build))

(.create storage blob-info (.getBytes "hello cloud storage") (into-array Storage$BlobTargetOption []))
