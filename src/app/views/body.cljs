(ns app.views.body
  (:require
    [app.logics.items :refer [import-items export-items]]
    [app.stores.store :as s]
    [app.views.account-list :refer [account-list]]
    [app.views.item-edit-dialog :refer [item-edit-dialog item-edit-dialog-open]]
    [cljs.core.async :refer [go <!]]
    ;; icons
    [reagent-mui.icons.add :refer [add]]
    [reagent-mui.icons.download :refer [download]]
    [reagent-mui.icons.upload :refer [upload]]
    ;; mui components
    [reagent-mui.material.app-bar :refer [app-bar]]
    [reagent-mui.material.container :refer [container]]
    [reagent-mui.material.fab :refer [fab]]
    [reagent-mui.material.icon-button :refer [icon-button]]
    [reagent-mui.material.toolbar :refer [toolbar]]
    [reagent.core :as r]))


(defn- on-change-input
  []
  (go (let [input (.getElementById js/document "input")]
        (<! (import-items (.. input -files (item 0))))
        (<! (s/init-items))
        (js/alert "Import completed"))))


(defn body
  []
  (r/create-class
    {:component-did-mount
     (fn [_]
       (s/init-items))
     :reagent-render
     (fn []
       [:<>
        [app-bar {:position "fixed"}
         [toolbar
          [:div {:style {:flex-grow "1"}}]
          [icon-button {:color "inherit"
                        :on-click #(.click (.getElementById js/document "input"))}
           [upload]]
          [icon-button {:color "inherit" :on-click #(export-items)} [download]]]]
        [container [account-list {:items @s/items}]]
        [fab {:variant "contained"
              :on-click #(item-edit-dialog-open)
              :style {:position "fixed" :bottom "20px" :right "20px"}} [add]]
        [:input {:id "input"
                 :type "file"
                 :accept ".json"
                 :style {:visibility "hidden" :width 0 :height 0}
                 :on-change #(on-change-input)}]
        [item-edit-dialog]])}))
