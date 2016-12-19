/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.api;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a callback handler for compensating any work done within this annotated method (or all methods of the class
 * when annotated at the class level). The callback is used later if the compensation-based transaction is cancelled and is
 * used to undo any work completed by the associated method.
 * <p>
 * The compensation handler is only registered if the method completes successfully (i.e doesn't throw a RuntimeException, or subclass thereof).
 * <p>
 * If the method fails, (i.e. throws a RuntimeException or subclass thereof) it is expected that the method leaves the
 * application in a consistent state as none of the registered handlers will be invoked.
 *
 * @author paul.robinson@redhat.com 21/03/2013
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TxCompensate {

    @Nonbinding
    public Class<? extends CompensationHandler> value() default DefaultTxCompensateHandler.class;
}
