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
import jakarta.transaction.Transactional.TxType;

@TransactionalRequiredStereotype
public class StereotypeBean {

    @Resource(mappedName = "java:jboss/TransactionManager")
    private TransactionManager txnMgr;

    /**
     * Method using {@link Transactional} declaration inherited from
     * {@link Stereotype} defined at the class level.
     */
    public void stereotypeRequiredAtBean() throws SystemException {
        // expected @TransactionalRequiredStereotype with REQUIRED
        if (Status.STATUS_ACTIVE != txnMgr.getStatus())
            throw new AssertionError("@TransactionalRequiredStereotype defines having an active txn");
    }

    /**
     * Method rewriting the {@link Transactional} annotation in {@link Stereotype}
     * from the class level and changing it by direct usage of the annotation.
     */
    @Transactional(value = TxType.NEVER)
    public void transactionalAtMethod() throws SystemException {
        // expected @Transactional with NEVER
        if (Status.STATUS_NO_TRANSACTION != txnMgr.getStatus())
            throw new AssertionError("@Transactional/NEVER defines no active transaction expected");
    }
}