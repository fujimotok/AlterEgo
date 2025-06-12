(ns app.logics.passkey
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan put!]]))


(defn create-passkey [v1 v2]
  (try
    (let [userId (.encode (js/TextEncoder.) (str v1 " " v2))
          publicKeyCredentialCreationOptions
          #js {:publicKey
               #js {:challenge (.getRandomValues js/window.crypto (js/Uint8Array. 32))
                    :rp #js {:name "alter-ego"}
                    :user #js {:id userId
                                :name "alter-ego"
                                :displayName "alter-ego"}
                    :pubKeyCredParams #js [#js {:type "public-key" :alg -7}]
                    :authenticatorSelection #js {:residentKey "required"}
                    :timeout 3000}}]
      (.create js/navigator.credentials publicKeyCredentialCreationOptions)
      (print "Passkey Create Success"))
    (catch js/Error e
      (print (str "Passkey Create Failed " (.-message e))))))


(defn get-passkey
  []
  (let [publicKeyCredentialRequestOptions
        #js {:publicKey
             #js {:challenge (.getRandomValues js/window.crypto (js/Uint8Array. 32))
                  :allowCredentials #js []}}
        promise (.get js/navigator.credentials publicKeyCredentialRequestOptions)
        ch (chan)]
    (.then promise
           (fn [^js credential]
             (let [user-id (-> (js/TextDecoder. "utf-8")
                               (.decode (.. credential -response -userHandle)))
                   [v1 v2] (clojure.string/split user-id #" ")]
               (go
                 (put! ch {:v1 v1 :v2 v2})))))
    (.catch promise
            (fn [e]
              (println (str "Passkey 取得失敗：" (.-message e)))
              (go
                (put! ch '()))))
    ch))
