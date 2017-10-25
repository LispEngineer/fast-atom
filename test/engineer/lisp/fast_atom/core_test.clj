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
  [trials trial-num v init & body]
  `(let [trials# ~trials]
     (when (pos? trials#)
       (let [~v ~init]
         (loop [~trial-num 1]
           ~@body
           (when (< ~trial-num trials#)
             (recur (inc ~trial-num))))))))

(defmacro repeat-body
  "num must be an actual number (for now)"
  [sym num form]
  (letfn [(makeform [n]
            ;; This isn't recursive and only handles top-level replacements
            (map #(if (= % sym) n %) form))]
    (cons 'do
          (map makeform (range 1 (inc num))))))

(defmacro reset!m
  [atom & body]
  `(reset! ~atom ~body))

(defn perf-test-2
  []
  (let [trials 10000000]
    (System/gc)
    (println "Standard atom/map swap x10, trials: " trials)
    (time
      (trial-body trials num a (atom {})
        (repeat-body n 10
          (swap! a assoc ,,, n num))))

    (System/gc)
    (println "Standard atom/transient map swap x10, trials: " trials)
    (time
      (trial-body trials num a (atom (transient {}))
        (repeat-body n 10
          (swap! a assoc! ,,, n num))))

    (System/gc)
    (println "Unsynchronized atom/map swap x10, trials: " trials)
    (time
      (trial-body trials num a (UnsynchronizedAtom. {})
        (repeat-body n 10
          (swap! a assoc ,,, n num))))

    (System/gc)
    (println "Unsynchronized atom/transient map swap x10, trials: " trials)
    (time
      (trial-body trials num a (UnsynchronizedAtom. (transient {}))
        (repeat-body n 10
          (swap! a assoc! ,,, n num))))

    (System/gc)
    (println "Fast atom/map swap x10, trials: " trials)
    (time
      (trial-body trials num a (FastAtom. {})
        (repeat-body n 10
          (swap! a assoc ,,, n num))))

    (System/gc)
    (println "Fast atom/transient map swap x10, trials: " trials)
    (time
      (trial-body trials num a (FastAtom. (transient {}))
        (repeat-body n 10
          (swap! a assoc! ,,, n num))))

    (println)
    (System/gc)
    (println "Standard atom/map reset x10, trials: " trials)
    (time
      (trial-body trials num a (atom {})
        (repeat-body n 10
          (reset!m a assoc @a n num))))

    (System/gc)
    (println "Standard atom/transient map reset x10, trials: " trials)
    (time
      (trial-body trials num a (atom (transient {}))
        (repeat-body n 10
          (reset!m a assoc! @a n num))))

    (System/gc)
    (println "Unsynchronized atom/map reset x10, trials: " trials)
    (time
      (trial-body trials num a (UnsynchronizedAtom. {})
        (repeat-body n 10
          (reset!m a assoc @a n num))))

    (System/gc)
    (println "Unsynchronized atom/transient map reset x10, trials: " trials)
    (time
      (trial-body trials num a (UnsynchronizedAtom. (transient {}))
        (repeat-body n 10
          (reset!m a assoc! @a n num))))

    (System/gc)
    (println "Fast atom/map reset x10, trials: " trials)
    (time
      (trial-body trials num a (FastAtom. {})
        (repeat-body n 10
          (reset!m a assoc @a n num))))

    (System/gc)
    (println "Fast atom/transient map reset x10, trials: " trials)
    (time
      (trial-body trials num a (FastAtom. (transient {}))
        (repeat-body n 10
          (reset!m a assoc! @a n num))))

    ) ; End of let
  ;; "Test" succeeded
  true)



(deftest test-perf-1
  (testing "Performance"
    (println "\n\nTest 1 (Warm up the JVM)\n\n")
    (is (perf-test-2))
    (println "\n\nTest 2 (JVM warmed up)\n\n")
    (is (perf-test-2))))
