(ns app.body
  (:require [reagent.core :as r]
            ;; my components
            [app.account-list :refer [account-list]]
            [app.item-edit-dialog :refer [item-edit-dialog item-edit-dialog-open]]
            ;; mui components
            [reagent-mui.material.grid :refer [grid]]
            [reagent-mui.material.app-bar :refer [app-bar]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent-mui.material.container :refer [container]]
            [reagent-mui.material.fab :refer [fab]]
            ;; icons
            [reagent-mui.icons.add :refer [add]]
            ))

(def open (r/atom false))
(def items (r/atom [{:id 0 :title "test0" :url "http://example.com" :name "Alice" :val "abcde"}
                    {:id 1 :title "test1" :url "http://example.com" :name "Bob" :val "abcde"}]))

(defn body []
  [:<>
   [app-bar {:position "static"} [toolbar "List"]]
   [container [account-list {:items @items}]]
   [fab {:variant "contained"
         :on-click #(item-edit-dialog-open)
         :style {:position "fixed" :bottom "20px" :right "20px"}} [add]]
   [item-edit-dialog]
   ])
  
