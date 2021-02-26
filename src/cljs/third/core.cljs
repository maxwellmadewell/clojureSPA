(ns third.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [reagent.core :as r :refer [atom]]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [third.ajax :as ajax]
    [third.components.navbar :refer [navbar]]
    [third.components.posts :refer [check-posts-component add-post-component]]
    [third.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string]
    [devtools.core :as devtools])
  (:import goog.History))

(defn home-page
  "Main home page react component - pulls in posts-component and add-posts"
  []
  [:section.section>div.container>div.content
     [:div
      [check-posts-component]]
   [:div
      [add-post-component]]])



(defn page
  "Checks if pages exists before dispatching navbar and page request"
  []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [navbar]
     [page]]))

;; -------------------------
;; Routes
(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name :home
           :view #'home-page}]]))

(defn start-router!
  "TODO - Luminus default start router function - removed for now"
  []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation!
  "TODO - need to understand this function - pulled from Luminus default template"
  []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (let [uri (or (not-empty (string/replace (.-token event) #"^.*#" "")) "/")]
          (rf/dispatch
             [:common/navigate (reitit/match-by-path router uri)]))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:common/navigate (reitit/match-by-name router :home)])
  (ajax/load-interceptors!)
  (rf/dispatch [:fetch-all-posts])
  (hook-browser-navigation!)
  (mount-components))
