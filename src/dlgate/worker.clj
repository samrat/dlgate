(ns dlgate.worker
  (:require [taoensso.carmine.message-queue :as mq]
            [copy-api.client :as copy]
            [me.raynes.fs :refer [delete mkdir]]
            [clj-http.client :as client]
            [dlgate.views :refer [consumer]]
            [dlgate.db :refer [insert-download update-download-status]]))

(defn filename
  [url]
  (let [headers (:headers (client/head url))]
    (or (->> (get headers "content-disposition" "")
             (re-find #"filename=\"(\S+)\"")
             second)
        (last (drop 3 (clojure.string/split url #"/")))
        "index.html")))

(defn download-and-upload
  [access-token url id]
  (let [file-name (filename url)
        local-path (format "/tmp/%s/%s" id file-name)]
    (try (do (mkdir (format "/tmp/%s" id))
             (with-open [w (clojure.java.io/output-stream local-path)]
               (.write w (:body (client/get url {:as :byte-array}))))
             (copy/upload-file consumer
                               access-token
                               :path (str "/dlgate")
                               :local-path local-path)
             (delete local-path)
             (update-download-status id url file-name "COMPLETE")
             {:status :success})
         (catch Exception e (do (delete local-path)
                                (update-download-status id url "FAILED")
                                {:status :error})))))

(defn start-workers
  [n]
  (repeatedly n
              #(mq/worker nil "dl-queue"
                          {:handler
                           (fn [{:keys [message attempt]}]
                             ;;(println message)
                             (download-and-upload (:access-token message)
                                                  (:url message)
                                                  (:id message)))})))