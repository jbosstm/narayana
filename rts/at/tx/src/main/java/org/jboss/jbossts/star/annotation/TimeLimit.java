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
import java.util.concurrent.TimeUnit;

/**
 * Used on {@link SRA} and {@link Rollback} annotations to indicate the maximum time that the SRA or
 * participant should remain active for.
 *
 * When applied at the class level the timeout applies to any method that starts an SRA
 * or causes a participant to be registered (ie when the bean class contains a Commit annotation).
 *
 * In the case of participants the corresponding method that is annotated with {@link Rollback} will
 * be invoked when the time limit is reached.
 */
@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface TimeLimit {
    long limit() default 0;

    TimeUnit unit() default TimeUnit.SECONDS;
}