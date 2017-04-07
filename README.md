# gcs-pow
A basic Google Cloud Storage client for Clojure.

## Authentication and initialization
1. Create a service account and download a key file in JSON format: https://console.cloud.google.com/storage/browser
2. Put the key file in `resources/credentials/`
3. Get project ID from https://console.cloud.google.com/storage/settings
4. Run:  
```clojure
(require '[gcs.core :as gcs])

(gcs/initialize "PROJECT_ID" "credentials/KEY.json")
```

## Create an object
### Basic
```clojure
(require '[gcs.core :as gcs])
(require '[clojure.java.io :as io])

; Object content from a string
(gcs/create-object "bucket" "object-name" "Hello, world!")

; Object content from a stream
(gcs/create-object "bucket" "object-name" (io/input-stream (io/resource "index.html")))

; Object content from a byte array
(gcs/create-object "bucket" "object-name" (.toByteArray byte-array-output-stream)
```

### Advanced
```clojure
(require '[gcs.core :as gcs])
(require '[gcs.acl :as acl])
(require '[gcs.utils :as utils])

; Upload an object with public read access
(gcs/create-object
  {:bucket-name "bucket"
   :blob-id "object-name"
   :acl acl/public-read}
  "Hello, world!")

; Upload a gzipped file
(gcs/create-object
  {:bucket-name "bucket"
   :blob-id "index.html"
   :content-encoding "gzip"
   :content-type "text/html"}
  (utils/gzip-compress (slurp (io/resource "index.html"))))
```

## Create a bucket
### Basic
```clojure
(require '[gcs.core :as gcs])

(gcs/create-bucket "bucket")
```

### Advanced
```clojure
(require '[gcs.core :as gcs])
(require '[gcs.acl :as acl])

; Create a bucket with default public read access
(gcs/create-bucket
  {:name "bucket"
   :default-acl acl/public-read})
```
