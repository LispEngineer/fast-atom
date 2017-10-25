(ns engineer.lisp.fast-atom.core-test
  (:import (engineer.lisp.fastatom UnsynchronizedAtom))
  (:require [clojure.test :refer :all]
            [engineer.lisp.fast-atom.core :refer :all]))

(defn perf-test
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

(deftest a-test
  (testing "Performance"
    (is (perf-test))
    (is (perf-test))))
