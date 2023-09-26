/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.tm;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.List;

public class TestResource implements XAResource
{
    List<TestResource> prepareOrder = null;
    List<TestResource> endOrder = null;

    public int prepare (Xid xid) throws XAException {
        prepareOrder.add(this);
        return XA_OK;
    }

    public TestResource(List<TestResource> prepareOrder, List<TestResource> endOrder) {
        this.prepareOrder = prepareOrder;
        this.endOrder = endOrder;
    }

    public void commit (Xid id, boolean onePhase) throws XAException {
        endOrder.add(this);
    }

    public void rollback (Xid xid) throws XAException {
        endOrder.add(this);
    }

    public void end (Xid xid, int flags) throws XAException {
    }

    public void forget (Xid xid) throws XAException {
    }

    public int getTransactionTimeout () throws XAException {
        return _timeout;
    }

    public boolean isSameRM (XAResource xares) throws XAException {
        return false;
    }

    public Xid[] recover (int flag) throws XAException {
        return null;
    }

    public boolean setTransactionTimeout (int seconds) throws XAException {
        _timeout = seconds;
        return true;
    }

    public void start (Xid xid, int flags) throws XAException {
    }

    protected int _timeout = 0;
}