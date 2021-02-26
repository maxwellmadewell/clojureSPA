(ns third.components.settings
  (:require
    [reagent.dom :as rdom]
    [reagent.core :as r]))

;TODO - not implemented
(defn simple-component [name]
  [:p "Hello, " name "!"])

(defn ^:export run []
  (rdom/render [simple-component] (.getElementById js/document "app")))
