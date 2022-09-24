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

    ;; open実行ブロック
    (-> req
        (db/on "error" (fn [e] (put! error-ch e)))
        (db/on "blocked" (fn [e] (put! error-ch e)))
        (db/on "upgradeneeded" handle-upgrade)
        (db/on "success" (fn [e] (put! success-ch e))))

    ;; open実行後の処理待ちブロック作成
    (go (println "error: " (<! error-ch))
        (>! ret-ch nil))
    (go (<! success-ch)
        (>! ret-ch (db/create-database (db/result req))))

    ;; チャンネル返す。これを外で<!するとこの関数の処理が実行される
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
