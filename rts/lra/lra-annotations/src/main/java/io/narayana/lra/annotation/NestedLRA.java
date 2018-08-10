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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated as of 5.8.1.Final. The API has been moved under the Eclipse umbrella org.eclipse.microprofile.lra.annotation
 *
 * <p>
 * Used on the interface or class. Defines that the container will create
 * a new LRA for each method invocation, regardless of whether or not there is
 * already an LRA associated with the caller. These LRAs will then
 * either be top-level LRAs or nested automatically depending upon the
 * context within which they are created.
 * <p>
 * When a nested LRA is closed its' compensators are completed but retained. At any time prior to the enclosing LRA
 * being closed or cancelled the nested LRA can be told to compensate (even though it may have already been told
 * to complete).
 * <p>
 * Compatability with the @LRA annotation: if {@link LRA} is not present @NestedLRA is ignored, otherwise the behaviour depends
 * upon the value of the @LRA type attribute:
 * <p>
 * <ul>
 *   <li>REQUIRED
 *     if there is an LRA present a new LRA is nested under it
 *   <li>REQUIRES_NEW,
 *     the @NestedLRA annotation is ignored
 *  <li>MANDATORY,
 *     a new LRA is nested under the incoming LRA
 *  <li>SUPPORTS,
 *    if there is an LRA present a new LRA is nested under otherwise a new top level LRA is begun
 *  <li>NOT_SUPPORTED,
 *    nested does not make sense and operations on this resource that contain a LRA context will immediately return
 *    with a <code>412 Precondition Failed</code> HTTP status code
 *  <li>NEVER,
 *    nested does not make sense and requests that carry a LRA context will immediately return
 *    with a <code>412 Precondition Failed</code> HTTP status code
 * </ul>
 */
@Inherited
@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Deprecated
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface NestedLRA {
}
