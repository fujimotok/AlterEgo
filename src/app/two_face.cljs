(ns app.two-face
  (:require
    [app.sesame :refer [decrypt-text]]
    [cljs.core.async :refer [<!]]
    ;; icons
    [reagent-mui.icons.visibility :refer [visibility]]
    [reagent-mui.icons.visibility-off :refer [visibility-off]]
    ;; material-ui react components
    [reagent-mui.material.grid :refer [grid]]
    [reagent-mui.material.icon-button :refer [icon-button]]
    [reagent-mui.material.typography :refer [typography]]
    [reagent.core :as r])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


(defn decode
  [id val code]
  (go
    (reset! code (<! (decrypt-text (js/prompt "phrase1") (js/prompt "phrase2") (str id) val)))))


;; define reagent react component
(defn two-face
  [{:keys [id val]}]
  (let [visible (r/atom false)
        code (r/atom val)]
    (fn [{:keys [id val]}]
      [grid {:container true :justify-content "space-between" :align-items "center"}
       (if @visible
         [grid  [typography {:variant "body1"} @code]]
         [grid  [typography {:variant "body1"} val]])
       (if @visible
         [grid {:item true :xs 1}
          [icon-button {:on-click #(reset! visible false)} [visibility]]]
         [grid {:item true :xs 1}
          [icon-button {:on-click (fn []
                                    (decode id val code)
                                    (reset! visible true))} [visibility-off]]])])))


