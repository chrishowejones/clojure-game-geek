(ns clojure-game-geek.db
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]))

(defrecord ClojureGameGeekDb [data]
  component/Lifecycle
  (start [this]
    (assoc this :data (-> (io/resource "cgg-data.edn")
                          slurp
                          edn/read-string
                          atom)))

  (stop [this]
    (assoc this :data nil)))

(defn new-db
  []
  {:db (map->ClojureGameGeekDb {})})

(defn- find-entity-by-id
  [entity]
  (fn [id db]
    (->> db
         :data
         deref
         entity
         (filter #(= id (:id %)))
         first)))

(defn find-game-by-id
  [db game-id]
  (let [find-by-id (find-entity-by-id :games)]
    (find-by-id game-id db)))

(defn find-member-by-id
  [db member-id]
  ((find-entity-by-id :members) member-id db))

(defn list-designers-for-game
  [db game-id]
  (let [games-designers (:designers (find-game-by-id db game-id))]
    (->> db
         :data
         deref
         :designers
         (filter #(contains? games-designers (:id %))))))

(defn list-games-for-designer
  [db designer-id]
  (->> db
       :data
       deref
       :games
       (filter #(contains? (:designers %) designer-id))))

(defn- get-ratings
  [db]
  (->> db :data deref :ratings))

(defn list-ratings-for-game
  [db game-id]
  (let [ratings (get-ratings db)]
    (filter #(= (:game_id %) game-id) ratings)))

(defn list-ratings-for-member
  [db member-id]
  (let [ratings (get-ratings db)]
    (filter #(= (:member_id %) member-id) ratings)))

(defn list-members
  [db member_name]
  (let [members (->> db :data deref :members)]
   (if member_name
     (filter #(re-find (re-pattern member_name) (:member_name %)) members)
     members)))

(defn list-games
  [db name]
  (let [games (->> db :data deref :games)]
    (if name
      (filter #(re-find (re-pattern name) (:name %)) games)
      games)))

(defn ^:private apply-game-rating
  [game-ratings game-id member-id rating]
  (->> game-ratings
       (remove #(and (= game-id (:game_id %))
                     (= member-id (:member_id %))))
       (cons {:game_id game-id
              :member_id member-id
              :rating rating})))

(defn upsert-game-rating
  "Adds a new game rating, or changes the value of an existing game rating"
  [db game-id member-id rating]
  (-> db
      :data
      (swap! update :ratings apply-game-rating game-id member-id rating)))

(comment

  (new-db)
  (def db (.start (map->ClojureGameGeekDb {})))

  (find-game-by-id db "1237")
  (find-member-by-id (->> user/system :schema-provider :db) "37")

  (upsert-game-rating (->> user/system :schema-provider :db) "1234" "2812" 4)

  (->> user/system :schema-provider :db)

  )
