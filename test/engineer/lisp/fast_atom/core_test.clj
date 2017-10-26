;; Copyright © 2017 Douglas P. Fields, Jr. All Rights Reserved.
;; Web: https://symbolics.lisp.engineer/
;; E-mail: symbolics@lisp.engineer
;; Twitter: @LispEngineer

;; Sorry that this is really ugly. I was iterating super fast and wanted
;; to get some efficient performance test functions without too much
;; overhead, so lots of macros.

(ns engineer.lisp.fast-atom.core-test
  (:import (engineer.lisp.fastatom UnsynchronizedAtom FastAtom))
  (:require [clojure.test :refer :all]
            [engineer.lisp.fast-atom.core :refer :all]))

(defn perf-test-1
  []
  (let [trials 100000000]
    (println "Standard atom swap!")
    (time
      (let [a (atom {})]
        (dorun
          (for [x (range trials)]
            (swap! a assoc ,,, :x x)))))

    (println "Unsynchronized atom swap!")
    (time
      (let [a (UnsynchronizedAtom. {})]
        (dorun
          (for [x (range trials)]
            (swap! a assoc ,,, :x x)))))

    (println "Standard atom reset!")
    (time
      (let [a (atom {})]
        (dorun
          (for [x (range trials)]
            (reset! a {:x x})))))

    (println "Unsynchronized atom reset!")
    (time
      (let [a (UnsynchronizedAtom. {})]
        (dorun
          (for [x (range trials)]
            (reset! a {:x x}))))))

  ;; Return test value
  true)

(defmacro trial-body
  "Makes the body of a performance trial function, looping it trials times."
  [trials trial-num v init & body]
  `(let [trials# ~trials]
     (when (pos? trials#)
       (let [~v ~init]
         (loop [~trial-num 1]
           ~@body
           (when (< ~trial-num trials#)
             (recur (inc ~trial-num))))))))

(defmacro repeat-body
  "Repeats the form num times assigning the repeat to specified sym each time.
   Basically, unrolls something.
   num must be an actual number (for now)"
  [sym num form]
  (letfn [(makeform [n]
            ;; This isn't recursive and only handles top-level replacements
            (map #(if (= % sym) n %) form))]
    (cons 'do
          (map makeform (range 1 (inc num))))))

(defmacro reset!m
  "Since repeat-body doesn't handle nested forms, this just
   wraps reset! with a flattened body after the atom."
  [atom & body]
  `(reset! ~atom ~body))

(defmacro make-perf-func
  "Creates a performance function with a given atom creator and
   atom contents updator."
  [name desc create body
   a n num] ; These are necessary symbols
  `(defn ~name [trials#]
     (println ~desc " x" 10 ", trials: " trials#)
     (time
       (trial-body trials# ~num ~a ~create
         (repeat-body ~n 10 ~body)))))

(make-perf-func std-map-swap
  "Std atom/map swap"
  (atom {})
  (swap! a assoc,,, n num)
  a n num)

(make-perf-func std-trans-swap
  "Std atom/transient swap"
  (atom (transient {}))
  (swap! a assoc!,,, n num)
  a n num)

(make-perf-func unsync-map-swap
  "Unsync atom/map swap"
  (UnsynchronizedAtom. {})
  (swap! a assoc,,, n num)
  a n num)

(make-perf-func unsync-trans-swap
  "Unsync atom/transient swap"
  (UnsynchronizedAtom. (transient {}))
  (swap! a assoc!,,, n num)
  a n num)

(make-perf-func fast-map-swap
  "Fast atom/map swap"
  (FastAtom. {})
  (swap! a assoc,,, n num)
  a n num)

(make-perf-func fast-trans-swap
  "Fast atom/transient swap"
  (FastAtom. (transient {}))
  (swap! a assoc!,,, n num)
  a n num)

(make-perf-func std-map-reset
  "Std atom/map reset"
  (atom {})
  (reset!m a assoc @a n num)
  a n num)

(make-perf-func std-trans-reset
  "Std atom/transient reset"
  (atom (transient {}))
  (reset!m a assoc! @a n num)
  a n num)

(make-perf-func unsync-map-reset
  "Unsync atom/map reset"
  (UnsynchronizedAtom. {})
  (reset!m a assoc @a n num)
  a n num)

(make-perf-func unsync-trans-reset
  "Unsync atom/transient reset"
  (UnsynchronizedAtom. (transient {}))
  (reset!m a assoc! @a n num)
  a n num)

(make-perf-func fast-map-reset
  "Fast atom/map reset"
  (FastAtom. {})
  (reset!m a assoc @a n num)
  a n num)

(make-perf-func fast-trans-reset
  "Fast atom/transient reset"
  (FastAtom. (transient {}))
  (reset!m a assoc! @a n num)
  a n num)



(defn perf-test-2
  "Run all the individual performance test functions."
  []
  (let [trials 10000000
        funcs [#'std-map-swap     #'std-trans-swap
               #'unsync-map-swap  #'unsync-trans-swap
               #'fast-map-swap    #'fast-trans-swap
               #'std-map-reset    #'std-trans-reset
               #'unsync-map-reset #'unsync-trans-reset
               #'fast-map-reset   #'fast-trans-reset]]
    (doseq [f funcs]
      (System/gc)
      (f trials)))
  true)



(deftest test-perf-1
  (testing "Performance"
    (println "\n\nTest 1 (Warm up the JVM)\n\n")
    (is (perf-test-2))
    (println "\n\nTest 2 (JVM warmed up)\n\n")
    (is (perf-test-2))))
