(ns app.store
  (:require [reagent.core :as r]
            [app.items :refer [get-items]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def items (r/atom []))
(def open (r/atom false))

(defn init-items []
  (go (reset! items (<! (get-items)))))

