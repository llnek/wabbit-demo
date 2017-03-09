;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^:no-doc
    ^{:author "Kenneth Leung"}

  czlab.wabbit.demo.pop3.core

  (:require [czlab.basal.logging :as log]
            [czlab.basal.process :refer [delayExec]])

  (:use [czlab.flux.wflow.core]
        [czlab.basal.core]
        [czlab.basal.str])

  (:import [javax.mail Message Message$RecipientType Multipart]
           [java.util.concurrent.atomic AtomicInteger]
           [javax.mail.internet MimeMessage]
           [czlab.flux.wflow Job Activity]
           [org.apache.commons.io IOUtils]
           [czlab.wabbit.sys Execvisor]
           [czlab.wabbit.plugs.io MailMsg]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def ^:private ^AtomicInteger gint (AtomicInteger.))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- ncount "" [] (.incrementAndGet gint))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demo "" []
  #(do->nil
     (let [^MailMsg ev (.origin ^Job %)
           ^MimeMessage msg (.message ev)
           ^Multipart p (.getContent msg)]
       (println "######################## (" (ncount) ")" )
       (print "Subj:" (.getSubject msg) "\r\n")
       (print "Fr:" (first (.getFrom msg)) "\r\n")
       (print "To:" (first (.getRecipients msg
                                           Message$RecipientType/TO)))
       (print "\r\n")
       (println (IOUtils/toString (-> (.getBodyPart p 0)
                                      (.getInputStream)) "utf-8")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn myAppMain "" []
  (System/setProperty "wabbit.mock.mail.proto" "pop3s"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


