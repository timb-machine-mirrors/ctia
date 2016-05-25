(ns ctia.test-helpers.db
  (:require [ctia.store :as store]
            [ctia.stores.sql.db :as sql-db]
            [ctia.stores.sql.judgement :as sql-judgement]
            [ctia.stores.sql.store :as sql-store]
            [ctia.test-helpers.core :as helpers-core]
            [clojure.java.io :as io]
            [korma.core :as k]))

(def ddl (atom nil))
(def tables (atom nil))

(defn read-ddl []
  (or @ddl
      (reset! ddl
              (slurp
               (io/reader
                (io/as-file
                 (io/resource "schema.sql")))))))

(defn table-names []
  (or @tables
      (reset! tables
              (map last (re-seq #"CREATE TABLE (\w+)"
                                (read-ddl))))))

(defn create-tables [db]
  (k/exec-raw db (read-ddl)))

(defn exec-raw-for-all-tables [f]
  (fn [db]
    (doseq [t (table-names)]
      (k/exec-raw db (f t)))))

(def truncate-tables
  (exec-raw-for-all-tables
   (fn [table]
     (format "TRUNCATE TABLE \"%s\";" table))))

(def drop-tables
  (exec-raw-for-all-tables
   (fn [table]
     (format "DROP TABLE IF EXISTS \"%s\";" table))))

(defn fixture-properties:sql-store [test]
  (helpers-core/with-properties
    ["ctia.store.default.type"           "atom"
     "ctia.store.judgement.type"         "sql"
     "ctia.store.judgement.classname"    "org.h2.Driver"
     "ctia.store.judgement.subprotocol"  "h2"
     "ctia.store.judgement.subname"      "/tmp/ctia-h2-db;DATABASE_TO_UPPER=false"
     "ctia.store.judgement.delimiters"   ""]
    (test)))

(defn fixture-db-recreate-tables [test]
  (drop-tables @sql-db/db)
  (create-tables @sql-db/db)
  (test)
  (drop-tables @sql-db/db))

(defn fixture-clean-db [f]
  (truncate-tables @sql-db/db)
  (f))
