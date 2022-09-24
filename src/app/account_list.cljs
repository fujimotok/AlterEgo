(ns app.account-list
  (:require
    [app.item-edit-dialog :refer [item-edit-dialog-open]]
    ;; model
    [app.items :refer [del-item]]
    [app.store :as s]
    [app.two-face :refer [two-face]]
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
    [reagent-mui.material.grid :refer [grid]]
    [reagent-mui.material.icon-button :refer [icon-button]]
    [reagent-mui.material.list :refer [list]]
    [reagent.core :as r]))


;; define reagent react component
(defn account-list
  [{:keys [items]}]
  [list {:style {:margin-top "80px" :margin-bottom "80px"}}
   (for [item items]
     ^{:key (:id item)}
     [accordion
      [accordion-summary
       {:expandIcon (r/as-element [expand-more])}
       (:title item)]
      [accordion-details
       [grid {:container true}
        [grid {:item true :xs 1} [link]]
        [grid {:item true :xs 11} [:a {:href (:url item), :target "_blank"} (:url item)]]
        [grid {:item true :xs 1} [person]]
        [grid {:item true :xs 11} (:name item)]
        [grid {:item true :xs 1} [abc]]
        [grid {:item true :xs 11} [two-face {:id (:id item), :val (:val item)}]]]]
      [accordion-actions
       [icon-button {:on-click #(item-edit-dialog-open (:id item) (:title item) (:url item) (:name item))} [edit]]
       [icon-button {:on-click (fn []
                                 (when (js/confirm "Delete it?")
                                   (del-item (:id item))
                                   (s/init-items)))} [delete]]]])])
