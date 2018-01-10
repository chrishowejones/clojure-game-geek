(ns user
  (:require [clojure-game-geek.schema :as s]
            [clojure.walk :as walk]
            [com.walmartlabs.lacinia :as lacinia])
  (:import clojure.lang.IPersistentMap))

(def schema (s/load-schema))

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

(defn q
  [query-string]
  (simplify (lacinia/execute schema query-string nil nil)))

(comment

  (q "{ game_by_id(id: \"1234\") { id name summary}}")

  (q "{ game_by_id(id: \"1237\") { name designers { name }}}")

  (q "{ game_by_id(id: \"1237\") { name designers { name games { name }}}}")

  )
