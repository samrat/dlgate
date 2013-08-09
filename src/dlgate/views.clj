(ns dlgate.views
  (:require [dlgate.layout :as layout]
            [copy-api.auth :as auth]
            [copy-api.client :as copy]
            [ring.util.response :as ring]
            [sandbar.stateful-session :refer :all]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as mq]
            [dlgate.db :refer (user-downloads insert-download)])
  (:load "copy_keys"))

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
       :prev-downloads (take 10 (user-downloads (:id account-info)))))
    (layout/index :alert (flash-get :alert))))

(defn login
  []
  (let [request-token (auth/request-token consumer
                                          "http://localhost:8080/auth")
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
      (do (car/wcar nil (mq/enqueue "dl-queue"
                                    {:url url
                                     :access-token access-token
                                     :id id}))
          (insert-download id url url "PENDING")
          (flash-put! :alert "Your download has been queued.")
          (ring/redirect "/")))
    (do (flash-put! :alert "You're not logged in!")
        (ring/redirect "/"))))
