(defproject engineer.lisp/fast-atom "0.2.0-SNAPSHOT"
  :description "fast-atom: Alternative atoms library for Clojure"
  :url "https://symbolics.lisp.engineer/"
  :license {:name "Copyright 2017 Douglas P. Fields, Jr. All Rights Reserved."}

  :source-paths      ["src"]
  :java-source-paths ["java"]
  :javac-options     ["-target" "1.8" "-source" "1.8"]
  :jvm-opts          ["-Xmx4g" "-Xms4g" "-Xmn2g" "-server"]

  ;; :main engineer.lisp.fast-atom.core

  :dependencies [[org.clojure/clojure "1.8.0"]]
)
