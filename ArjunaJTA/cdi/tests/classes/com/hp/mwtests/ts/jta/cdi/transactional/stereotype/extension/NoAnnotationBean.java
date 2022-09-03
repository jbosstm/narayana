/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.extension;

import jakarta.annotation.Resource;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

public class NoAnnotationBean {
    @Resource(mappedName = "java:jboss/TransactionManager")
    private TransactionManager txnMgr;

    /**
     * Expecting the cdi adds a stereotype or transactional annotation
     * to define bean works in a transaction.
     */
    public void process() throws SystemException {
        if (Status.STATUS_ACTIVE == txnMgr.getStatus())
            // exception is necessary to be thrown here as it tests how transactional interceptor behaves
            // in case of the transaction is thrown
            throw new RuntimeException("exception for testing purposes - correct active status");

        // not expected/fail:
        throw new AssertionError("The stereotype bean define by extension annotation for"
                + "having active transaction on method call, but there is not.");
    }
}
