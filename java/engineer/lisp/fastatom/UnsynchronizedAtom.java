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
 * to be used in imperative-style code inner loops, for example, on those
 * rare occasions such things would be useful in Clojure.
 */
final public class UnsynchronizedAtom
        extends ARef
        implements IAtom {

    /** The reflection-found Method for ARef.validate(Object). */
    static final protected Method validateMethod;

    /* Initialize our validateMethod, which was declared with package access
     * in the ARef parent class, so we need to do reflection to call it.
     */
    static {
        Method vm = null;
        try {
            vm = UnsynchronizedAtom.class.getSuperclass().getDeclaredMethod("validate", Object.class);
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        }
        vm.setAccessible(true);
        validateMethod = vm;
    }

    /** The state of the atom. This is a simple, non-volatile object reference,
     * so there are no memory barriers here. This should only be used in single-
     * threaded code, or code which provides its own memory barriers.
     */
    protected Object state;

    /** Create UnsynchronizedAtom with starting state. */
    public UnsynchronizedAtom(Object state) {
        this.state = state;
    }

    /** Create UnsynchronizedAtom with starting state and Clojure metadata. */
    public UnsynchronizedAtom(Object state, IPersistentMap meta) {
        super(meta);
        this.state = state;
    }

    /** Get the state of the UnsynchronizedAtom. */
    public Object deref() {
        return state;
    }

    /** Call ARef.validate which is clojure.lang package access only. */
    protected void callableValidate(Object val) {
        try {
            validateMethod.invoke(this, val);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
        }
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
        Object v = state;
        Object newv = f.invoke(v);
        if (validator != null) { callableValidate(newv); }
        state = newv;
        notifyWatches(v, newv);
        return newv;
    }

    /** IAtom: Swaps the UnsynchronizedAtom's state using the provided function
     * and arg to mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f, Object arg) {
        Object v = state;
        Object newv = f.invoke(v, arg);
        if (validator != null) { callableValidate(newv); }
        state = newv;
        notifyWatches(v, newv);
        return newv;
    }

    /** IAtom: Swaps the UnsynchronizedAtom's state using the provided function
     * and args to mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f, Object arg1, Object arg2) {
        Object v = state;
        Object newv = f.invoke(v, arg1, arg2);
        if (validator != null) { callableValidate(newv); }
        state = newv;
        notifyWatches(v, newv);
        return newv;
    }

    /** IAtom: Swaps the UnsynchronizedAtom's state using the provided function
     * and args to mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f, Object x, Object y, ISeq args) {
        Object v = state;
        Object newv = f.applyTo(RT.listStar(v, x, y, args));
        if (validator != null) { callableValidate(newv); }
        state = newv;
        notifyWatches(v, newv);
        return newv;
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
        if (validator != null) { callableValidate(newState); }
        boolean set = false;
        if (state == oldState) {
            state = newState;
            set = true;
            notifyWatches(oldState, newState);
        }
        return set;
    }

    /** IAtom: Sets the UnsynchronizedAtom's state unconditionally.
     * Always validates new state. Notifies watchers.
     * @param newState The new state
     * @return the new state
     */
    public Object reset(Object newState) {
        Object oldval = state;
        if (validator != null) { callableValidate(newState); }
        state = newState;
        notifyWatches(oldval, newState);
        return newState;
    }
}