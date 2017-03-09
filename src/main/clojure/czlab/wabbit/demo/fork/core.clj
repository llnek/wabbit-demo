;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:no-doc true
      :author "Kenneth Leung"}

  czlab.wabbit.demo.fork.core

  (:require [czlab.basal.logging :as log])

  (:use [czlab.flux.wflow.core]
        [czlab.basal.core]
        [czlab.basal.str])

  (:import [czlab.wabbit.sys Execvisor]
           [czlab.flux.wflow Job Activity Workstream]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- fib "" [n]
  (if (< n 3) 1 (+ (fib (- n 2)) (fib (- n 1)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;   parent(s1) --> split&nowait
;;                  |-------------> child(s1)----> split&wait --> grand-child
;;                  |                              |                    |
;;                  |                              |<-------------------+
;;                  |                              |---> child(s2) -------> end
;;                  |
;;                  |-------> parent(s2)----> end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def ^:private a1
  #(do->nil
     (let [job %]
       (println "I am the *Parent*")
       (println "I am programmed to fork off a parallel child process, "
                "and continue my business."))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def ^:private a2
  (group<>
    #(do->nil
       (let [^Job job %]
         (println "*Child*: will create my own child (blocking)")
         (.setv job :rhs 60)
         (.setv job :lhs 5)))
    (fork<>
      :and
      #(do->nil
         (let [^Job job %]
           (println "*Child->child*: taking some time to do "
                    "this task... ( ~ 6secs)")
           (dotimes [n 7]
             (Thread/sleep 1000) (print "."))
           (println "")
           (println "*Child->child*: returning result back to *Child*.")
           (.setv job :result (* (.getv job :rhs)
                                 (.getv job :lhs)))
           (println "*Child->child*: done."))))
    #(do->nil
       (let [^Job job %]
         (println "*Child*: the result for (5 * 60) according to "
                  "my own child is = "
                  (.getv job :result))
         (println "*Child*: done.")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def ^:private a3
  #(do->nil
     (let [_ %
           b (strbf<> "*Parent*: ")]
       (println "*Parent*: after fork, continue to calculate fib(6)...")
       (dotimes [n 7]
         (when (> n 0)
           (.append b (str (fib n) " "))))
       (println (str b) "\n" "*Parent*: done."))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn demo
  "Split but no wait, parent continues" []
  (workstream<> (group<> a1 (fork<> :nil a2) a3)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


