(defproject engineer.lisp/fast-atom "0.2.0-SNAPSHOT"
  :description "fast-atom: Alternative atoms library for Clojure"
  :url "https://symbolics.lisp.engineer/"
  :license {:name "Copyright 2017 Douglas P. Fields, Jr. All Rights Reserved."}

  :source-paths      ["src"]
  :java-source-paths ["java"]
  :javac-options     ["-target" "1.8" "-source" "1.8"]
  :jvm-opts          ["-Xmx8g" "-Xms8g" "-Xmn7g" ; Memory
                      "-server"
                      #_"-Xcomp" ; Force JIT compilation at the start; harms performance due to no JIT optimization
                      ;; Watch what's going on in the GC - Clojure makes a metric ton of garbage in the young gen
                      ;; due to immutable data structures
                      #_"-XX:+PrintGCDetails" #_"-verbose:gc"
                     ]

  ;; :main engineer.lisp.fast-atom.core

  :dependencies [[org.clojure/clojure "1.8.0"]]
)
