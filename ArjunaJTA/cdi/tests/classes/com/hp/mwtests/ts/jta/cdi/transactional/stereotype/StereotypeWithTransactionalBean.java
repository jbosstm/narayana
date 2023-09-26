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

@TransitiveToRequiredTransactionalNever
public class StereotypeWithTransactionalBean {

    @Resource(mappedName = "java:jboss/TransactionManager")
    private TransactionManager txnMgr;

    /**
     * Having declared the {@link Transactional} annotation in {@link Stereotype}
     * last in the transitive row and in the middle one too.
     */
    public void process() throws SystemException, TestException {
        // expected: @TransitiveToRequiredTransactionalNever with NEVER
        if (Status.STATUS_NO_TRANSACTION != txnMgr.getStatus())
            throw new TestException();

        // not expected/fail:
        throw new AssertionError("There was active transaction even NEVER is expected to be taken"
            + " as Transactional attribute");
    }
}