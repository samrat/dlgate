(ns dlgate.views
  (:require [dlgate.layout :as layout]
            [copy-api.auth :as auth]
            [copy-api.client :as copy]
            [ring.util.response :as ring]
            [sandbar.stateful-session :refer :all])
  (:load "copy_keys"))

(def consumer (auth/make-consumer consumer-key
                             consumer-secret))

(defn index
  []
  (if-let [access-token (session-get :access-token)]
    (str "Hey, "
         (:first_name (copy/account-info consumer access-token))
         "!")
    (layout/index)))

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