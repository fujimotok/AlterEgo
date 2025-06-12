(ns app.logics.sesame
  (:require
    [cljs.core.async :refer [go chan <! >!]]
    [clojure.string :refer [join]]))


;;
;; Web API async wrapper
;;

(defn- digest
  "
  [i] Uint8Array (val): value to be hashed
  [i] strint (algo): hash algorithm
  [o] chan<ArrayBuffer>
  "
  [val algo]
  (let [ch (chan)]
    (.then
      (.digest (.. js/crypto -subtle)
               (clj->js {:name algo})
               val)
      (fn [res] (go (>! ch res))))
    ch))


(defn- import-key
  "
  [i] ArrayBuffer (key): key data
  [i] string (format): 'raw' 'pkcs8' 'spki' 'jwk'
  [i] obj (algo): 'Pbkdf2Param' etc...
  [i] bool (extract): Can be able to export the key?
  [i] array (usages): 'encrypt' 'decrypt' 'sign' 'verify' etc...
  [o] chan<CryptKey>
  "
  [key format algo extract usages]
  (let [ch (chan)]
    (.then
      (.importKey (.. js/crypto -subtle)
                  format
                  key
                  (clj->js algo)
                  extract
                  usages)
      (fn [res] (go (>! ch res))))
    ch))


(defn- derive-key
  "
  [i] CryptKey (key): key data
  [i] object (algo): Pdkdf2Param etc...
  [i] object (derive-algo): AesKeyGenParam etc...
  [i] bool (extract): Can be able to export the key?
  [i] array (usages): 'encrypt' 'decrypt' 'sign' 'verify' etc...
  [o] chan<CryptKey>
  "
  [key algo derive-algo extract usages]
  (let [ch (chan)]
    (.then
      (.deriveKey (.. js/crypto -subtle)
                  (clj->js algo)
                  key
                  (clj->js derive-algo)
                  extract
                  usages)
      (fn [res] (go (>! ch res))))
    ch))


(defn- encrypt
  "
  [i] CryptKey (key): key data
  [i] object (algo): AesGcmParams etc...
  [i] ArrayBuffer (data): 
  [o] chan<ArrayBuffer>
  "
  [key algo data]
  (let [ch (chan)]
    (.then
      (.encrypt (.. js/crypto -subtle)
                (clj->js algo)
                key
                data)
      (fn [res] (go (>! ch res))))
    ch))


(defn- decrypt
  "
  [i] CryptKey (key): key data
  [i] object (algo): AesGcmParams etc...
  [i] ArrayBuffer (data): 
  [o] chan<ArrayBuffer>
  "
  [key algo data]
  (let [ch (chan)]
    (.then
      (.decrypt (.. js/crypto -subtle)
                (clj->js algo)
                key
                data)
      (fn [res] (go (>! ch res))))
    ch))


;;
;; Buffer util
;;

(defn- str-2-Uint8Array
  [str]
  (.encode (new js/TextEncoder) str))


(defn- ArrayBuffer-2-str
  [ab]
  (.decode (new js/TextDecoder) (new js/Uint8Array ab)))


(defn- ArrayBuffer-2-Base64
  [ab]
  (-> (map #(char %) (vec (new js/Uint8Array ab)))
      (join)
      (js/btoa)))


(defn- Base64-2-ArrayBuffer
  [str]
  (.from js/Uint8Array
         (.split (js/atob str) "")
         #(.charCodeAt % 0)))


(defn- gen-key
  "[o] chan<CryptKey>"
  [v1 v2]
  (let [ch (chan)]
    (go
      (->>
        (->
          (import-key (<! (digest (str-2-Uint8Array v1) "SHA-256"))
                      "raw"
                      #js {:name "PBKDF2"}
                      false
                      ["deriveKey"])
          (<!)
          (derive-key (clj->js {:name "PBKDF2",
                                :salt (<! (digest (str-2-Uint8Array v2) "SHA-256")),
                                :iterations 100000,
                                :hash "SHA-256"})
                      #js {:name "AES-GCM", :length 256}
                      false
                      ["encrypt", "decrypt"])
          (<!))
        (>! ch)))
    ch))


;;
;; Public
;;

(defn encrypt-text
  "[o] chan<ArrayBuffer>"
  [v1 v2 i t]
  (let [ch (chan)]
    (go
      (->>
        (encrypt
          (<! (gen-key v1 v2))
          (clj->js {:name "AES-GCM",
                    :iv (<! (digest (str-2-Uint8Array i) "SHA-256"))})
          (str-2-Uint8Array t))
        (<!)
        (ArrayBuffer-2-Base64)
        (>! ch)))
    ch))


(defn decrypt-text
  "[o] chan<ArrayBuffer>"
  [v1 v2 i t]
  (let [ch (chan)]
    (go
      (->>
        (decrypt
          (<! (gen-key v1 v2))
          (clj->js {:name "AES-GCM",
                    :iv (<! (digest (str-2-Uint8Array i) "SHA-256"))})
          (Base64-2-ArrayBuffer t))
        (<!)
        (ArrayBuffer-2-str)
        (>! ch)))
    ch))

