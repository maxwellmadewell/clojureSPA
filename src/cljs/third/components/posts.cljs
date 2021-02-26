(ns third.components.posts
  (:require
    [reagent.core :as r :refer [atom]]
    [re-frame.core :as rf]
    [third.config :as cfg]
    [goog.object :as g]
    [clojure.string :as string]))

(defn post-component
  "Main post listing component - displays all posts retrieved from server/mongodb"
  [post]
  [:div.container.mt-5.w-100
   [:h2.text-center "All Posts"]
   (for [{:keys [_id username title description upvotes downvotes]} @post]
       [:div {:key _id}
        [:div.card
         [:div.card-content
          [:div.media
           [:div.media-content
            [:p.title.is-4 title]
            [:p.subtitle.is-6 username]]]
          [:div.content description]]
         [:div
          [:span.tag.is-rounded
           [:span
            [:span.icon
             [:img {:alt "thumbup"
                    :src "img/th_up.png"
                    :on-click #(do
                                  (rf/dispatch [:update-vote-count _id "upvote"]))}]]
            [:span [:span upvotes]]]]
          [:span.tag.is-rounded
           [:span
            [:span.icon
             [:img {:alt "thumbdown"
                    :src "img/th_d.png"
                    :on-click #(do
                                 (rf/dispatch [:update-vote-count _id "downvote"]))}]]
            [:span [:span downvotes]]]]]
         [:footer.card-footer
          [:a.card-footer-item {:href "#"
                                :on-click #(do
                                             (js/alert "Click Edit")
                                             (rf/dispatch [:modal-toggle]))} "Edit"]
          [:a.card-footer-item {:href "#"
                                :id _id
                                :on-click #(do
                                             (rf/dispatch [:delete-post-by-id _id]))} "Delete"]]]
        [:br]])])

(defn add-post-component
  "Add new post text areas - react component"
  []
  (let [fields (r/atom {})]
    (fn []
      [:div.container.mt-5.w-75
       [:div.text-center [:h2 "Create New Post"]]
       [:div
        [:div.form-group
         [:label {:for :title} "Title"]]
        [:textarea.textarea.is-primary {:placeholder "Enter Post Title"
                                        :name :title
                                        :rows 1
                                        :on-change #(swap! fields assoc :title (-> % .-target .-value))
                                        :value (:title @fields)}]
        [:div.form-group
         [:label {:for :description} "Description"]
         [:textarea.textarea.is-primary
          {:placeholder "Enter Post Description"
           :name :description
           :on-change #(swap! fields assoc :description (-> % .-target .-value))
           :value (:description @fields)}]
         [:h4]
         [:a.button.is-primary
          {:type :submit
           :on-click #(do
                        (rf/dispatch [:update-user-posts fields]))} "Save"]]]])))

(defn check-posts-component
  "Checks if posts exist before calling post-component react component"
  []
  (let [posts (rf/subscribe [:posts])]
    (fn []
      (if @posts
        [:div
           [:div (post-component posts)]]))))

