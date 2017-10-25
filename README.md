# fast-atom

A Clojure library designed to act exactly like an `atom` but without
any thread-safety or other atomicity. This can be used for fast,
mutable data in inner loops or imperative sections of code.
The underlying Java class is called `UnsynchronizedAtom` and supports
validators and watchers.

This is currently targeting the Clojure 1.8 `Atom`. The Clojure 1.9 `Atom` has
different semantics as it implements a different interface (`IAtom2`) which
all return both the old and new values. I am not yet sure if this 

# Usage

FIXME

# License

Copyright © 2017 Douglas P. Fields, Jr. All Rights Reserved.

Proprietary, trade secret. Not available for license.

* Web: https://symbolics.lisp.engineer/
* E-mail: symbolics@lisp.engineer
* Twitter: @LispEngineer
