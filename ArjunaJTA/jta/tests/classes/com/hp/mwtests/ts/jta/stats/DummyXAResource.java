/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.stats;

import java.io.Serializable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class DummyXAResource implements XAResource, Serializable {
    static final long serialVersionUID = 1;

    public enum faultType {HALT, EX, NONE, RESOURCE_ROLLBACK, TIMEOUT_PREPARE, TIMEOUT_COMMIT, XA_HEURRB, COMMIT}

    private transient faultType fault = faultType.NONE;

    private static int commitRequests = 0;

    private Xid[] recoveryXids;

    public boolean startCalled;
    public boolean endCalled;
    public boolean prepareCalled;
    public boolean commitCalled;
    public boolean rollbackCalled;
    public boolean forgetCalled;
    public boolean recoverCalled;

    public DummyXAResource()
    {
        this(faultType.NONE);
    }

    public DummyXAResource(faultType fault)
    {
        this.fault = fault;
    }

    public void commit(final Xid xid, final boolean arg1) throws XAException
    {
        System.out.println("DummyXAResource commit() called, fault: " + fault + " xid: " + xid);
        commitCalled = true;
        commitRequests += 1;

        if (fault != null) {
            if (fault.equals(faultType.EX)) {
                throw new XAException(XAException.XA_RBTRANSIENT);
            } else if (fault.equals(faultType.HALT)) {
                recoveryXids = new Xid[1];
                recoveryXids[0] = xid;
                Runtime.getRuntime().halt(1);
            } else if (fault.equals(faultType.RESOURCE_ROLLBACK)) {
                throw new XAException(XAException.XA_RBTIMEOUT);
            } else if (fault.equals(faultType.XA_HEURRB)) {
                throw new XAException(XAException.XA_HEURRB);
            }
        }
    }

    public void end(final Xid xid, final int arg1) throws XAException
    {
        endCalled = true;
    }

    public void forget(final Xid xid) throws XAException
    {
        forgetCalled = true;
    }

    public int getTransactionTimeout() throws XAException
    {
        return 0;
    }
    public boolean isSameRM(final XAResource arg0) throws XAException
    {
        return this.equals(arg0);
    }

    public int prepare(final Xid xid) throws XAException
    {
        prepareCalled = true;

        if (fault.equals(faultType.RESOURCE_ROLLBACK))
            throw new XAException(XAException.XA_RBTIMEOUT);

        return XAResource.XA_OK;
    }

    public Xid[] recover(final int arg0) throws XAException
    {
        recoverCalled = true;
        return recoveryXids;
    }

    public void rollback(final Xid xid) throws XAException
    {
        rollbackCalled = true;
    }

    public void start(final Xid xid, final int arg1) throws XAException
    {
        startCalled = true;
    }

    public boolean setTransactionTimeout(final int arg0) throws XAException
    {
        return false;
    }


    public static int getCommitRequests() {
        return commitRequests;
    }
}