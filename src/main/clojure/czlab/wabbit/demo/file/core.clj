;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:no-doc true
      :author "Kenneth Leung"}

  czlab.wabbit.demo.file.core

  (:require [czlab.basal.logging :as log]
            [clojure.java.io :as io])

  (:use [czlab.basal.core]
        [czlab.basal.str]
        [czlab.basal.io]
        [czlab.flux.wflow.core])

  (:import [java.util.concurrent.atomic AtomicInteger]
           [czlab.wabbit.ctl Pluglet PlugMsg]
           [czlab.wabbit.sys Execvisor]
           [czlab.flux.wflow Job]
           [czlab.wabbit.plugs.io TimerMsg FileMsg]
           [java.util Date]
           [java.io File IOException]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def ^:private gint (AtomicInteger.))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- ncount "" [] (.incrementAndGet ^AtomicInteger gint))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demoGen "" []
  #(let [^TimerMsg msg (.origin ^Job %)
         ^Pluglet
         p (-> ^Execvisor
               (.. msg source server)
               (.child :default-sample))]
     (-> (:targetFolder (.config p))
         (io/file (str "ts-" (ncount) ".txt"))
         (spitUtf8 (str "Current time is " (Date.))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demoPick "" []
  #(let [^FileMsg msg (.origin ^Job %)
         f (.file msg)]
     (println "picked up new file: " f)
     (println "content: " (slurpUtf8 f))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


