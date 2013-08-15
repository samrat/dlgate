(ns dlgate.views
  (:require [dlgate.layout :as layout]
            [copy-api.auth :as auth]
            [copy-api.client :as copy]
            [ring.util.response :as ring]
            [sandbar.stateful-session :refer :all]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as mq]
            [me.raynes.fs :as fs]
            [clj-http.client :as client]
            [dlgate.db :refer (user-downloads insert-download pop-download)])
  (:use environ.core))

(declare queue)

(def consumer-key (env :copy-key))
(def consumer-secret (env :copy-secret))
(def callback-url (env :callback-url))

(defn redis-spec
  []
  {:host (env :redis-host)
   :port (read-string (env :redis-port))
   :password (env :redis-password)})

(def consumer (auth/make-consumer consumer-key
                                  consumer-secret))

(defn index
  []
  (if-let [access-token (session-get :access-token)]
    (let [account-info (copy/account-info consumer access-token)]
      (session-put! :user-id (:id account-info))
      (layout/logged-in
       account-info
       :alert (flash-get :alert)
       :prev-downloads (user-downloads (:id account-info))))
    (layout/index :alert (flash-get :alert))))

(defn login
  []
  (let [request-token (auth/request-token consumer
                                          callback-url)
        redirect-url (auth/authorization-url consumer request-token)]
    (session-put! :request-token request-token)
    (ring/redirect redirect-url)))

(defn auth
  [oauth-token oauth-verifier]
  (let [request-token (session-get :request-token)]
    (session-put! :access-token
                  (auth/access-token-response consumer
                                              request-token
                                              oauth-verifier))
    (when (session-get :url)
      (do (queue (session-get :url))
          (session-delete-key! :url)))
    (ring/redirect "/")))

(defn size
  [url]
  (let [headers (:headers (client/head url))]
    (read-string (get headers "content-length" "-1"))))

(defn queue
  [url]
  (if-let [access-token (session-get :access-token)]
    (let [id (or (session-get :user-id)
                 (:id (copy/account-info consumer
                                         access-token)))
          filesize (future (size url))
          message {:url url
                   :access-token access-token
                   :id id}
          add-to-queue (fn [queue]
                         (do (car/wcar {:spec (redis-spec)}
                                       (mq/enqueue queue
                                                   message))
                             (insert-download id
                                              url
                                              url
                                              "NA"
                                              "PENDING")))]
      (do (future
            (cond (= @filesize -1)
                  (add-to-queue "large-files-queue")
                  (< @filesize (* 50 1024 1024))
                  (add-to-queue "small-files-queue")
                  (< @filesize (* 180 1024 1024))
                  (add-to-queue "large-files-queue")
                  :else (insert-download id url url "NA" "FAILED")))
          (future (pop-download id))
          (flash-put! :alert "Your download has been queued.")
          (ring/redirect "/")))
    (do (flash-put! :alert "You're not logged in!")
        (ring/redirect "/"))))
