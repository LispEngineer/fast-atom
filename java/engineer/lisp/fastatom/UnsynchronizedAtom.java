/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/*
 * Copyright © 2017 Douglas P. Fields, Jr. All Rights Reserved.
 * Web: https://symbolics.lisp.engineer/
 * E-mail: symbolics@lisp.engineer
 * Twitter: @LispEngineer
 *
 * Fast "atom"-like data structure for Clojure, based upon the
 * Clojure 1.8 Atom. This may need to be revised to work with
 * Clojure 1.9 which has a new IAtom2 interface.
 *
 * October 24, 2017
 */

package engineer.lisp.fastatom;

import clojure.lang.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
            vm = UnsynchronizedAtom.class.getDeclaredMethod("validate", Object.class);
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

    /** Swaps the UnsynchronizedAtom's state using the provided function to
     * mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f) {
        Object v = state;
        Object newv = f.invoke(v);
        callableValidate(newv);
        state = newv;
        notifyWatches(v, newv);
        return newv;
    }

    /** Swaps the UnsynchronizedAtom's state using the provided function
     * and arg to mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f, Object arg) {
        Object v = state;
        Object newv = f.invoke(v, arg);
        callableValidate(newv);
        state = newv;
        notifyWatches(v, newv);
        return newv;
    }

    /** Swaps the UnsynchronizedAtom's state using the provided function
     * and args to mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f, Object arg1, Object arg2) {
        Object v = state;
        Object newv = f.invoke(v, arg1, arg2);
        callableValidate(newv);
        state = newv;
        notifyWatches(v, newv);
        return newv;
    }

    /** Swaps the UnsynchronizedAtom's state using the provided function
     * and args to mutate it.
     * Never fails. Always validates and notifies watchers.
     * @param f modification function that takes the old state
     * @return the new state
     */
    public Object swap(IFn f, Object x, Object y, ISeq args) {
        Object v = state;
        Object newv = f.applyTo(RT.listStar(v, x, y, args));
        callableValidate(newv);
        state = newv;
        notifyWatches(v, newv);
        return newv;
    }

    /** Sets the UnsynchronizedAtom's state as long as the state
     * is as provided.
     * Always validates new state. Notifies watchers if set.
     * <br/>
     * <em>Note that this does not have any memory barriers.</em>
     * @param oldState The old state, compared with == (object identity)
     * @param newState The new state
     * @return true if we set the state
     */
    public boolean compareAndSet(Object oldState, Object newState) {
        callableValidate(newState);
        boolean set = false;
        if (state == oldState) {
            state = newState;
            set = true;
            notifyWatches(oldState, newState);
        }
        return set;
    }

    /** Sets the UnsynchronizedAtom's state unconditionally.
     * Always validates new state. Notifies watchers.
     * @param newState The new state
     * @return the new state
     */
    public Object reset(Object newState) {
        Object oldval = state;
        callableValidate(newState);
        state = newState;
        notifyWatches(oldval, newState);
        return newState;
    }
}