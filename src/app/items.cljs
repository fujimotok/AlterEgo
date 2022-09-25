(ns app.items
  (:require
    [cljs.core.async :as async :refer [<! >! chan put!]]
    [indexed.db :as db])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


;;
;; private
;;

(defn- handle-upgrade
  [e]
  (let [db (-> (db/create-version-change-event e)
               (db/get-request)
               (db/result)
               (db/create-database))
        items (db/create-object-store db "items" {:key-path "id" :auto-increment true})]
    (db/create-index items "title" "title" {:unique? false})
    (db/create-index items "url" "url" {:unique? false})
    (db/create-index items "name" "name" {:unique? false})
    (db/create-index items "val" "val" {:unique? false})))


(defn- open
  "[o] IDBDatabase"
  []
  (let [error-ch (chan)
        success-ch (chan)
        ret-ch (chan)
        req (db/open "AlterEgo" 1)]

    (-> req
        (db/on "error" (fn [e] (put! error-ch e)))
        (db/on "blocked" (fn [e] (put! error-ch e)))
        (db/on "upgradeneeded" handle-upgrade)
        (db/on "success" (fn [e] (put! success-ch e))))

    (go (println "error: " (<! error-ch))
        (>! ret-ch nil))
    (go (<! success-ch)
        (>! ret-ch (db/create-database (db/result req))))

    ret-ch))


(defn- get-store
  "
  [i] IDBDatabase
  [o] IDBDatastore
  "
  [db]
  (-> db
      (db/transaction ["items"] "readwrite")
      (db/object-store "items")))


;;
;; Public
;;

(defn get-items
  []
  (let [ret-ch (chan)]
    (go (let [db (<! (open))]
          (-> (get-store db)
              (db/get-all)
              (db/on "success"
                     (fn [res]
                       (put! ret-ch res)
                       (db/close db))))))
    (go (js->clj (.. (<! ret-ch) -target -result)
                 :keywordize-keys true))))


(defn get-item
  [key]
  (let [ret-ch (chan)]
    (go (let [db (<! (open))]
          (-> (get-store db)
              (db/get key)
              (db/on "success"
                     (fn [res]
                       (put! ret-ch res)
                       (db/close db))))))
    (go (js->clj (.. (<! ret-ch) -target -result)
                 :keywordize-keys true))))


(defn put-item
  [map]
  (let [ret-ch (chan)
        data (if (:id map) map (dissoc map :id))]
    (go (let [db (<! (open))]
          (-> (get-store db)
              (db/put (clj->js data))
              (db/on "success"
                     (fn [res]
                       (put! ret-ch res)
                       (db/close db))))))
    (go (<! ret-ch))))


(defn del-item
  [key]
  (let [ret-ch (chan)]
    (go (let [db (<! (open))]
          (-> (get-store db)
              (db/delete key)
              (db/on "success"
                     (fn [res]
                       (put! ret-ch res)
                       (db/close db))))))
    (go (<! ret-ch))))


(defn export-items
  []
  (go
    (let [json (->> (<! (get-items))
                    (clj->js)
                    (.stringify js/JSON))
          blob (new js/Blob [json] #js {:type "application/json"})
          link (.createElement js/document "a")]
      (set! (.-href link) (.createObjectURL js/URL blob))
      (set! (.-download link) "alter-ego.json")
      (.click link))))


(defn import-items
  []
  (let [file (chan)
        json (chan)
        input (.createElement js/document "input")
        reader (new js/FileReader)]
    (set! (.-type input) "file")
    (set! (.-accept input) "application/json")
    (set! (.-onchange input) (fn [] (go (>! file (.. input -files (item 0))))))

    (set! (.-onloadend reader) (fn [e] (go (>! json (.parse js/JSON (.. e -target -result))))))
    (.click input)
    (go
      (.readAsText reader (<! file)))
    (go
      (let [items (js->clj (<! json))]
        (doseq [item items]
          (put-item item))))))
