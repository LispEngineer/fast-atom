# fast-atom

A Clojure library designed to act exactly like an `atom` but without
any thread-safety or other atomicity. Hopefully this could be used for fast,
mutable data in inner loops or imperative sections of code.

WORK IN PROGRESS:
Additionally, a `stable-transient` is provided, which is an enhancement of
standard Clojure `transient` which makes the various mutating functions
make an in-place modification of the transient. Hence, there is no need
to wrap `stable-transient`s in `atom`s. They have all the key drawbacks
of `transient`s, including not being equal, not being able to get their
keys, being single-threaded, etc.

This is currently targeting the Clojure 1.8 `Atom`. The Clojure 1.9 `Atom` has
different semantics as it implements a different interface (`IAtom2`) which
all return both the old and new values of the atom.

The two classes provided are:

* `UnsynchronizedAtom` which is like the regular `Atom` but without using
  `AtomicReference`. It needs to use reflection to call the package-access
  `validate()` function in its superclass.
* `FastAtom` which has no validation or watcher support, and also abandons
  `AtomicReference`. It's an absolutely minimal implementation of `Atom`.
  
Clojure 1.7 also provides `volatile`. Perhaps that is good enough. I could
always create a version of that which omits the `volatile` Java keyword
for total thread-non-safety, of course. The performance of this over Atom
is swamped by exactly how functions are invoked in Clojure, though, so
maybe this is enough and I should just drop the whole investigation.
See [this comment](https://groups.google.com/d/msg/clojure/QTzG8Ze6wuc/g9Tj4lDeSocJ)
for instance.

# Usage

* `lein test`
* `lein test :only engineer.lisp.stable-transient.core-test`

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
  * JIT pre-compilation? Seems to slow things down, probably due to
    having no JIT optimization.
  * Use `*warn-on-reflection*` and `*unchecked-math*` to ensure that
    silly things aren't being done - they're not (anymore).
  * The non-transient map generates a ton of garbage, so that is definitely
    impacting performance, but the GC summaries say the YoungGen GC is
    0.00 seconds, so less than 10ms, so maybe not important.
* Accounting for the overhead of the test harness has not been done.
* Using `transient`s are almost 25% faster in the tests than
  persistent maps.
* Using `UnsynchronizedAtom` is a bit faster than regular `Atom`,
  when using `swap!`. Slower with `reset!`???
* Using `FastAtom` is about 10% faster than regular `Atom`,
  when using `swap!`. Slower with `reset!`???
* `reset!` is a lot slower. A guess is that evaluating the
  value form of the `reset!` slows things down compared to the
  `swap!` which invokes the IFn directly without an extra `deref`
  (`@`) call. Needs to be profiled.
  
  
# TODO

* Use [criterium](https://github.com/hugoduncan/criterium) instead of `time`
* Benchmark/test `volatile`


# Suggestions

* Modify `clojure.lang.ARef` to make `validate(...)` protected instead
  of package access. This seems to be safe as it's only used by
  `Atom` and `Agent` which have `ARef` as their superclasses, and
  doesn't seem to have any actual package access invocations. 


# License

Copyright © 2017 Douglas P. Fields, Jr. All Rights Reserved.

Proprietary. Not available for license... yet.

* Web: https://symbolics.lisp.engineer/
* E-mail: symbolics@lisp.engineer
* Twitter: @LispEngineer
