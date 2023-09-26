/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional.stereotype;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Stereotype;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;

@TransitiveToRequiredNoTransactional
public class StereotypeTransitiveBean {

    @Resource(mappedName = "java:jboss/TransactionManager")
    private TransactionManager txnMgr;

    /**
     * Having declared the {@link Transactional} annotation in {@link Stereotype}
     * last in the transitive row.
     */
    public void process() throws SystemException, TestException {
        // expected: @TransitiveToRequiredNoTransactional with REQUIRED
        if (Status.STATUS_ACTIVE == txnMgr.getStatus())
            throw new TestException();

        // not expected/fail:
        throw new AssertionError("@TransitiveToRequiredNoTransactional defines having an active txn");
    }
}