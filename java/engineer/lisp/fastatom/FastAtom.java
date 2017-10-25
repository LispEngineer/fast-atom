/*
 * Copyright © 2017 Douglas P. Fields, Jr. All Rights Reserved.
 * Web: https://symbolics.lisp.engineer/
 * E-mail: symbolics@lisp.engineer
 * Twitter: @LispEngineer
 *
 * Fast "atom"-like data structure for Clojure, based upon the
 * Clojure 1.8 Atom by Rich Hickey. This may need to be revised to work with
 * Clojure 1.9 which has a new IAtom2 interface.
 *
 * October 24, 2017
 */

package engineer.lisp.fastatom;

import clojure.lang.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** A class implementing the same interfaces as
 * Clojure 1.8 Atom, but without any synchronization or memory barriers,
 * nor any support for validators or watchers,
 * to be used in imperative-style code inner loops, for example, on those
 * rare occasions such things would be useful in Clojure.
 */
final public class FastAtom
        extends ARef
        implements IAtom {

    /** The state of the atom. This is a simple, non-volatile object reference,
     * so there are no memory barriers here. This should only be used in single-
     * threaded code, or code which provides its own memory barriers.
     */
    protected Object state;

    /** Create UnsynchronizedAtom with starting state. */
    public FastAtom(Object state) {
        this.state = state;
    }

    /** Create UnsynchronizedAtom with starting state and Clojure metadata. */
    public FastAtom(Object state, IPersistentMap meta) {
        super(meta);
        this.state = state;
    }

    /** Get the state of the UnsynchronizedAtom. */
    public Object deref() {
        return state;
    }

    //////////////////////////////////////////////////////////////////////////////
    // IAtom Implementation

    /** IAtom: Swaps the UnsynchronizedAtom's state using the provided function to
     * mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f) {
        return state = f.invoke(state);
    }

    /** IAtom: Swaps the UnsynchronizedAtom's state using the provided function
     * and arg to mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f, Object arg) {
        return state = f.invoke(state, arg);
    }

    /** IAtom: Swaps the UnsynchronizedAtom's state using the provided function
     * and args to mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f, Object arg1, Object arg2) {
        return state = f.invoke(state, arg1, arg2);
    }

    /** IAtom: Swaps the UnsynchronizedAtom's state using the provided function
     * and args to mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f, Object x, Object y, ISeq args) {
        return state = f.applyTo(RT.listStar(state, x, y, args));
    }

    /** IAtom: Sets the UnsynchronizedAtom's state as long as the state
     * is as provided.
     * Always validates new state. Notifies watchers if set.
     * <br/>
     * <em>Note that this does not have any memory barriers.</em>
     * @param oldState The old state, compared with == (object identity)
     * @param newState The new state
     * @return true if we set the state
     */
    public boolean compareAndSet(Object oldState, Object newState) {
        boolean set = false;
        if (state == oldState) {
            state = newState;
            set = true;
        }
        return set;
    }

    /** IAtom: Sets the UnsynchronizedAtom's state unconditionally.
     * Always validates new state. Notifies watchers.
     * @param newState The new state
     * @return the new state
     */
    public Object reset(Object newState) {
        return state = newState;
    }
}