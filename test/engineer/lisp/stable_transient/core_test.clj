;; Copyright © 2017 Douglas P. Fields, Jr. All Rights Reserved.
;; Web: https://symbolics.lisp.engineer/
;; E-mail: symbolics@lisp.engineer
;; Twitter: @LispEngineer

(ns engineer.lisp.stable-transient.core-test
  (:import (engineer.lisp.stabletransient StableTransientGenerator))
  (:use [clojure.pprint])
  (:require [clojure.test :refer :all]
            [engineer.lisp.stable-transient.core :refer :all]))

;; This is currently exploring the capabilities of transients and
;; how stable-transient proxy works, and isn't actually a real test
;; suite for the time being.

(deftest test-proxy-1
  (testing "Regular transient"
    (let [t (transient {})]
      (println "\nRegular transient")
      ;; I know this isn't the right way to use a transient, but it works
      ;; due to an implementation detail
      (assoc! t :a 1)
      (assoc! t :b 2)
      ;; Cannot call keys on a transient
      #_(println "Keys of t: " (keys t))
      (println "t a: " (:a t) ", b: " (:b t) ", c: " (:c t))
      (is (not= t {:a 1 :b 2}) "transient isn't {:a 1 :b 2} even though it should be")
      (is (= {:a 1 :b 2} (persistent! t)) "persistent is equal")
    ))

  (testing "Regular transient equality"
    (let [t1 (atom (transient {}))
          t2 (atom (transient {}))]
      (println "\nRegular transient equality")
      (reset! t1 (assoc! @t1 :a 1))
      (reset! t1 (assoc! @t1 :b 2))
      (swap! t2 assoc! ,,, :a 1)
      (swap! t2 assoc! ,,, :b 2)
      (is (not= @t1 @t2) "(= t1 t2) should be true but it isn't")
      (is (= (persistent! @t1) (persistent! @t2)) "persistent versions should be equal")
    ))


  (testing "Proxy Stable Transient Works"
    (let [t (stable-transient {})]
      ;; conj! assoc! dissoc! pop! disj!
      (println "\nStable transient")
      (assoc! t :a 1)
      (assoc! t :b 2)
      (println "T should be {:a 1 :b 2} but isn't:" (= t {:a 1 :b 2}))
      (dissoc! t :b)
      ;; Regular transients don't support keys, so neither will mine
      #_(println "t's keys:" (keys t))
      (is (= {:a 1} (persistent! t)))
      (println "And now we're persistent.")
    ))

  (testing "Force Stable Transient head change"
    (let [t (stable-transient {})]
      ;; conj! assoc! dissoc! pop! disj!
      (println "\nStable transient head change")
      (assoc! t :a 1)
      (assoc! t :b 2)
      (assoc! t :c 3)
      (assoc! t :d 4)
      (assoc! t :e 5)
      (assoc! t :f 6)
      (assoc! t :g 7)
      (assoc! t :h 8)
      ;; Clojure 1.8 Maps store the first 8 keys a different way than the
      ;; later keys
      (println ">>>The head should change here.")
      (assoc! t :i 9)
      (println ">>>The head should change above.")
      (assoc! t :j 10)
      (assoc! t :k 11)
      (assoc! t :l 12)
      (println (persistent! t))
    ))
)
