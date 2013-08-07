(ns dlgate.web
  (:use compojure.core
        [org.httpkit.server :only [run-server]])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.reload :as reload]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      handler/site
      reload/wrap-reload))

(defn -main [& args]
  (let [port (Integer/parseInt 
              (or (System/getenv "PORT") "8080"))]
    (run-server app {:port port :join? false})))
