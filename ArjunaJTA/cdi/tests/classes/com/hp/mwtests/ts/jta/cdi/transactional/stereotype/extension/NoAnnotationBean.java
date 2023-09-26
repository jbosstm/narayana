/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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