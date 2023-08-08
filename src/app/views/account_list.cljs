(ns app.views.account-list
  (:require
    ;; model
    [app.logics.items :refer [del-item]]
    [app.stores.store :as s]
    [app.views.item-edit-dialog :refer [item-edit-dialog-open]]
    [app.views.two-face :refer [two-face]]
    ;; icons
    [reagent-mui.icons.abc :refer [abc]]
    [reagent-mui.icons.delete-icon :refer [delete]]
    [reagent-mui.icons.edit :refer [edit]]
    [reagent-mui.icons.expand-more :refer [expand-more]]
    [reagent-mui.icons.link :refer [link]]
    [reagent-mui.icons.person :refer [person]]
    ;; material-ui react components
    [reagent-mui.material.accordion :refer [accordion]]
    [reagent-mui.material.accordion-actions :refer [accordion-actions]]
    [reagent-mui.material.accordion-details :refer [accordion-details]]
    [reagent-mui.material.accordion-summary :refer [accordion-summary]]
    [reagent-mui.material.icon-button :refer [icon-button]]
    [reagent-mui.material.list :refer [list]]
    [reagent-mui.material.typography :refer [typography]]
    [reagent.core :as r]))


;; define reagent react component
(defn account-list
  [{:keys [items]}]
  [list {:style {:margin-top "calc(env(safe-area-inset-top) + 80px)" :margin-bottom "80px"}}
   (for [item items]
     ^{:key (:id item)}
     [accordion
      [accordion-summary
       {:expandIcon (r/as-element [expand-more])}
       [typography {:variant "subtitle1"} (:title item)]]
      [accordion-details
       [:div {:style {:display "flex" :align-items "center"}}
        [link {:style {:margin "8px 16px 8px 0px"}}]
        [:a {:href (:url item), :target "_blank" :style {:overflow "hidden", :white-space "nowrap", :text-overflow "ellipsis"}}
         [typography {:variant "body1"} (:url item)]]]
       [:div {:style {:display "flex" :align-items "center"}}
        [person {:style {:margin "8px 16px 8px 0px"}}]
        [typography {:variant "body1"} (:name item)]]
       [:div {:style {:display "flex" :align-items "center"}}
        [abc {:style {:margin "8px 16px 8px 0px"}}]
        [two-face {:id (:id item), :val (:val item)}]]]
      [accordion-actions
       [icon-button {:on-click #(item-edit-dialog-open (:id item) (:title item) (:url item) (:name item))} [edit]]
       [icon-button {:on-click (fn []
                                 (when (js/confirm "Delete it?")
                                   (del-item (:id item))
                                   (s/init-items)))} [delete]]]])])
