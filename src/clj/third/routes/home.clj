(ns third.routes.home
  (:require
    [third.layout :refer [register-page login-page home-page reset-page settings-page new-pass-page]]
   [clojure.java.io :as io]
   [third.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]
   [third.db.core :as db]
   [third.validation :refer [validate-register validate-login validate-pass-length]]
   [buddy.hashers :as hs]
   [postal.core :refer [send-message]]
   [buddy.sign.jwt :as jwt]))

(def admin-email "functional.maxco@gmail.com")
(def admin-pass "temp_1234_812020")
(defonce secret "JWTTOKEN") ; change token once a day to invalidate tokens.
(def email-connect {
                    :host "smtp.gmail.com"
                    :tls true
                    :port 587
                    :user admin-email
                    :pass admin-pass})

(defn gen-sig [email]
  (jwt/sign {:user email} secret))

(defn unsign-token [token]
  (jwt/unsign token secret))

(defn register-handler! [{:keys [params]}]
  (println params)
  (if-let [errors (validate-register params)]
    (-> (response/found "/register")
        (assoc :flash {:errors errors
                       :email (:email params)}))
    (if-not (db/add-user (:email params) (:password params))
      (-> (response/found "/register")
          (assoc :flash {:errors {:email "Email already exists"}
                         :email (:email params)}))
      (-> (response/found "/login")
          (assoc :flash {:messages {:success "Registration Successful. Please log in."}
                         :email (:email params)})))))

(defn password-valid? [user pass]
  (hs/check pass (:password user)))


(defn login-handler [{:keys [params session]}]
  (if-let [errors (validate-login params)]
    (-> (response/found "/login")
        (assoc :flash {:errors errors
                       :email (:email params)}))
    (let [user (db/find-user (:email params))]
      (cond
        (not user)
        (-> (response/found "/login")
            (assoc :flash {:errors {:email "user with that email does not exist"}
                           :email (:email params)}))
        (and user
             (not (password-valid? user (:password params))))
        (-> (response/found "/login")
            (assoc :flash {:errors {:password "The password is wrong"}
                           :email (:email params)}))
        (and user
             (password-valid? user (:password params)))
        (let [updated-session (assoc session :identity (:email params))]
          (-> (response/found "/")
              (assoc :session updated-session)))))))


(defn email-contents [user link]
  (str user ",\n\n"
       "Click on password reset link below:\n"
       link))

(defn reset-handler [{:keys [params]}]
  (let [user (db/find-user (:email params))]
    (cond
      (not user)
      (-> (response/found "/reset")
          (assoc :flash {:errors {:email "User does not exist"}
                         :email (:email params)}))
      (and user)
      (try
        (send-message email-connect {:from admin-email
                                     :to (:email params)
                                     :subject "Password Reset Link from Third"
                                     :body (email-contents (:email params) (str "http://localhost:5000/api/verify-password?token=" (gen-sig (:email params))))})
        (->(response/found "/reset")
         (assoc :flash {:messages {:success "Password Reset Email Sent"}
                        :email (:email params)}))))))

(defn reset-password-handler [{:keys [params]}]
  (let [user (:user (unsign-token (:token params)))
        new-password (:password params)]
    (cond
      (> (count new-password) 7)
      (try (println "success")
           (db/change-pass user new-password)
           (->(response/found "/login")
              (assoc :flash {:messages {:success "Reset Successful. Log in."}
                               :email user})))

      (< (count new-password) 7)
      (try (println (str "the user " user))
           (->(response/found (str "/api/verify-password?token=" (:token params)))
              (assoc :flash {:errors {:password "Try a longer password"}}))))))

(defn logout-handler [request]
  (-> (response/found "/login")
      (assoc :session {})))

(defn get-posts-handler [{:keys [session]}]
  (let [email (:identity session)]
    (-> (response/ok (into [] (db/get-all-posts))))))
        ;(response/header "Content-Type" "application/edn"))))


(defn put-posts-handler [{:keys [params session]}]
  (let [email (:identity session)
        title (:title params)
        post (:id params)]
    (try
      (db/create-post (merge params {:username email :upvotes 0 :downvotes 0}))
      (-> (response/ok (pr-str (db/get-all-posts))))
          ;(response/header "Content-Type" "application/edn"))
      (catch Exception e (response/bad-request
                           (str "Error: " (.getMessage e)))))))

(defn delete-post-by-id [{:keys [params session]}]
  (let [email (:identity session)
        id (:id params)]
    (try
      (db/delete-post-by-id id)
      (-> (response/ok (pr-str (db/get-all-posts))))
      (catch Exception e (response/bad-request
                           (str "Error: " (.getMessage e)))))))

(defn update-post-vote-count [{:keys [params session]}]
  (let [email (:identity session)
        id (:id params)
        type (:type params)]
    (try
      (db/update-post-vote id type)
      (-> (response/ok (pr-str (db/get-all-posts))))
      (catch Exception e (response/bad-request
                           (str "Error: " (.getMessage e)))))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page
         :middleware [middleware/wrap-restricted]}]
   ["/api"
    {:middleware [middleware/wrap-restricted]}
    ["/user-posts"
     ["" {:get get-posts-handler
          :put put-posts-handler}]]
    ["/delete-post"
     ["" {:put delete-post-by-id}]]
    ["/update-post-vote-count"
     ["" {:put update-post-vote-count}]]]
   ["/register" {:get register-page
                 :post register-handler!}]
   ["/login" {:get login-page
              :post login-handler}]
   ["/logout" {:get logout-handler}]
   ["/reset" {:get reset-page
              :post reset-handler}]
   ["/settings" {:get settings-page
                 :middleware [middleware/wrap-restricted]}]
   ["/api/verify-password" {
                            :get new-pass-page
                            :post reset-password-handler}]])

