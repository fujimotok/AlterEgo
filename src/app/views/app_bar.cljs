(ns app.views.app-bar
  (:require
    [app.logics.items :refer [import-items export-items]]
    [app.logics.passkey :refer [create-passkey get-passkey]]
    [app.stores.store :as s]
    [cljs.core.async :refer [go <!]]
    ;; icons
    [reagent-mui.icons.more-vert :refer [more-vert]]
    [reagent-mui.icons.search :refer [search]]
    ;; mui components
    [reagent-mui.material.app-bar :as mui-app-bar]
    [reagent-mui.material.icon-button :refer [icon-button]]
    [reagent-mui.material.input :refer [input]]
    [reagent-mui.material.input-adornment :refer [input-adornment]]
    [reagent-mui.material.menu :refer [menu]]
    [reagent-mui.material.menu-item :refer [menu-item]]
    [reagent-mui.material.toolbar :refer [toolbar]]
    [reagent.core :as r]
    [app.views.modal-dialog :as modal]))


;; letの中でやるとうまくいかない
(def data (r/atom {:anchor-el nil :open false}))
(def show-passkey-modal (r/atom false))


(defn- on-change-input
  []
  (go (let [input (.getElementById js/document "input")]
        (<! (import-items (.. input -files (item 0))))
        (<! (s/init-items))
        (js/alert "Import completed"))))


(defn app-bar
  []
  ;; 同時に必要になるatomはまとめないと更新うまくいかない
  (let [{:keys [anchor-el open]} @data]
    [mui-app-bar/app-bar
     {:position "fixed", :color "inherit"}
     [toolbar
      [:div
       [icon-button
        {:aria-label "show more"
         :aria-controls "menu"
         :aria-haspopup true
         :on-click
         (fn [e]
           (reset! data {:anchor-el (.-currentTarget e), :open true}))}
        [more-vert]]
       [menu
        {:id "menu"
         :keep-mounted true
         :anchor-origin {:vertical "bottom", :horizontal "left"}
         :open open
         :anchor-el anchor-el
         :on-close #(reset! data {:anchor-el nil, :open false})}
        [menu-item
         {:on-click (fn []
                      (.click (.getElementById js/document "input"))
                      (reset! data {:anchor-el nil, :open false}))}
         "import"]
        [menu-item
         {:on-click (fn []
                      (export-items)
                      (reset! data {:anchor-el nil, :open false}))}
         "export"]
        [menu-item
         {:on-click (fn []
                      (reset! show-passkey-modal true)
                      (reset! data {:anchor-el nil, :open false}))}
         "passkey"]]
       [:input {:id "input"
                :type "file"
                :accept ".json"
                :style {:display "none" :width 0 :height 0}
                :on-change #(on-change-input)}]]
      [input
       {:size "small",
        :placeholder "Search...",
        :id "search",
        :disableUnderline true,
        :end-adornment
        (r/as-element
          [input-adornment {:position "end"}
           [icon-button {:color "inherit"
                         :on-click #(s/search-items @s/search-text)}
            [search]]]),
        :defaultValue @s/search-text,
        :on-change #(reset! s/search-text (.. % -target -value)),
        :on-key-down #(when (= (.. % -key) "Enter")
                         (s/search-items @s/search-text)),
        :style {:flex-grow "1"}}]]
      ;; dialog
      (when @show-passkey-modal
        [modal/modal-dialog {:open @show-passkey-modal
                             :on-close (fn [v1 v2]
                                         (reset! show-passkey-modal false)
                                         (create-passkey v1 v2))}])
      ]))

