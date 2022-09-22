(ns app.item-edit-dialog
  (:require [reagent.core :as r]
            [app.store :as s]
            [app.items :refer [put-item]]
            ;; material-ui react components
            [reagent-mui.material.grid :refer [grid]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.text-field :refer [text-field]]
            [reagent-mui.material.dialog :refer [dialog]]
            [reagent-mui.material.dialog-title :refer [dialog-title]]
            [reagent-mui.material.dialog-content :refer [dialog-content]]
            [reagent-mui.material.dialog-actions :refer [dialog-actions]]
            ;; icons
            [reagent-mui.icons.title :refer [title]]
            [reagent-mui.icons.link :refer [link]]
            [reagent-mui.icons.person :refer [person]]
            [reagent-mui.icons.abc :refer [abc]]
            [reagent-mui.icons.save :refer [save]])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))

;; variables
(def open (r/atom false))
(def item (r/atom nil))

;; functions
(defn item-edit-dialog-open [&[id title url name val]]
  (reset! item
          {:id id
           :title title
           :url url
           :name name
           :val val})
  (reset! open true))

(defn clear-item []
  (reset! item
          {:id nil
           :title nil
           :url nil
           :name nil
           :val nil}))

(defn save-item [phrease]
  (.log js/console phrease)
  (go (put-item @item)
      (clear-item)))

(defn exec-save []
  (save-item (js/prompt "master phrase"))
  (s/init-items)
  (reset! open false))

(defn on-change-text [e key]
  (reset! item (assoc @item key (.. e -target -value))))

;; define reagent react component
(defn item-edit-dialog []
  [dialog
   {:open @open :onClose #(reset! open false)}
   [dialog-title "Edit Item"]
   [dialog-content
    [grid {:container true :alignItems "center" :spacing 4}
     [grid {:item true :xs 1} [title]]
     [grid {:item true :xs 11}
      [text-field {:variant "standard"
                   :fullWidth true
                   :value (:title @item)
                   :onChange (fn [e] (on-change-text e :title))}]]
     [grid {:item true :xs 1} [link]]
     [grid {:item true :xs 11}
      [text-field {:variant "standard"
                   :fullWidth true
                   :type "email"
                   :value (:url @item)
                   :onChange (fn [e] (on-change-text e :url))}]]
     [grid {:item true :xs 1} [person]]
     [grid {:item true :xs 11}
      [text-field {:variant "standard"
                   :fullWidth true
                   :type "email"
                   :value (:name @item)
                   :onChange (fn [e] (on-change-text e :name))}]]
     [grid {:item true :xs 1} [abc]]
     [grid {:item true :xs 11}
      [text-field {:variant "standard"
                   :fullWidth true
                   :type "password"
                   :value (:val @item)
                   :onChange (fn [e] (on-change-text e :val))}]]
     ]]
   [dialog-actions
    [button {:start-icon (r/as-element [save])
             :variant "contained"
             :on-click #(exec-save)}
     "save"]]
   ])
  
      
   
