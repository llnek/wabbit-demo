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

  (:require [czlab.basal.process :refer [delayExec]]
            [czlab.basal.logging :as log])

  (:use [czlab.flux.wflow.core]
        [czlab.basal.core]
        [czlab.basal.str])

  (:import [java.io DataOutputStream DataInputStream BufferedInputStream]
           [czlab.flux.wflow Job Activity Workstream]
           [czlab.wabbit.plugs.io TcpMsg]
           [java.net Socket]
           [java.util Date]
           [czlab.jasal Muble]
           [czlab.wabbit.ctl Pluglet]
           [czlab.wabbit.sys Execvisor]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def ^:private ^String text-msg "Hello World, time is ${TS} !")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demoClient "" []
  ;; wait, then opens a socket and write something to server process.
  (workstream<>
    (postpone<> 3)
    #(do->nil
       (let [^TcpMsg msg (.origin ^Job %)
             ^Pluglet
             tcp (-> ^Execvisor
                     (.. msg source server)
                     (.child :default-sample))
             s (.replace text-msg "${TS}" (str (Date.)))
             {:keys [host port]}
             (.config tcp)
             bits (.getBytes s "utf-8")]
         (println "TCP Client: about to send message" s)
         (with-open [soc (Socket. ^String host (int port))]
           (let [os (.getOutputStream soc)]
             (-> (DataOutputStream. os)
                 (.writeInt (int (alength bits))))
             (doto os
               (.write bits)
               (.flush))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demoServer "" []
  (workstream<>
    #(do->nil
       (let [^Job job %
             ^TcpMsg ev (.origin job)
             dis (DataInputStream. (.sockIn ev))
             clen (.readInt dis)
             bf (BufferedInputStream. (.sockIn ev))
             buf (byte-array clen)]
         (.read bf buf)
         (.setv job :cmsg (String. buf "utf-8"))
         ;; add a delay into the workflow before next step
         (postpone<> 1.5)))
    #(do->nil
       (println "Socket Server Received: " (.getv ^Job % :cmsg)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


