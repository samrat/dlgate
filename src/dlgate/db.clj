(ns dlgate.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql]))

(def db "postgres://localhost:5432/mydb")

(defn schema
  []
  (jdbc/with-connection db
    (try (jdbc/create-table "downloads"
                            [:user_id :varchar "(20)"]
                            [:url :varchar "NOT NULL"]
                            [:filename :varchar "NOT NULL"]
                            [:status :varchar "(10)"]
                            [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])
         (catch Exception e e))))

(defn insert-download
  [user-id url filename status]
  (jdbc/insert! db
                :downloads
                nil
                [user-id url filename status]))

(defn update-download-status
  [user-id url filename status]
  (jdbc/update! db
                :downloads
                {:status status
                 :filename filename}
                (sql/where {:user_id user-id
                            :url url})))

(defn user-downloads
  [user-id]
  (jdbc/query db
              (sql/select * {:downloads :d}
                          (sql/where {:d.user_id user-id})
                          "ORDER BY created_at DESC")))