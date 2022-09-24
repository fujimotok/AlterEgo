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
      [grid {:container true}
       (if @visible
         [grid {:item true :xs 11} @code]
         [grid {:item true :xs 11} val])
       (if @visible
         [grid {:item true :xs 1}
          [icon-button {:on-click #(reset! visible false)} [visibility]]]
         [grid {:item true :xs 1}
          [icon-button {:on-click (fn []
                                    (decode id val code)
                                    (reset! visible true))} [visibility-off]]])])))


