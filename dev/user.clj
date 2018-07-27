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

  (stop)

  system

  (q "{ member_by_id(id: \"1410\") { member_name ratings { game { name rating_summary { count average } designers { name  games { name }}} rating }}}")

  (q "{ member_by_id(id: \"1410\") { member_name ratings { game { id name } rating }}}")

  (q "mutation { rate_game(member_id: \"1410\", game_id: \"1236\", rating: 3) { rating_summary { count average }}}")
  (q "mutation { rate_game(member_id: \"1410\", game_id: \"1235\", rating: 4) { rating_summary { count average }}}")

  ;; bad mutation query
  (q "mutation { rate_game(member_id: \"1410\", game_id: \"9999\", rating: 4) { name rating_summary { count average }}}")
  (q "mutation { rate_game(member_id: \"1410\", game_id: \"1234\", rating: 0) { name rating_summary { count average }}}")
  (q "mutation { rate_game(member_id: \"1234\", game_id: \"1234\", rating: 4) { name rating_summary { count average }}}")

  ;; overall parsing query error
  (q "mutation { rate_game(member_id: \"1410\", game_id: \"1234\") { name rating_summary { count average }}}")
  (q "mutation { rate_game(member_id: \"1410\", game_id: \"1234\", rating: \"Great!\") { name rating_summary { count average }}}")

  # added a change

  )
