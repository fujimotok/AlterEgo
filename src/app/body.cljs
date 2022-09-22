(ns app.body
  (:require [reagent.core :as r]
            ;; my components
            [app.items :refer [get-items]]
            [app.account-list :refer [account-list]]
            [app.item-edit-dialog :refer [item-edit-dialog item-edit-dialog-open]]
            ;; mui components
            [reagent-mui.material.grid :refer [grid]]
            [reagent-mui.material.app-bar :refer [app-bar]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent-mui.material.container :refer [container]]
            [reagent-mui.material.fab :refer [fab]]
            ;; icons
            [reagent-mui.icons.add :refer [add]])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))

(def open (r/atom false))
(def items (r/atom []))

(defn init-items []
  (go (reset! items (<! (get-items)))))

(defn body []
  (r/create-class
   {:component-did-mount
     (fn [comp]
      (init-items))
    :reagent-render
    (fn []
      [:<>
       [app-bar {:position "fixed"} [toolbar "List"]]
       [container [account-list {:items @items}]]
       [fab {:variant "contained"
             :on-click #(item-edit-dialog-open)
             :style {:position "fixed" :bottom "20px" :right "20px"}} [add]]
       [item-edit-dialog]
       ])
    }))
  
