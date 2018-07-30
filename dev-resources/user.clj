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
  (alter-var-root #'system component/start-system)
  (browse-url "http://localhost:8888/")
  :started)

(defn stop
  []
  (alter-var-root #'system component/stop-system))

(comment

  (q "{ game_by_id(id: \"1234\") { id name summary}}")

  (q "{ game_by_id(id: \"1237\") { name designers { name }}}")

  (q "{ game_by_id(id: \"1237\") { name designers { name url games { name }}}}")

  (q "{ member_by_id(id: \"37\") { member_name ratings { rating game { name }} }}")

  (alter-var-root #'system (constantly (system/new-system)))

  (start)

  system

  (alter-var-root #'system component/start-system)

  (-> system :schema-provider :schema)

  (stop)

  (list 1 2 3 4)
  '(1 2 3 4)

  (def  stringify (fn [x] (str x)))
  (defn stringify2 [x] (str x))
  (stringify 1)

  (component/stop-system system)

  (hash-map :a 2 "b" 2)
  (get {:a 1 "b" "2"} "b")
  ("b" { "b" "2"} )

  (Integer/parseInt "2")

  (filter :a [{:a 1} {:a 2 :b 3} {:b 3}])

  )
