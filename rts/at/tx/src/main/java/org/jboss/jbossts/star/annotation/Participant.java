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
 * When a bean method executes in the context of an LRA any methods in the bean class that are annotated with
 * @Compensate will be used as a compensator for that LRA and when it is present, so too must the
 * {@link Compensate} and {@link Status} annotations. If it is applied to multiple methods an arbitrary one
 * is chosen.
 *
 * If the associated LRA is subsequently cancelled the method annotated with @Compensate will be invoked.
 *
 * The annotation can be combined with {@link TimeLimit} annotation to limit the time that the compensator
 * remains valid, after which the corresponding @Compensate method will be called.
 */
@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Participant {
}