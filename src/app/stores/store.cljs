(ns app.stores.store
  (:require
    [app.logics.items :refer [get-items]]
    [cljs.core.async :refer [<!]]
    [reagent.core :as r])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


(def items (r/atom []))
(def open (r/atom false))


(defn init-items
  []
  (go (reset! items (<! (get-items)))))
