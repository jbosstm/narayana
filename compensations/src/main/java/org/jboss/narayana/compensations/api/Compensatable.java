/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.narayana.compensations.api;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import org.jboss.narayana.compensations.internal.interceptors.participant.TxCompensateInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * The {@link org.jboss.narayana.compensations.api.Compensatable} annotation provides the application
 * the ability to declaratively control compensation transaction boundaries on CDI managed beans, as
 * well as classes defined as managed beans by the Java EE specification, at both the class
 * and method level where method level annotations override those at the class level
 * <p>
 * This support is provided via an implementation of CDI interceptors that conduct the
 * necessary suspending, resuming, etc. The Compensatable interceptor {@link TxCompensateInterceptor}
 * interposes on business method invocations only and not on lifecycle events.
 * Lifecycle methods are invoked in an unspecified transaction context.
 * <p>
 * The {@link CompensationTransactionType} element of the annotation indicates whether a bean method
 * is to be executed within a transaction context.<br>
 * Functionality is equivalent to what is known from Java EE and how annotation javax.ejb.TransactionAttribute
 * works. The {@link CompensationTransactionType} defines when and how the transactional context
 * is propagated to the annotated method.
 * {@link CompensationTransactionType#REQUIRED} is the default.
 * <p>
 * By default checked exceptions do not result in the transactional interceptor marking the transaction for rollback
 * and instances of {@link RuntimeException} and its subclasses do.
 * This default behavior can be modified by specifying exceptions that result in the interceptor
 * marking the transaction for rollback and/or exceptions that do not result in rollback.
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
