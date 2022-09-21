(ns app.account-list
  (:require [reagent.core :as r]
            [app.item-edit-dialog :refer [item-edit-dialog-open]]
            ;; material-ui react components
            [reagent-mui.material.grid :refer [grid]]
            [reagent-mui.material.list :refer [list]]
            [reagent-mui.material.accordion :refer [accordion]]
            [reagent-mui.material.accordion-summary :refer [accordion-summary]]
            [reagent-mui.material.accordion-details :refer [accordion-details]]
            [reagent-mui.material.accordion-actions :refer [accordion-actions]]
            [reagent-mui.material.icon-button :refer [icon-button]]
            ;; icons
            [reagent-mui.icons.expand-more :refer [expand-more]]
            [reagent-mui.icons.link :refer [link]]
            [reagent-mui.icons.person :refer [person]]
            [reagent-mui.icons.abc :refer [abc]]
            [reagent-mui.icons.edit :refer [edit]]
            [reagent-mui.icons.delete-icon :refer [delete]]
            ))

;; define reagent react component
(defn account-list [{:keys [items]}]
  [list
   (for [item items]
     ^{:key (:id item)}
     [accordion 
      [accordion-summary
       {:expandIcon (r/as-element [expand-more])}
       (str "#" (:id item) ": " (:title item))]
      [accordion-details
       [grid {:container true}
        [grid {:item true :xs 1} [link]]
        [grid {:item true :xs 11} (:url item)]
        [grid {:item true :xs 1} [person]]
        [grid {:item true :xs 11} (:name item)]
        [grid {:item true :xs 1} [abc]]
        [grid {:item true :xs 11} (:val item)]
        ]]
      [accordion-actions
       [icon-button {:on-click #(item-edit-dialog-open (:id item) (:title item) (:url item) (:name item) (:val item))} [edit]]
       [icon-button [delete]]]
      ])
   ])
  
      
   
