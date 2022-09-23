(ns app.items
  (:require
    [cljs.core.async :as async :refer [<! >! chan put!]]
    [indexed.db :as db])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


(defn handle-upgrade
  [e]
  (let [store (-> (db/create-version-change-event e)
                  (db/get-request)
                  (db/result)
                  (db/create-database)
                  (db/create-object-store "items" {:key-path "id" :auto-increment true}))]
    (db/create-index store "title" "title" {:unique? false})
    (db/create-index store "url" "url" {:unique? false})
    (db/create-index store "name" "name" {:unique? false})
    (db/create-index store "val" "val" {:unique? false})))


(defn open
  "DBを開く処理完了後にセットされるchanを返す"
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
        (>! ret-ch req))

    ;; チャンネル返す。これを外で<!するとこの関数の処理が実行される
    ret-ch))


(defn get-store
  "DBを開くリクエストオブジェクトを受けてストア（テーブル）を返す"
  [req]
  (-> (db/result req)
      (db/create-database)
      (db/transaction ["items"] "readwrite")
      (db/object-store "items")))


;; 公開
(defn get-items
  []
  (let [ret-ch (chan)]
    (go (-> (<! (open))
            (get-store)
            (db/get-all)
            (db/on "success" (fn [res] (put! ret-ch res)))))
    (go (js->clj (.. (<! ret-ch) -target -result)
                 :keywordize-keys true))))


(defn get-item
  [key]
  (let [ret-ch (chan)]
    (go (-> (<! (open))
            (get-store)
            (db/get key)
            (db/on "success" (fn [res] (put! ret-ch res)))))
    (go (js->clj (.. (<! ret-ch) -target -result)
                 :keywordize-keys true))))


(defn put-item
  [map]
  (let [ret-ch (chan)
        data (if (:id map) map (dissoc map :id))]
    (go (-> (<! (open))
            (get-store)
            (db/put (clj->js data))
            (db/on "success" (fn [res] (put! ret-ch res)))))
    (go (<! ret-ch))))


(defn del-item
  [key]
  (let [ret-ch (chan)]
    (go (-> (<! (open))
            (get-store)
            (db/delete key)
            (db/on "success" (fn [res] (put! ret-ch res)))))
    (go (<! ret-ch))))


