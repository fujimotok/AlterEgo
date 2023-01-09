(ns app.views.modal-dialog
  (:require
    ;; material-ui react components
    [reagent-mui.material.button :refer [button]]
    [reagent-mui.material.dialog :refer [dialog]]
    [reagent-mui.material.dialog-actions :refer [dialog-actions]]
    [reagent-mui.material.dialog-content :refer [dialog-content]]
    [reagent-mui.material.dialog-title :refer [dialog-title]]
    [reagent-mui.material.text-field :refer [text-field]]
    [reagent.core :as r]))


;; define reagent react component
(defn modal-dialog
  [{:keys [open on-close]}]
  (let [v1 (r/atom "")
        v2 (r/atom "")]
    (fn [{:keys [open on-close]}]
      [dialog
       {:full-width true
        :open open
        :on-close (fn []
                    (on-close "" "")
                    (reset! v1 "")
                    (reset! v2 ""))}
       [dialog-title "Input"]
       [dialog-content
        [text-field {:id "v1"
                     :auto-focus true
                     :variant "standard"
                     :full-width true
                     :type "password"
                     :value @v1
                     :on-key-down (fn [e]
                                    (when (= "Enter" (.-key e))
                                      (.focus (.getElementById js/document "v2"))))
                     :on-change (fn [e] (reset! v1 (.. e -target -value)))}]
        [text-field {:id "v2"
                     :variant "standard"
                     :full-width true
                     :type "password"
                     :value @v2
                     :on-key-down (fn [e]
                                    (when (= "Enter" (.-key e))
                                      (.click (.getElementById js/document "ok"))))
                     :on-change (fn [e] (reset! v2 (.. e -target -value)))}]]
       [dialog-actions
        [button {:id "ok"
                 :variant "contained"
                 :on-click (fn []
                             (on-close @v1 @v2)
                             (reset! v1 "")
                             (reset! v2 ""))}
         "ok"]]])))


