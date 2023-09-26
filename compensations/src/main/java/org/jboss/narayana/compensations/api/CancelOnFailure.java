/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When applied at method level, states that the compensation-based transaction must cancel, if a RuntimeException
 * (or subclass of) is thrown from that particular method.
 * <p/>
 * When applied at class level, states that the compensation-based transaction must cancel, if a RuntimeException
 * (or subclass of) is thrown from any business method of the class.
 *
 * @author paul.robinson@redhat.com 25/04/2013
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CancelOnFailure {

}