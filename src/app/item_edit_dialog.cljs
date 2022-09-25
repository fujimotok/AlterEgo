(ns app.item-edit-dialog
  (:require
    [app.items :refer [put-item]]
    [app.sesame :refer [encrypt-text]]
    [app.store :as s]
    [cljs.core.async :refer [<!]]
    [reagent-mui.icons.abc :refer [abc]]
    [reagent-mui.icons.link :refer [link]]
    [reagent-mui.icons.person :refer [person]]
    [reagent-mui.icons.save :refer [save]]
    ;; icons
    [reagent-mui.icons.title :refer [title]]
    [reagent-mui.material.button :refer [button]]
    [reagent-mui.material.dialog :refer [dialog]]
    [reagent-mui.material.dialog-actions :refer [dialog-actions]]
    [reagent-mui.material.dialog-content :refer [dialog-content]]
    [reagent-mui.material.dialog-title :refer [dialog-title]]
    ;; material-ui react components
    [reagent-mui.material.text-field :refer [text-field]]
    [reagent.core :as r])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


;; variables
(def open (r/atom false))
(def item (r/atom nil))


;; functions
(defn item-edit-dialog-open
  [& [id title url name]]
  (reset! item
          {:id id
           :title title
           :url url
           :name name
           :val ""})
  (reset! open true))


(defn clear-item
  []
  (reset! item
          {:id nil
           :title nil
           :url nil
           :name nil
           :val nil}))


(defn save-item
  [v1 v2]
  (go
    (when (not (:id @item))
      (reset! item (assoc @item :id
                          (.. (<! (put-item {:title "" :url "" :name "" :val ""})) -target -result))))
    (reset! item (assoc @item :val (<! (encrypt-text v1 v2 (str (:id @item)) (:val @item)))))
    (<! (put-item @item))
    (s/init-items)
    (clear-item)))


(defn exec-save
  []
  (save-item (js/prompt "phrase1") (js/prompt "phrase2"))
  (reset! open false))


(defn on-change-text
  [e key]
  (reset! item (assoc @item key (.. e -target -value))))


;; define reagent react component
(defn item-edit-dialog
  []
  [dialog
   {:open @open :on-close #(reset! open false) :full-width true}
   [dialog-title "Input"]
   [dialog-content
    [:div {:style {:display "flex" :align-items "center"}}
     [title {:style {:margin "8px 16px 8px 0px"}}]
     [text-field {:variant "standard"
                  :fullWidth true
                  :value (:title @item)
                  :onChange (fn [e] (on-change-text e :title))}]]
    [:div {:style {:display "flex" :align-items "center"}}
     [link {:style {:margin "8px 16px 8px 0px"}}]
     [text-field {:variant "standard"
                  :fullWidth true
                  :type "url"
                  :value (:url @item)
                  :onChange (fn [e] (on-change-text e :url))}]]
    [:div {:style {:display "flex" :align-items "center"}}
     [person {:style {:margin "8px 16px 8px 0px"}}]
     [text-field {:variant "standard"
                  :fullWidth true
                  :type "url"
                  :value (:name @item)
                  :onChange (fn [e] (on-change-text e :name))}]]
    [:div {:style {:display "flex" :align-items "center"}}
     [abc {:style {:margin "8px 16px 8px 0px"}}]
     [text-field {:variant "standard"
                  :fullWidth true
                  :type "password"
                  :value (:val @item)
                  :onChange (fn [e] (on-change-text e :val))}]]]
   [dialog-actions
    [button {:start-icon (r/as-element [save])
             :variant "contained"
             :on-click #(exec-save)}
     "save"]]])


