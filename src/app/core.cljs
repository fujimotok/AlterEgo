(ns app.core
  "This namespace contains your application and is the entrypoint for 'yarn start'."
  (:require
    [app.views.body :refer [body]]
    [reagent.core :as r]))


(defn ^:dev/after-load render
  "Render the toplevel component for this app."
  []
  (r/render [body] (.getElementById js/document "app")))


(defn ^:export main
  "Run application startup logic."
  []
  (render))
