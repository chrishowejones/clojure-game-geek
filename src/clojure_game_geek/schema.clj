(ns clojure-game-geek.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia.resolve :refer [resolve-as]]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [clojure-game-geek.db :as db]))

(defn rating-summary
  [db]
  (fn [_ _ board-game]
    (let [id (:id board-game)
          ratings (map :rating (db/list-ratings-for-game db id))
          n (count ratings)]
      {:count n
       :average (if (zero? n)
                  0
                  (/ (apply + ratings)
                     (float n)))})))

(defn member-ratings
  [db]
  (fn [_ _ member]
    (let [member-id (:id member)]
      (db/list-ratings-for-member db member-id))))

(defn game-rating->game
  [db]
  (fn [_ _ game-rating]
    (db/find-game-by-id db (:game_id game-rating))))

(defn all-members
  [db]
  (fn [_ {:keys [member_name]} _]
    (db/list-members db member_name)))

(defn all-games
  [db]
  (fn [_ {:keys [name]} _]
    (db/list-games db name)))

(defn game-by-id
  [db]
  (fn [_ args _]
    (db/find-game-by-id db (:id args))))

(defn member-by-id
  [db]
  (fn [_ args _]
    (db/find-member-by-id db (:id args))))

(defn board-game-designers
  [db]
  (fn [_ _ board-game]
    (db/list-designers-for-game db (:id board-game))))

(defn designer-games
  [db]
  (fn [_ _ designer]
    (db/list-games-for-designer db (:id designer))))

(defn rate-game
  [db]
  (fn [_ args _]
    (let [{game-id :game_id
           member-id :member_id
           rating :rating} args
          game (db/find-game-by-id db game-id)
          member (db/find-member-by-id db member-id)
          upsert-rating (fn [db game game-id member-id rating]
                          (db/upsert-game-rating db game-id member-id rating)
                          game)]
      (cond
        (nil? game)
        (resolve-as nil {:message "Game not found."
                         :status 404})
        (nil? member)
        (resolve-as nil {:message "Member not found"
                         :status 404})
        (not (<= 1 rating 5))
        (resolve-as nil {:message "Rating must be between 1 and 5"})
        :else
        (upsert-rating db game game-id member-id rating)))))

(defn rating-summary
  [cgg-data]
  (fn [_ _ board-game]
    (let [id (:id board-game)
          ratings (->> cgg-data
                       :ratings
                       (filter #(= id (:game_id %)))
                       (map :rating))
          n (count ratings)]
      {:count n
       :average (if (zero? n)
                  0
                  (/ (apply + ratings)
                     (float n)))})))

(defn member-ratings
  [ratings-map]
  (fn [_ _ member]
    (let [id (:id member)]
      (filter #(= id (:member_id %)) ratings-map))))

(defn game-rating->game
  [games-map]
  (fn [_ _ game-rating]
   (get games-map (:game_id game-rating))))

(defn resolver-map
  [component]
  (let [db (:db component)]
    {:query/game-by-id (game-by-id db)
     :query/member-by-id (member-by-id db)
     :query/all-members (all-members db)
     :query/all-games (all-games db)
     :mutation/rate-game (rate-game db)
     :BoardGame/designers (board-game-designers db)
     :BoardGame/rating-summary (rating-summary db)
     :GameRating/game (game-rating->game db)
     :Designer/games (designer-games db)
     :Member/ratings (member-ratings db)}))

(defn load-schema
  [component]
  (-> (io/resource "cgg-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map component))
      schema/compile))

(defrecord SchemaProvider [schema]
  component/Lifecycle
  (start [this]
    (assoc this :schema (load-schema this)))
  (stop [this]
    (-> this
        (assoc :schema nil)
        (assoc :db nil))))

(defn new-schema-provider
  []
  {:schema-provider (component/using (map->SchemaProvider {})
                                     [:db])})

(comment

  (new-schema-provider)
  ((game-by-id (:db user/system)) nil {:id "1234"} nil)
  ((:query/member-by-id (resolver-map (->> user/system :schema-provider))) nil {:id "37"} nil)

  ((member-by-id (->> user/system :schema-provider :db)) nil {:id "37"} nil)
  ((board-game-designers (->> user/system :schema-provider :db)) nil nil {:id "1237"})

  (-> {}
      load-schema)

  )
