;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc ""
      :author "Kenneth Leung"}

  czlab.wabbit.demo.mvc.core

  (:require [czlab.wabbit.plugs.mvc :as mvc]
            [czlab.basal.log :as log]
            [czlab.wabbit.xpis :as xp]
            [czlab.wabbit.base :as b]
            [czlab.convoy.core :as cc]
            [czlab.basal.core :as c]
            [czlab.basal.str :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- ftlContext "" []
  {:landing {:title_line "Sample Web App"
             :title_2 "Demo Skaro"
             :tagline "Say something" }
   :about {:title "About Skaro demo" }
   :services {}
   :contact {:email "a@b.com"}
   :description "Default Skaro web app."
   :encoding "utf-8"
   :title "Skaro|Sample"})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn handler "" [evt res]
  (c/do-with
    [ch (:socket evt)]
    (let [ri (get-in evt [:route :info])
          tpl (:template ri)
          plug (xp/get-pluglet evt)
          co (xp/get-server plug)
          {:keys [data ctype]}
          (mvc/loadTemplate co tpl (ftlContext))]
      (->> (-> (cc/set-res-header res "content-type" ctype)
               (assoc :body data))
           cc/reply-result ))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn myAppMain "" [_] (log/info "My AppMain called!"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


