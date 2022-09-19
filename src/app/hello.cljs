(ns app.hello
  (:require [reagent.core :as r]
            [reagent-mui.material.button :refer [button]]))

(defn click-counter [click-count]
  [:div
   "The atom " [:code "click-count"] " has value: "
   @click-count ". "
   [button {:variant "contained" :on-click #(swap! click-count inc)} "Click me!"]])

(def click-count (r/atom 0))

(defn hello []
  [:<>
   [:p "Hello, AccountManager is running!"]
   [:p "Here's an example of using a component with state:"]
   [click-counter click-count]])
