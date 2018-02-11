(ns clojure-game-geek.system
  (:require [clojure-game-geek.db :as db]
            [clojure-game-geek.schema :as schema]
            [clojure-game-geek.server :as server]
            [com.stuartsierra.component :as component]))

(defn new-system
  []
  (merge (component/system-map)
         (schema/new-schema-provider)
         (server/new-server)
         (db/new-db)))
