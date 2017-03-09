;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^:no-doc
    ^{:author "Kenneth Leung"}

  czlab.wabbit.demo.http.core

  (:require [czlab.basal.process :refer [delayExec]]
            [czlab.basal.logging :as log])

  (:use [czlab.flux.wflow.core]
        [czlab.convoy.net.core]
        [czlab.basal.core]
        [czlab.basal.str])

  (:import [czlab.convoy.net HttpResult]
           [czlab.flux.wflow Job Activity]
           [czlab.wabbit.plugs.io HttpMsg]
           [czlab.wabbit.sys Execvisor]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def ^:private fx-str
  (str "<?xml version = \"1.0\" encoding = \"utf-8\"?>"
       "<hello xmlns=\"http://simple/\">"
       "<world>"
       "  Holy Batman!"
       "</world>"
       "</hello>"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demo "" []
  #(do->nil
     (let [^HttpMsg ev (.origin ^Job %)
           res (httpResult<> ev)]
       ;; construct a simple html page back to caller
       ;; by wrapping it into a stream data object
       (doto res
         (.setContentType "text/xml")
         (.setContent fx-str))
       ;; associate this result with the orignal event
       ;; this will trigger the http response
       (replyResult res))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF

