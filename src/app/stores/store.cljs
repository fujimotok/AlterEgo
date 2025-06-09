(ns app.stores.store
  (:require
    [app.logics.items :refer [get-items]]
    [clojure.string :as str]
    [cljs.core.async :refer [<!]]
    [reagent.core :as r])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


(def items (r/atom []))
(def cache-items (r/atom []))
(def open (r/atom false))
(def search-text (r/atom ""))


(defn init-items
  []
  (go (reset! items (<! (get-items)))
      (reset! cache-items @items)))


(defn search-items
  [text]
  (->> @cache-items
       (filter (fn [item]
                 (str/includes? (:title item) text)))
       (reset! items)))
