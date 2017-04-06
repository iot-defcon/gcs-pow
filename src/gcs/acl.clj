(ns gcs.acl
  (:import [com.google.cloud.storage Acl Acl$User Acl$Role]))

(def public-read (Acl/of (Acl$User/ofAllUsers) Acl$Role/READER))
