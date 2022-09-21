(ns app.items
  (:require [indexed.db :as db]))

(defn handle-upgrade [e]
  (let [store (-> (db/create-version-change-event e)
                  (db/get-request)
                  (db/result)
                  (db/create-database)
                  (db/create-object-store "items" {:key-path "id" :auto-increment true}))]
    (db/create-index store "title" "title" {:unique? false})
    (db/create-index store "url" "url" {:unique? false})
    (db/create-index store "name" "name" {:unique? false})
    (db/create-index store "val" "val" {:unique? false})))

(defn put-item [open-req json]
  (-> (db/result open-req) 
      (db/create-database)
      (db/transaction ["items"] "readwrite")
      (db/object-store "items")
      (db/put json)
      (db/on "success" #(.log js/console "success"))
      (db/on "complete" #(.log js/console "complete"))))

(defn save-item [json]
  (let [open-req (db/open "AlterEgo" 1)]
    (-> open-req
        (db/on "error" #(.log js/console "error"))
        (db/on "blocked" #(.log js/console "blocked"))
        (db/on "upgradeneeded" handle-upgrade)
        (db/on "success" (fn [](put-item open-req json))))))
  
(defn get-item [open-req key]
  (-> (db/result open-req) 
      (db/create-database)
      (db/transaction ["items"] "readwrite")
      (db/object-store "items")
      (db/get key)
      (db/on "success" (fn [res] (.log js/console res)))
      (db/on "complete" (fn [res] (.log js/console res)))))

(defn load-item [key]
  (let [open-req (db/open "AlterEgo" 1)]
    (-> open-req
        (db/on "error" #(.log js/console "error"))
        (db/on "blocked" #(.log js/console "blocked"))
        (db/on "upgradeneeded" handle-upgrade)
        (db/on "success" (fn [](get-item open-req key))))))

(defn get-all [open-req]
  (-> (db/result open-req) 
      (db/create-database)
      (db/transaction ["items"] "readwrite")
      (db/object-store "items")
      (db/get-all)
      (db/on "success" (fn [res] (.log js/console res) (.. res -target -result)))
      (db/on "complete" (fn [res] (.log js/console res)))))

(defn load-all []
  (let [open-req (db/open "AlterEgo" 1)]
    (-> open-req
        (db/on "error" #(.log js/console "error"))
        (db/on "blocked" #(.log js/console "blocked"))
        (db/on "upgradeneeded" handle-upgrade)
        (db/on "success" (fn [](get-all open-req))))))

(save-item #js {:title "test1" :url "http://example.com" :name "Alice" :val "abcde" :id 2})
(load-item 1)
(load-all)
