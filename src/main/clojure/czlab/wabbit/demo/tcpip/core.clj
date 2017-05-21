;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc ""
      :author "Kenneth Leung"}

  czlab.wabbit.demo.tcpip.core

  (:require [czlab.basal.log :as log]
            [czlab.wabbit.xpis :as xp]
            [czlab.basal.core :as c]
            [czlab.basal.str :as s])

  (:import [java.io DataOutputStream DataInputStream BufferedInputStream]
           [java.net Socket]
           [java.util Date]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def ^:private ^String text-msg "Hello World, time is ${TS} !")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demoClient "" [evt]
  (let [plug (xp/get-pluglet evt)
        svr (xp/get-server plug)
        tcp (xp/get-child svr :default-sample)
        s (.replace text-msg "${TS}" (str (Date.)))
        {:keys [host port]}
        (:conf @tcp)
        bits (.getBytes s "utf-8")]
    (println "TCP Client: about to send message" s)
    (with-open [soc (Socket. ^String host (int port))]
      (let [os (.getOutputStream soc)]
        (-> (DataOutputStream. os)
            (.writeInt (int (alength bits))))
        (doto os
          (.write bits)
          (.flush))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demoServer "" [evt]
  (let [dis (DataInputStream. (:sockin evt))
        clen (.readInt dis)
        bf (BufferedInputStream. (:sockin evt))
        buf (byte-array clen)]
    (.read bf buf)
    (->> (String. buf "utf-8")
         (println "Socket Server Received: "))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


