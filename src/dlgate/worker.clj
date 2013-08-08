(ns dlgate.worker
  (:require [taoensso.carmine.message-queue :as mq]
            [copy-api.client :as copy]
            [me.raynes.fs :refer [delete mkdir]]
            [clj-http.client :as client]
            [dlgate.views :refer [consumer]]
            [dlgate.db :refer [insert-download update-download-status]]))

(defn download-and-upload
  [access-token url]
  (let [file-name (last (clojure.string/split url #"/"))
        id (:id (copy/account-info consumer access-token))
        local-path (format "/tmp/%s/%s" id file-name)]
    (insert-download id url "PENDING")
    (mkdir (format "/tmp/%s" id))
    (with-open [w (clojure.java.io/output-stream local-path)]
      (.write w (:body (client/get url {:as :byte-array}))))
    (copy/upload-file consumer
                      access-token
                      :path (str "/dlgate/" file-name)
                      :local-path local-path)
    (delete local-path)
    (update-download-status id url "COMPLETE")
    nil))

(defn start-workers
  [n]
  (repeatedly n
              #(mq/worker nil "dl-queue"
                          {:handler
                           (fn [{:keys [message attempt]}]
                             (println message)
                             (download-and-upload (:access-token message)
                                                  (:url message)))})))