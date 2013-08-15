(ns dlgate.web
  (:use compojure.core
        sandbar.stateful-session
        [org.httpkit.server :only [run-server]])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.reload :as reload]
            [ring.util.response :as ring]
            [dlgate.worker :refer [start-workers]]
            [dlgate.views :as views]))

(defroutes app-routes
  (GET "/" [] (views/index))
  (GET "/login" [] (views/login))
  (POST "/save-url" [url] (do (flash-put! :url url)
                              (ring/redirect "/login")))
  (GET "/auth" [oauth_token oauth_verifier] (views/auth oauth_token
                                                        oauth_verifier))
  (POST "/q" [url] (views/queue url))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      handler/site
      reload/wrap-reload
      wrap-stateful-session))

(defn -main [& args]
  (let [port (Integer/parseInt 
              (or (System/getenv "PORT") "8080"))]
    (doall (start-workers "small-files-queue" 3))
    (doall (start-workers "large-files-queue" 1))
    (run-server app {:port port :join? false})))
