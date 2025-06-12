(ns app.views.two-face
  (:require
    [app.logics.passkey :refer [get-passkey]]
    [app.logics.sesame :refer [decrypt-text]]
    [app.views.modal-dialog :refer [modal-dialog]]
    [cljs.core.async :refer [chan <! >!]]
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
  [v id val code]
  (go
    (reset! code (<! (decrypt-text (:v1 v) (:v2 v) (str id) val)))))

(defn hide
  [visible]
  (reset! visible false))

(defn show
  [visible modal ch id val code]
  (go
    (let [passkey (<! (get-passkey))]
      (print passkey)
      (if (empty? passkey)
        (do
          (reset! modal true)
          (decode (<! ch) id val code)
          (reset! visible true))
        (do
          (decode passkey id val code)
          (reset! visible true))))))


;; define reagent react component
(defn two-face
  [{:keys [id val]}]
  (let [visible (r/atom false)
        code (r/atom val)
        modal (r/atom false)
        ch (chan)]
    (fn [{:keys [id val]}]
      [grid {:container true :justify-content "space-between" :align-items "center"}
       (if @visible
         [grid  [typography {:variant "body1"} @code]]
         [grid  [typography {:variant "body1"} val]])
       (if @visible
         [grid {:item true :xs 1}
          [icon-button
           {:on-click #(hide visible)}
           [visibility]]]
         [grid {:item true :xs 1}
          [icon-button
           {:on-click #(show visible modal ch id val code)}
           [visibility-off]]])
       [modal-dialog {:open @modal
                      :on-close (fn [v1 v2]
                                  (go (reset! modal false)
                                      (>! ch {:v1 v1 :v2 v2})))}]])))


