package engineer.lisp.stabletransient;

import clojure.lang.IEditableCollection;
import clojure.lang.ITransientCollection;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Generates a transient of the provided IEditableCollection
 * which is stable in its head, such that the transient could
 * be modified in place. Hence, the semantics are slightly
 * changed (but backwards compatible) with regular Clojure
 * transients, but the APIs should remain the same.
 *
 * <p>This is probably Clojure 1.8 specific at this juncture.</p>
 * <code>
 * Copyright © 2017 Douglas P. Fields, Jr. All Rights Reserved.
 * Web: https://symbolics.lisp.engineer/
 * E-mail: symbolics@lisp.engineer
 * Twitter: @LispEngineer
 * </code>
 * October 27, 2017
 */
public class StableTransientGenerator {

    protected static final Class<?>[] CLASS_ARRAY_TYPE = new Class<?>[0];

    /** Creates the transient, wraps it in a proxy with all its same interfaces
     * but which checks for head changing calls and stores the new head as the
     * invocation target.
     */
    public static ITransientCollection makeStableTransient(IEditableCollection coll) {
        ITransientCollection theTransient = coll.asTransient();
        // System.err.println("theTransient: " + theTransient);

        // Class<?>[] ifs = theTransient.getClass().getInterfaces();
        Class<?>[] allIFs = ClassUtils.getAllInterfaces(theTransient.getClass()).toArray(CLASS_ARRAY_TYPE);
        // System.err.println("Interfaces: " + Arrays.toString(allIFs));

        Object newProxy = Proxy.newProxyInstance(
                ITransientCollection.class.getClassLoader(),
                allIFs,
                new StableTransientIH(theTransient));

        // I cannot imagine how this cast could fail given the above code,
        // so I won't check for a ClassCastException.
        return (ITransientCollection)newProxy;
    } // makeStableTransient


    /** The meat of the StableTransient: The proxy which intercepts changes
     * to the head and stores them for future invocations.
     */
    static class StableTransientIH implements InvocationHandler {

        private ITransientCollection target;

        private StableTransientIH(ITransientCollection target) {
            this.target = target;
        }

        /** Intercepts functions that return a new potential head for the
         * transient and saves them as the new target. These include:
         * <ul>
         *     <li>ITransientCollection.conj</li>
         *     <li>ITransientAssociative.assoc</li>
         *     <li>ITransientSet.disjoin</li>
         *     <li>ITransientMap.assoc</li>
         *     <li>ITransientMap.without</li>
         *     <li>ITransientVector.assocN</li>
         *     <li>ITransientVector.pop</li>
         * </ul>
         * We can check <code>method.getReturnType()</code> to see if it is
         * an ITransientCollection or a subInterface thereto,
         * and if so, just save it. Maybe that will
         * work...
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String mName = method.getName();
            Class rType = method.getReturnType();

            // Is the return type a transient collection? Is this a fast call?
            // If not, we probably should consider optimizing or doing the name
            // check first, for example.
            boolean isRTTC = ITransientCollection.class.isAssignableFrom(rType);
            if (false) {
                System.err.println("Calling: " + method +
                                   ", name: " + mName +
                                   ", return type: " + rType +
                                   ", RT is transient collection: " + isRTTC +
                                   // ", with args: " + args +
                                   // ", on: " + target +
                "");
            }

            Object retval = method.invoke(target, args);

            // If the return value is another transient collection, and it's
            // one of the functions that is used to mutate said transient
            // collections
            if (isRTTC) {
                if ("assoc".equals(mName) || "conj".equals(mName) ||
                    "disjoin".equals(mName) || "without".equals(mName) ||
                    "assocN".equals(mName) || "pop".equals(mName)) {
                        if (target != retval) {
                            System.err.println("Updated transient head.");
                        }
                        target = (ITransientCollection)retval;
                }
            }

            return retval;
        } // invoke
    } // StableTransientIH
}