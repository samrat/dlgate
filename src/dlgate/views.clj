(ns dlgate.views
  (:require [dlgate.layout :as layout]
            [copy-api.auth :as auth]
            [copy-api.client :as copy]
            [ring.util.response :as ring]
            [sandbar.stateful-session :refer :all]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as mq]
            [me.raynes.fs :as fs]
            [dlgate.db :refer (user-downloads insert-download pop-download)])
  (:use environ.core))

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
    (ring/redirect "/")))

(defn queue
  [url]
  (if-let [access-token (session-get :access-token)]
    (let [id (or (session-get :user-id)
                 (:id (copy/account-info consumer
                                         access-token)))]
      (do (car/wcar {:spec (redis-spec)}
                    (mq/enqueue "dl-queue"
                                    {:url url
                                     :access-token access-token
                                     :id id}))
          (insert-download id url url "NA" "PENDING")
          (future (pop-download id))
          (flash-put! :alert "Your download has been queued.")
          (ring/redirect "/")))
    (do (flash-put! :alert "You're not logged in!")
        (ring/redirect "/"))))
