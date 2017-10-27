;; Copyright © 2017 Douglas P. Fields, Jr. All Rights Reserved.
;; Web: https://symbolics.lisp.engineer/
;; E-mail: symbolics@lisp.engineer
;; Twitter: @LispEngineer

(ns engineer.lisp.stable-transient.core
  "Stable transients: transients whose head will never change
   and hence have different semantics despite having the same
   syntax and API.

   Relevant Java interfaces and classes:
   IEditableCollection

   ITransientCollection
   ITransientAssociative
   ITransientMap
   ITransientSet
   ITransientVector

   This initial implementation uses Java Proxy. Hopefully that
   will have decent performance. See https://opencredo.com/dynamic-proxies-java-part-2/
   which suggests only 2.6ns per call, which should be fine.

   Copyright © 2017 Douglas P. Fields, Jr. All Rights Reserved.
   Web: https://symbolics.lisp.engineer/
   E-mail: symbolics@lisp.engineer
   Twitter: @LispEngineer"
  (:import (engineer.lisp.stabletransient StableTransientGenerator)
           (clojure.lang IEditableCollection)))

(defn x
  []
  (transient {}))

(defn stable-transient
  "Returns a new, transient version of the collection, in constant
   time, and whose head will not change so it may be edited in place
   safely with the usual transient API."
  [^IEditableCollection coll]
  (StableTransientGenerator/makeStableTransient coll))

