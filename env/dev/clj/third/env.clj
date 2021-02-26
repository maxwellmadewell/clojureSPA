(ns third.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [third.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[third started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[third has shut down successfully]=-"))
   :middleware wrap-dev})
