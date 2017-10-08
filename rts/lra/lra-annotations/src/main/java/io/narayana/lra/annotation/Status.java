/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.annotation;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * In order to support recovery compensators must be able to report their status once the completion part of the protocol
 * starts.
 * <p>
 * Methods annotated with this annotation must be JAX-RS resources and respond to GET requests (ie are annotated with
 * javax.ws.rs.Path and javax.ws.rs.GET, respectively). They must report their status using one of the enum names listed
 * in {@link CompensatorStatus} whenever an HTTP GET request is made on the method.
 * <p>
 * If the participant has not yet been asked to complete or compensate it should return with a <code>412 Precondition Failed</code>
 * HTTP status code. NB although this circumstance could be detected via the framework
 * it would necessitate a network call to the LRA coordinator.
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Status {
}
