(ns dlgate.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql])
  (:use environ.core))

(def db (env :database-url))

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

(defn user-records-count
  [user_id]
  (:count (first (jdbc/query db
                             ["SELECT COUNT(*) FROM downloads WHERE user_id=?"
                              user_id]))))

(defn pop-download
  "Removes one oldest download for the given user_id"
  [user_id]
  (when (> (user-records-count user_id) 10)
    (jdbc/execute!
     db
     ["DELETE FROM downloads WHERE (user_id, url, created_at) IN (SELECT user_id, url, created_at FROM downloads WHERE user_id=? ORDER BY created_at LIMIT 1)"
      user_id])))

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
                          "ORDER BY created_at DESC LIMIT 10")))

(defn -main []
  (schema))