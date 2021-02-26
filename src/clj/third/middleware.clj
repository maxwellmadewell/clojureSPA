(ns third.middleware
  (:require
    [third.env :refer [defaults]]
    [cheshire.generate :as cheshire]
    [cognitect.transit :as transit]
    [clojure.tools.logging :as log]
    [third.layout :refer [error-page]]
    [third.middleware.formats :as formats]
    [third.config :refer [env]]
    [muuntaja.middleware :refer [wrap-format wrap-params]]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [ring.middleware.flash :refer [wrap-flash]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
    [ring.adapter.undertow.middleware.session :refer [wrap-session]]
    [ring.util.http-response :as response]
    [ring-ttl-session.core :refer [ttl-memory-store]]
    [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
    [buddy.auth.accessrules :refer [restrict]]
    [buddy.auth :refer [authenticated?]]
    [buddy.auth.backends.session :refer [session-backend]]))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "Invalid anti-forgery token"})}))


(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn on-error
  "Redirects to login page if not logged in/authorized"
  [request response]
  (response/found "/login"))

(defn wrap-restricted
  "Middleware func to restrict pages to authorized users"
  [handler]
  (restrict handler {:handler authenticated?
                     :on-error on-error}))

(defn wrap-auth
  "Middleware wrapper for serverside authorization"
  [handler]
  (let [backend (session-backend)]
    (-> handler
        (wrap-authentication backend)
        (wrap-authorization backend))))

;set expiration time for authorized users
(def ^:private three-days (* 60 60 24 3))

(defn wrap-base
  "Middleware wrapper to set authorization expiration"
  [handler]
  (-> ((:middleware defaults) handler)
      wrap-auth
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store] (ttl-memory-store three-days))))
      wrap-internal-error))
