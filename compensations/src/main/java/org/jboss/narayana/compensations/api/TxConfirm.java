/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a callback handler for confirming any work done within this annotated method (or all methods of the class
 * when annotated at the class level). The callback is used later if the compensation-based transaction is completes successfully.
 * <p/>
 * The confirmation handler is only registered if the method completes successfully (i.e doesn't throw a RuntimeException, or subclass thereof).
 * <p/>
 * If the method fails, (i.e. throws a RuntimeException or subclass thereof) it is expected that the method leaves the
 * application in a consistent state as none of the registered handlers will be invoked.
 *
 * @author paul.robinson@redhat.com 21/03/2013
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TxConfirm {

    @Nonbinding
    public Class<? extends ConfirmationHandler> value() default DefaultTxConfirmHandler.class;
}