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
import java.util.concurrent.TimeUnit;

/**
 * @deprecated as of 5.8.1.Final. The API has been moved under the Eclipse umbrella org.eclipse.microprofile.lra.annotation
 *
 * <p>
 * Used on ({@link LRA} and {@link Compensate} annotations to indicate the maximum time that the LRA or
 * participant should remain active for.
 * <p>
 * When applied at the class level the timeout applies to any method that starts an LRA or registers a participant.
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Deprecated
public @interface TimeLimit {
    /**
     * @return the period for which the LRA or participant will remain valid. A value
     * of zero indicates that it is always remain valid.<br>
     *
     * For compensations the corresponding compensation (a method annotated with {@link Compensate} in the
     * same class) will be invoked if the time limit is reached.
     */
    long limit() default 0;

    TimeUnit unit() default TimeUnit.SECONDS;
}
