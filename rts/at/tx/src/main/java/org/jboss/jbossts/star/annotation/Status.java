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
 * In order to support recovery, participants must be able to report their status once the completion
 * part of the protocol starts.
 *
 * Methods annotated with this annotation must be JAX-RS resources and respond to GET requests
 * (ie are annotated with @Path and @GET, respectively). They must report their status using one
 * of the enum names listed in {@link ParticipantStatus} whenever an HTTP GET request
 * is made on the method.
 */
@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Status {
}