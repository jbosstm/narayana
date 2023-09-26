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
 * When a SRA rolls back any bean resource classes that were invoked in its context will be notified by
 * calling methods marked with this Rollback annotation. If a resource class contains multiple Rollback
 * annotations an arbitrary one is chosen.
 *
 * The annotation can be combined with {@link TimeLimit} annotation to limit the time that the
 * participant remains valid, after which the corresponding @Rollback method will be called.
 */
@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Rollback {
}