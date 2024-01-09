(ns app.views.item-edit-dialog
  (:require
    [app.logics.items :refer [put-item]]
    [app.logics.sesame :refer [encrypt-text]]
    [app.stores.store :as s]
    [app.views.modal-dialog :refer [modal-dialog]]
    [cljs.core.async :refer [<! >! chan]]
    ;; icons
    [reagent-mui.icons.abc :refer [abc]]
    [reagent-mui.icons.link :refer [link]]
    [reagent-mui.icons.person :refer [person]]
    [reagent-mui.icons.save :refer [save]]
    [reagent-mui.icons.title :refer [title]]
    ;; material-ui react components
    [reagent-mui.material.button :refer [button]]
    [reagent-mui.material.dialog :refer [dialog]]
    [reagent-mui.material.dialog-actions :refer [dialog-actions]]
    [reagent-mui.material.dialog-content :refer [dialog-content]]
    [reagent-mui.material.dialog-title :refer [dialog-title]]
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
  [v]
  (save-item (:v1 v) (:v2 v))
  (reset! open false))


(defn on-change-text
  [e key]
  (reset! item (assoc @item key (.. e -target -value))))


;; define reagent react component
(defn item-edit-dialog
  []
  (let [modal (r/atom false)
        ch (chan)]
    (fn []
      [dialog
       {:open @open :on-close #(reset! open false) :full-width true}
       [dialog-title "Input"]
       [dialog-content
        [:div {:style {:display "flex" :align-items "center"}}
         [title {:style {:margin "8px 16px 8px 0px"}}]
         [text-field {:variant "standard"
                      :fullWidth true
                      :defaultValue (:title @item)
                      :onChange (fn [e] (on-change-text e :title))}]]
        [:div {:style {:display "flex" :align-items "center"}}
         [link {:style {:margin "8px 16px 8px 0px"}}]
         [text-field {:variant "standard"
                      :fullWidth true
                      :type "url"
                      :defaultValue (:url @item)
                      :onChange (fn [e] (on-change-text e :url))}]]
        [:div {:style {:display "flex" :align-items "center"}}
         [person {:style {:margin "8px 16px 8px 0px"}}]
         [text-field {:variant "standard"
                      :fullWidth true
                      :type "url"
                      :defaultValue (:name @item)
                      :onChange (fn [e] (on-change-text e :name))}]]
        [:div {:style {:display "flex" :align-items "center"}}
         [abc {:style {:margin "8px 16px 8px 0px"}}]
         [text-field {:variant "standard"
                      :fullWidth true
                      :type "password"
                      :defaultValue (:val @item)
                      :onChange (fn [e] (on-change-text e :val))}]]]
       [dialog-actions
        [button {:start-icon (r/as-element [save])
                 :variant "contained"
                 :on-click (fn []
                             (go
                               (reset! modal true)
                               (exec-save (<! ch))))}
         "save"]]
       [modal-dialog {:open @modal
                      :on-close (fn [v1 v2]
                                  (go (reset! modal false)
                                      (>! ch {:v1 v1 :v2 v2})))}]])))


