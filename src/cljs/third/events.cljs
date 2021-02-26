(ns third.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [ajax.edn :as ajax-edn]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]
    [third.config :as cfg]))

;Database only events-----------------------------------------------------
;;Default page request navigation
(rf/reg-event-db
  :common/navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :common/route new-match))))

;;Default set error function - sets error in re-frame's app db
(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

;;Takes posts map and inserts into re-frame's app-db
(rf/reg-event-db
  :set-posts
  (fn [db [ _ posts]]
    (assoc db :posts posts)))

;Database and external  events--------------------------------------------

;;Get request to mongodb server, home.clj, to get all posts
(rf/reg-event-fx
  :fetch-all-posts
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/api/user-posts"
                  :response-format (ajax-edn/edn-response-format)
                  :on-success      [:set-posts]
                  :on-failure      [:common/set-error]}}))

;;TODO - creates new post, takes post map, dereferences,
;; then calls set-posts to update to reframe app-db
(rf/reg-event-fx
  :update-user-posts
  (fn [{:keys [db]} [_ post]]
    {:http-xhrio {:method          :put
                  :uri             "/api/user-posts"
                  :params          @post
                  :format          (ajax-edn/edn-request-format)
                  :response-format (ajax-edn/edn-response-format)
                  :on-success      [:set-posts]
                  :on-failure      [:common/set-error]}}))

;;Deletes post in mongodb server based on received post id string
(rf/reg-event-fx
  :delete-post-by-id
  (fn [{:keys [db]} [_ id]]
    {:http-xhrio {:method          :put
                  :uri             "/api/delete-post"
                  :params          {:id id}
                  :format          (ajax-edn/edn-request-format)
                  :response-format (ajax-edn/edn-response-format)
                  :on-success      [:set-posts]
                  :on-failure      [:common/set-error]}}))

;;updates/increments up/downvotes based on type (upvote/downvote) string and post id string
(rf/reg-event-fx
  :update-vote-count
  (fn [{:keys [db]} [_ id type]]
    {:http-xhrio {:method          :put
                  :uri             "/api/update-post-vote-count"
                  :params          {:type type
                                    :id id}
                  :format          (ajax-edn/edn-request-format)
                  :response-format (ajax-edn/edn-response-format)
                  :on-success      [:set-posts]
                  :on-failure      [:common/set-error]}}))


;;subscriptions-----------------------------------------------
(rf/reg-sub
  :common/route
  (fn [db _]
    (-> db :common/route)))

(rf/reg-sub
  :common/page-id
  :<- [:common/route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :common/page
  :<- [:common/route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

(rf/reg-sub
  :posts
  (fn [db _]
    (:posts db)))

(rf/reg-sub
  :last-updated
  (fn [db _]
    (:last-updated db)))