# fast-atom

A Clojure library designed to act exactly like an `atom` but without
any thread-safety or other atomicity. Hopefully this could be used for fast,
mutable data in inner loops or imperative sections of code.

This is currently targeting the Clojure 1.8 `Atom`. The Clojure 1.9 `Atom` has
different semantics as it implements a different interface (`IAtom2`) which
all return both the old and new values of the atom.

The two classes provided are:

* `UnsynchronizedAtom` which is like the regular `Atom` but without using
  `AtomicReference`. It needs to use reflection to call the package-access
  `validate()` function in its superclass.
* `FastAtom` which has no validation or watcher support, and also abandons
  `AtomicReference`. It's an absolutely minimal implementation of `Atom`.

# Usage

`lein test`

# Notes

These doesn't actually seem to improve performance markedly.
I need to enhance the benchmark tests and do proper profiling, to
determine where the performance is being spent.

Interestingly, the `swap!` forms are vastly more efficient than the
`reset!` forms. Investigation is needed.

# Preliminary Findings

* I'm doing 10,000,000 trials of `assoc`'ing 10 keys to an
  atom containing a map (transient or persistent) with `swap!` or
  `reset!`.
* Accounting for GC has not yet been done, so these are necessarily
  very preliminary. The results are a bit inconsistent, perhaps due to
  GC or maybe JIT CompileThreshold being reached.
  * I should force JIT pre-compilation.
  * I should try using `*warn-on-reflection*` and `*unchecked-math*`.
* Accounting for the overhead of the test harness has not been done.
* Using `transient`s are almost 25% faster in the tests than
  persistent maps.
* Using `UnsynchronizedAtom` is a bit faster than regular `Atom`,
  when using `swap!`. Slower with `reset!`???
* Using `FastAtom` is about 10% faster than regular `Atom`,
  when using `swap!`. Slower with `reset!`???




# License

Copyright © 2017 Douglas P. Fields, Jr. All Rights Reserved.

Proprietary. Not available for license... yet.

* Web: https://symbolics.lisp.engineer/
* E-mail: symbolics@lisp.engineer
* Twitter: @LispEngineer
