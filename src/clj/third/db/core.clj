(ns third.db.core
  (:require
    [monger.core :as mg]
    [monger.collection :as mc]
    [monger.operators :refer :all]
    [mount.core :refer [defstate]]
    [third.config :refer [env]]
    [buddy.hashers :as hs])
  (:import
           (org.bson.types ObjectId)))

(defstate db*
  :start (-> env :database-url mg/connect-via-uri)
  :stop (-> db* :conn mg/disconnect))

(defstate db
  :start (:db db*))

(defn change-pass
  "finds user based on email address"
  [user-email new-pass]
  (let [conn (mg/connect)
        db (mg/get-db conn "users")]
    (mc/find-one-as-map db "user" {:email user-email})))


(defn add-user
  "Adds new user (email/password) to mongodb if not already existing"
  [email new-password]
  (let [conn (mg/connect)
        db (mg/get-db conn "users")]
    (when-not (mc/find-one-as-map db "user" {:email email})
      (mc/insert db "user" {:_id (ObjectId.)
                            :email email
                            :password (hs/derive new-password)}))))


(defn update-user
  "TODO - updates names and email of registered user in mongodb"
  [id first-name last-name email]
  (let [conn (mg/connect)
        db (mg/get-db conn "users")]
    (mc/update db "user" {:_id (ObjectId. id)}
             {$set {:first_name first-name
                      :last_name last-name
                      :email email}})))

(defn find-user
  "Finds user from mongodb based on email/username"
  [email]
  (let [conn (mg/connect)
        db (mg/get-db conn "users")]
    (mc/find-one-as-map db "user" {:email email})))

(defn create-post
  "creates new post in mongodb, takes post object and merges with new object id"
  [post]
  (let [conn (mg/connect)
        db (mg/get-db conn "users")]
    (mc/insert-and-return db "posts"
                          (merge post {:_id (ObjectId.)})))) ;;TODO - if post exists as map with keys equal to db will update correctly?

(defn update-post-vote
  "Increments up/downvote count for existing post in mongodb, takes mongo object ID string"
  [post_id type]
  (let [conn (mg/connect)
        db (mg/get-db conn "users")]
    (mc/update-by-id db "posts"
                     (ObjectId. post_id) (if (= type "upvote")
                                           {$inc {:upvotes 1}}
                                           {$inc {:downvotes 1}}))))


(defn get-post-by-id
  "Gets existing post from mongodb based on object ID string"
  [post_id]
  (let [conn (mg/connect)
        db (mg/get-db conn "users")]
    (mc/find-map-by-id db "posts" (ObjectId. post_id))))


(defn delete-post-by-id
  "Deletes post in mongodb based on object ID string"
  [post_id]
  (let [conn (mg/connect)
        db (mg/get-db conn "users")]
    (mc/remove-by-id db "posts" (ObjectId. post_id))))


(defn transform-to-str
  "Converts Object ID field to string format"
  [document]
  (if-let [id (:_id document)]
    (assoc document :_id (.toString id))))

(defn get-all-posts
  "Gets all posts in mongodb"
  []
  (let [conn (mg/connect)
        db (mg/get-db conn "users")]
    (map transform-to-str (mc/find-maps db "posts"))))




