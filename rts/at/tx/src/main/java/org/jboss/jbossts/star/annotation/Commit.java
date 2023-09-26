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
 * When a SRA has preparesd successfully any bean resource classes that were invoked in its context
 * will be notified by calling methods marked with the @Commit annotation. If a resource class
 * contains multiple Commit annotations an arbitrary one is chosen.
 */
@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Commit {
}