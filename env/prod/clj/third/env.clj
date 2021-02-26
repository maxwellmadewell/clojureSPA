(ns third.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[third started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[third has shut down successfully]=-"))
   :middleware identity})
