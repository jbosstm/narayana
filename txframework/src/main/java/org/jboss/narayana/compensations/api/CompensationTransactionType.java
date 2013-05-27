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

/**
 * Indicates whether a bean method is to be
 * executed within a transaction context where the values provide the following
 * corresponding behavior.
 *
 * Only REQUIRED supported at the moment. See https://issues.jboss.org/browse/JBTM-1678 for when other types will be available
 *
 * @author paul.robinson@redhat.com 21/03/2013
 */
public enum CompensationTransactionType {

    /**
     *  <p>If called outside a transaction context, the interceptor must begin a new
     *  compensation-based transaction, the managed bean method execution must then continue
     *  inside this transaction context, and the transaction must be completed by
     *  the interceptor.</p>
     *  <p>If called inside a compensation-based transaction context, the managed bean
     *  method execution must then continue inside this transaction context.</p>
     */
    REQUIRED
}
