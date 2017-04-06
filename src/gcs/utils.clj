(ns gcs.utils
  (:import java.util.zip.GZIPOutputStream))

(defn gzip-compress [content]
  (let [baos (java.io.ByteArrayOutputStream.)
        gzip (GZIPOutputStream. baos)]
    (.write gzip (.getBytes content "UTF-8"))
    (.close gzip)
    (.toByteArray baos)))

(defn bytes? [x]
  (if (nil? x)
    false
    (= (Class/forName "[B")
       (.getClass x))))
