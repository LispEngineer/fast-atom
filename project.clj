(defproject fast-atom "0.1.1-SNAPSHOT"
  :description "fast-atom: A fast, unsynchronized atom for Clojure"
  :url "https://symbolics.lisp.engineer/"
  :license {:name "Copyright 2017 Douglas P. Fields, Jr. All Rights Reserved."}

  :source-paths      ["src"]
  :java-source-paths ["java"]
  :javac-options     ["-target" "1.8" "-source" "1.8"]

  :dependencies [[org.clojure/clojure "1.8.0"]]
)
