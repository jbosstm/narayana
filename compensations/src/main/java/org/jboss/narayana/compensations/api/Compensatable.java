/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>The org.jboss.narayana.compensations.api.Compensatable annotation provides the application
 * the ability to declaratively control compensation transaction boundaries on CDI managed beans, as
 * well as classes defined as managed beans by the Java EE specification, at both the class
 * and method level where method level annotations override those at the class level.</p>
 * <p>This support is provided via an implementation of CDI interceptors that conduct the
 * necessary suspending, resuming, etc. The Compensatable interceptor interposes on business method
 * invocations only and not on lifecycle events. Lifecycle methods are invoked in an unspecified
 * transaction context.</p>
 * <p>The TxType element of the annotation indicates whether a bean method is to be executed within
 * a transaction context.  TxType.REQUIRED is the default.</p>
 * <p>By default checked exceptions do not result in the transactional interceptor marking the transaction for rollback
 * and instances of RuntimeException and its subclasses do. This default behavior can be modified by specifying
 * exceptions that result in the interceptor marking the transaction for rollback and/or exceptions that do not result in rollback.</p>
 *
 * @author paul.robinson@redhat.com 21/03/2013
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Compensatable {

    public CompensationTransactionType value() default CompensationTransactionType.REQUIRED;

    /**
     * The cancelOn element can be set to indicate exceptions that must cause
     * the interceptor to mark the transaction for compensation. Conversely, the dontCancelOn
     * element can be set to indicate exceptions that must not cause the interceptor to mark
     * the transaction for compensation. When a class is specified for either of these elements,
     * the designated behavior applies to subclasses of that class as well. If both elements
     * are specified, dontCancelOn takes precedence.
     *
     * @return Class[] of Exceptions
     */
    @Nonbinding Class[] cancelOn() default {};

    /**
     * The dontCancelOn element can be set to indicate exceptions that must not cause
     * the interceptor to mark the transaction for compensation. Conversely, the cancelOn element
     * can be set to indicate exceptions that must cause the interceptor to mark the transaction
     * for compensation. When a class is specified for either of these elements,
     * the designated behavior applies to subclasses of that class as well. If both elements
     * are specified, dontCancelOn takes precedence.
     *
     * @return Class[] of Exceptions
     */
    @Nonbinding Class[] dontCancelOn() default {};


    /**
     * The distributed element states whether a distributed or local transaction should be begun,
     * under circumstances where this annotation causes a new transaction to begin.
     *
     * @return Class[] of Exceptions
     */
    @Nonbinding boolean distributed() default false;

}