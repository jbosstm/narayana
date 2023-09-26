/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.recovery;

import java.io.Serializable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

public class TestXAResourceRmFail extends TestXAResource implements Serializable {
    private static int commitCount;
    private static int rollbackCount;
    
    private static final long serialVersionUID = 1L;
    private boolean wasThrown = false;

    @Override
    public void commit(Xid id, boolean onePhase) throws XAException {
        if(!wasThrown) {
            wasThrown = true;
            throw new XAException(XAException.XAER_RMFAIL);
        }
        TestXAResourceRmFail.commitCount++;
        super.commit(id, onePhase);
    }

    public void rollback(Xid xid) throws XAException {
        TestXAResourceRmFail.rollbackCount++;
        super.rollback(xid);
    }

    public int commitCount() {
        return TestXAResourceRmFail.commitCount;
    }

    public int rollbackCount() {
        return TestXAResourceRmFail.rollbackCount;
    }

    public TestXAResourceRmFail clearCounters() {
        commitCount = 0;
        rollbackCount = 0;
        return this;
    }
}