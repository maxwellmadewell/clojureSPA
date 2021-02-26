(ns third.components.navbar
  (:require
    [reagent.core :as r]))


(defn navbar
  "Navbar html/react component"
  []
  (r/with-let [expanded? (r/atom false)]
              [:nav.navbar.is-dark>div.container
               [:div.navbar-brand
                [:a.navbar-item {:href "/" :style {:font-weight :bold}} "My terrible app"]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click #(swap! expanded? not)
                  :class (when @expanded? :is-active)}
                 [:span] [:span] [:span]]]
               [:div#nav-menu.navbar-menu
                {:class (when @expanded? :is-active)}
                [:div.navbar-end
                 [:a.navbar-item {:href "/logout"} "Logout"]
                 [:a.navbar-item {:href "/settings"} "Settings"]]]]))