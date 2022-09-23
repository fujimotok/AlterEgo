(ns app.body
  (:require
    [app.account-list :refer [account-list]]
    [app.item-edit-dialog :refer [item-edit-dialog item-edit-dialog-open]]
    ;; my model
    [app.store :as s]
    ;; icons
    [reagent-mui.icons.add :refer [add]]
    [reagent-mui.material.app-bar :refer [app-bar]]
    [reagent-mui.material.container :refer [container]]
    [reagent-mui.material.fab :refer [fab]]
    ;; mui components
    [reagent-mui.material.toolbar :refer [toolbar]]
    [reagent.core :as r]))


(defn body
  []
  (r/create-class
    {:component-did-mount
     (fn [_]
       (s/init-items))
     :reagent-render
     (fn []
       [:<>
        [app-bar {:position "fixed"} [toolbar "List"]]
        [container [account-list {:items @s/items}]]
        [fab {:variant "contained"
              :on-click #(item-edit-dialog-open)
              :style {:position "fixed" :bottom "20px" :right "20px"}} [add]]
        [item-edit-dialog]])}))
