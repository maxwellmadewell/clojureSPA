  (ns third.layout
    (:require
      [selmer.parser :as parser]
      [selmer.filters :as filters]
      [markdown.core :refer [md-to-html-string]]
      [ring.util.http-response :refer [content-type ok]]
      [ring.util.anti-forgery :refer [anti-forgery-field]]
      [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
      [ring.util.response]))

(parser/set-resource-path!  (clojure.java.io/resource "html"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :markdown (fn [content] [:safe (md-to-html-string content)]))

(defn render
  "renders the HTML template located relative to resources/html"
  [request template & [params]]
  (content-type
    (ok
      (parser/render-file
        template
        (assoc params
          :page template
          :csrf-token *anti-forgery-token*)))
    "text/html; charset=utf-8"))

(defn auth-page
  "Wraps pages request in authorization"
  [type]
  (fn [{:keys [flash] :as request}]
    (render
        request
        (str type ".html")
        (select-keys flash [:errors :email :messages]))))

;wrap server based pages in authorization
(def register-page (auth-page "register"))
(def login-page (auth-page "login"))
(def reset-page (auth-page "reset"))
(def settings-page (auth-page "settings"))
(def new-pass-page (auth-page "newPass"))

(defn home-page [request]
  (render request "home.html"))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (parser/render-file "error.html" error-details)})
