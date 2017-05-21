;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc ""
      :author "Kenneth Leung"}

  czlab.wabbit.demo.flows.core

  (:require [czlab.wabbit.xpis :as xp]
            [czlab.flux.wflow :as wf]
            [czlab.basal.core :as c]
            [czlab.basal.str :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;; What this example demostrates is a webservice which takes in some user info, authenticate the
;; user, then exec some EC2 operations such as granting permission to access an AMI, and
;; permission to access/snapshot a given volume.  When all is done, a reply will be sent back
;; to the user.
;;
;; This flow showcases the use of conditional activities such a choices &amp; decision.
;; Shows how to loop using wloop, and how to use fork &amp; join.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- perfAuthMtd "" [t]
  (case t
    "facebook" #(c/do->nil % (println "-> use facebook"))
    "google+" #(c/do->nil % (println "-> use google+"))
    "openid" #(c/do->nil % (println "-> use open-id"))
    #(c/do->nil % (println "-> use internal db"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;step1. choose a method to authenticate the user
;;here, we'll use choice<> to pick which method
(defn- authUser "" []
  ;; hard code to use facebook in this example, but you
  ;; could check some data from the job,
  ;; such as URI/Query params
  ;; and decide on which value to switch on
  (wf/choice<>
    #(let [_ %]
       (println "step(1): choose an auth-method") "facebook")
    (perfAuthMtd "db")
    "facebook"  (perfAuthMtd "facebook")
    "google+" (perfAuthMtd "google+")
    "openid" (perfAuthMtd "openid")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;step2
(def ^:private GetProfile
  #(c/do->nil % (println "step(2): get user profile\n" "->user is superuser")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;step3 we are going to dummy up a retry of 2 times to simulate network/operation
;;issues encountered with EC2 while trying to grant permission
;;so here , we are using a wloop to do that
(def ^:private provAmi
  (wf/wloop<>
    #(let [job %
           v (:ami_count @job)
           c (if (some? v) (inc v) 0)]
       (c/alter-atomic job assoc :ami_count c)
       (< c 3))
    #(c/do->nil
       (let [job %
             v (:ami_count @job)
             c (if (some? v) v 0)]
         (if (== 2 c)
           (println "step(3): granted permission for user "
                    "to launch this ami(id)")
           (println "step(3): failed to contact "
                    "ami- server, will retry again (" c ")"))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;step3'. we are going to dummy up a retry of 2 times to simulate network/operation
;;issues encountered with EC2 while trying to grant volume permission
;;so here , we are using a wloop to do that
(def ^:private provVol
  (wf/wloop<>
    #(let [job %
           v (:vol_count @job)
           c (if (some? v) (inc v) 0)]
       (c/alter-atomic job assoc :vol_count c)
       (< c 3))
    #(c/do->nil
       (let [job %
             v (:vol_count @job)
             c (if (some? v) v 0)]
         (if (== c 2)
           (println "step(3'): granted permission for user "
                    "to access/snapshot this volume(id)")
           (println "step(3'): failed to contact vol- server, "
                    "will retry again (" c ")"))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;step4. pretend to write stuff to db. again, we are going to dummy up the case
;;where the db write fails a couple of times
;;so again , we are using a wloop to do that
(def ^:private saveSdb
  (wf/wloop<>
    #(let [job %
           v (:wdb_count @job)
           c (if (some? v) (inc v) 0)]
          (c/alter-atomic job assoc :wdb_count c)
          (< c 3))
    #(c/do->nil
       (let [job %
             v (:wdb_count @job)
             c (if (some? v) v 0)]
         (if (== c 2)
           (println "step(4): wrote stuff to database successfully")
           (println "step(4): failed to contact db- server, "
                    "will retry again (" c ")"))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;this is the step where it will do the provisioning of the AMI and the EBS volume
;;in parallel.  To do that, we use a split-we want to fork off both tasks in parallel.  Since
;;we don't want to continue until both provisioning tasks are done. we use a AndJoin to hold/freeze
;;the workflow
(def ^:private Provision
  (wf/group<> (wf/fork<> :and provAmi provVol) saveSdb))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; this is the final step, after all the work are done, reply back to the caller.
;; like, returning a 200-OK
(def ^:private ReplyUser
  #(c/do->nil
     (let [job %]
       (println "step(5): we'd probably return a 200 OK "
                "back to caller here"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def ^:private ErrorUser
  #(c/do->nil
     (let [job %]
       (println "step(5): we'd probably return a 200 OK "
                "but with errors"))))

;; do a final test to see what sort of response should we send back to the user.
(def ^:private FinalTest
  (wf/decision<> #(c/do->true %) ReplyUser ErrorUser))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demo "" [evt]
  ;; this workflow is a small (4 step) workflow, with the 3rd step (Provision) being
  ;; a split, which forks off more steps in parallel.
  (let [p (xp/get-pluglet evt)
        s (xp/get-server p)
        c (xp/get-scheduler s)
        w (wf/workstream<>
            (wf/group<> (authUser)
                        GetProfile Provision FinalTest))
        j (wf/job<> c w evt)]
    (wf/exec-with w j)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


