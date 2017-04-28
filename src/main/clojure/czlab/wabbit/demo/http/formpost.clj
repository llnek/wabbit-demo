;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns
  ^{:no-doc true
    :author "Kenneth Leung"}

  czlab.wabbit.demo.http.formpost

  (:require [czlab.basal.logging :as log])

  (:use [czlab.wabbit.xpis]
        [czlab.convoy.upload]
        [czlab.convoy.core]
        [czlab.basal.core]
        [czlab.basal.str])

  (:import [org.apache.commons.fileupload FileItem]
           [java.io File]
           [czlab.jasal XData]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demo "" [evt res]
  (do-with
    [ch (:socket evt)]
    (let [data (:body evt)
          stuff (some-> ^XData data .content)]
      (if (satisfies? ULFormItems stuff)
        (doseq [^FileItem fi (get-all-items stuff)]
          (println "Fieldname : " (.getFieldName fi))
          (println "Name : " (.getName fi))
          (println "Formfield : " (.isFormField fi))
          (if (.isFormField fi)
            (println "Field value: " (.getString fi))
            (if-some [xs (get-field-file fi)]
              (println "Field file = "
                       (.getCanonicalPath xs)))))
         (println "Error: data is not ULFormItems."))
      ;; associate this result with the orignal event
      ;; this will trigger the http response
      (reply-result ch res))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


