/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.annotation;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a SRA is asked to commit any bean resource classes that were invoked in its context will
 * be notified by calling methods marked with the @Prepare annotation. If a resource class contains
 * multiple Prepare annotations an arbitrary one is chosen.
 *
 * If the participant prepares successfully it must return a 200 code. If it cannot then it must return
 * a 409 code. Any response other than 200 MUST cause the SRA to roll back.
 */
@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Prepare {
}