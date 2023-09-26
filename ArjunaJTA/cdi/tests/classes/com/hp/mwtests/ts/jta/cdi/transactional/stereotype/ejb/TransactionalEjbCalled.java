/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.ejb;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Stateless
public class TransactionalEjbCalled {
    @Resource(lookup = "java:/TransactionManager")
    TransactionManager tm;

    @Transactional(TxType.REQUIRES_NEW)
    public int inNewTransaction() {
        try {
            return tm.getStatus();
        } catch (Exception e) {
            throw new IllegalStateException("Expected transaction is active and fine to get status", e);
        }
    }

}