;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^:no-doc
    ^{:author "Kenneth Leung"}

  czlab.wabbit.demo.http.websock

  (:require [czlab.basal.meta :as m :refer [instBytes?]]
            [czlab.wabbit.xpis :as xp]
            [czlab.basal.log :as log]
            [czlab.basal.core :as c]
            [czlab.basal.str :as s])

  (:import [czlab.jasal XData]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demo "" [evt]
  (c/do-with
    [ch (:socket evt)]
    (let [data (:body evt)
          stuff (some-> ^XData data .content)]
      (cond
        (string? stuff)
        (println "Got poked by websocket-text: " stuff)
        (m/instBytes? stuff)
        (println "Got poked by websocket-bin: len = " (alength ^bytes stuff))
        :else
        (println "Funky data from websocket????")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


