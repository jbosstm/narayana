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
 * If a bean class contains a method marked with this annotation is invoked in the context of an SRA that only
 * contains a single participant then the annotated method will be invoked when the SRA is asked to commit.
 *
 * If a resource class contains multiple OnePhaseCommit annotations an arbitrary one is chosen.
 */
@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface OnePhaseCommit {
}