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

  (:require [czlab.basal.logging :as log])

  (:use [czlab.wabbit.plugs.io.http]
        [czlab.wabbit.plugs.io.mvc]
        [czlab.wabbit.base.core]
        [czlab.convoy.net.core]
        [czlab.basal.core]
        [czlab.basal.str]
        [czlab.flux.wflow.core])

  (:import [czlab.flux.wflow Job Activity Workstream]
           [czlab.convoy.net HttpResult RouteInfo]
           [czlab.wabbit.plugs.io HttpMsg]
           [czlab.wabbit.sys Execvisor]))

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
(defn handler "" []
  (workstream<>
    #(do->nil
       (let [^HttpMsg evt (.origin ^Job %)
             ri (get-in (.gist evt)
                        [:route :info])
             tpl (some-> ^RouteInfo ri
                         .template)
             co (.. evt source)
             {:keys [data ctype]}
             (loadTemplate co tpl (ftlContext))
             res (httpResult<> evt)]
         (.setContentType res ctype)
         (.setContent res data)
         (replyResult res)))
    :catch
    (fn [_]
      (log/info "Oops, I got an error!"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn myAppMain "" [] (log/info "My AppMain called!"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


