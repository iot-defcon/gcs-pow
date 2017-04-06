(ns gcs.acl
  (:import com.google.cloud.storage.Acl))

(def public-read (Acl/of (Acl$User/ofAllUsers) Acl$Role/READER))
