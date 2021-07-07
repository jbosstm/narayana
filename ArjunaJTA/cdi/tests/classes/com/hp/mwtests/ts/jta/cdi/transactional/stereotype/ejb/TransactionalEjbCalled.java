/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.ejb;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

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
