(ns dlgate.worker
  (:require [taoensso.carmine.message-queue :as mq]
            [copy-api.client :as copy]
            [me.raynes.fs :refer [delete mkdir]]
            [clj-http.client :as client]
            [cemerick.url :as u]
            [dlgate.views :refer [consumer redis-spec]]
            [dlgate.db :refer [insert-download update-download-status]]
            [me.raynes.fs :as fs]
            [me.raynes.conch :refer [let-programs]]))

(defn filename
  [url]
  (let [headers (:headers (client/head url))]
    (or (->> (get headers "content-disposition" "")
             (re-find #"filename=\"(\S+)\"")
             second)
        (last (clojure.string/split (:path (u/url url))
                                    #"/"))
        "index.html")))

(defn download-and-upload
  [access-token url id]
  (let [file-name (filename url)
        local-path (format "/tmp/%s/%s" id file-name)]
    (try (do (mkdir (format "/tmp/%s" id))
             (let-programs [curl "/usr/bin/curl"]
                           ;; using curl avoids loading the whole
                           ;; file into memory before saving it.
               (curl "-o" local-path url))
             (copy/upload-file consumer
                               access-token
                               :path (str "/dlgate")
                               :local-path local-path)
             (update-download-status id
                                     url
                                     file-name
                                     (fs/size local-path)
                                     "COMPLETE")
             (delete local-path)
             {:status :success})
         (catch Exception e (do (delete local-path)
                                (update-download-status
                                 id
                                 url
                                 file-name
                                 "NA"
                                 "FAILED")
                                {:status :error})))))

(defn start-workers
  [n]
  (repeatedly n
              #(mq/worker
                {:spec (redis-spec)}
                "dl-queue"
                {:handler
                 (fn [{:keys [message attempt]}]
                   ;;(println message)
                   (download-and-upload (:access-token message)
                                        (:url message)
                                        (:id message)))})))