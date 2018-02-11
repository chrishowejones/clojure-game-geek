(ns user
  (:require [clojure-game-geek.schema :as s]
            [clojure.walk :as walk]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.pedestal :as lp]
            [io.pedestal.http :as http]
            [clojure.java.browse :refer [browse-url]]
            [clojure-game-geek.system :as system]
            [com.stuartsierra.component :as component])
  (:import clojure.lang.IPersistentMap))

(defn simplify
  "Converts all ordered maps nested within the map into standard hash
  maps, and sequences into vectors, whcih makes for easier constants
  in the tests, and eliminates ordering problems"
  [m]
  (walk/postwalk
   (fn [node]
     (cond
       (instance? IPersistentMap node) (into {} node)
       (seq? node) (vec node)
       :else node))
   m))

(defonce system (system/new-system))

(defn q
  [query-string]
  (-> system
      :schema-provider
      :schema
      (lacinia/execute query-string nil nil)
      simplify))


(defn start
  []
  (let [system (alter-var-root #'system component/start-system)]
    (browse-url "http://localhost:8888/")
    system))

(defn stop
  []
  (alter-var-root #'system component/stop-system))

(comment

  (start)

  (:db (system/new-system))

  (stop)

  (-> system :schema-provider :db)

  )
