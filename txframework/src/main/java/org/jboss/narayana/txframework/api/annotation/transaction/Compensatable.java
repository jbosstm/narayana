/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.api.annotation.transaction;

import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated The TXFramework API will be removed. The org.jboss.narayana.compensations API should be used instead.
 * The new API is superior for these reasons:
 * <p/>
 * i) offers a higher level API;
 * ii) The API very closely matches that of JTA, making it easier for developers to learn,
 * iii) It works for non-distributed transactions as well as distributed transactions.
 * iv) It is CDI based so only needs a CDI container to run, rather than a full Java EE server.
 * <p/>
 * Class level annotation used to declare that  a participant service will participate in lifecycle
 * processing for WSBA business activity transactions.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Compensatable {

    /**
     * the specific WSBA completion protocol which should be used for a WSBA service participant
     *
     * @return the type of completion to execute (coordinator or participant)
     */
    public CompletionType completionType() default CompletionType.COORDINATOR;
}