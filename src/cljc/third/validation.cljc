(ns third.validation
  (:require [struct.core :as st]))

;Registration email and password schema enforcement----------
(def register-schema
  [[:email
    st/required
    st/string
    st/email]

   [:password
    st/required
    st/string
    {:message "password must contain at least 8 characters"
     :validate #(> (count %) 7)}]])

(def password-length-schema
  [[:password
    st/required
    st/string
    {:message "password must contain at least 8 characters"
     :validate #(> (count %) 7)}]])

(def login-schema
  [[:email
    st/required
    st/string
    st/email]

   [:password
    st/required
    st/string]])

(defn validate-register
  "validates registration requirements/schema"
  [params]
  (first (st/validate params register-schema)))

(defn validate-pass-length
  "validates password requirements/schema"
  [params]
  (first (st/validate params password-length-schema)))

(defn validate-login
  "validates password requirements/schema"
  [params]
  (first (st/validate params login-schema)))