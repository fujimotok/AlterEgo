(ns app.views.body
  (:require
    [app.stores.store :as s]
    [app.views.account-list :refer [account-list]]
    [app.views.app-bar :refer [app-bar]]
    [app.views.item-edit-dialog :refer [item-edit-dialog item-edit-dialog-open]]
    ;; icons
    [reagent-mui.icons.add :refer [add]]
    ;; mui components
    [reagent-mui.material.container :refer [container]]
    [reagent-mui.material.fab :refer [fab]]
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
        [app-bar]
        [container [account-list {:items @s/items}]]
        [fab {:variant "contained"
              :on-click #(item-edit-dialog-open)
              :style {:position "fixed" :bottom "20px" :right "20px"}} [add]]
        [item-edit-dialog]])}))
