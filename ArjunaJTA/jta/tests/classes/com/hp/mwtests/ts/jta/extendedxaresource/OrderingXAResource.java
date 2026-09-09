/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.extendedxaresource;

import jakarta.transaction.xa.CommitPriority;
import jakarta.transaction.xa.ExtendedXAResource;
import jakarta.transaction.xa.PreparePriority;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import java.util.List;

/**
 * Test XAResource that implements ExtendedXAResource and records the
 * sequence of prepare/commit/rollback calls for assertion in tests.
 */
public class OrderingXAResource implements XAResource, ExtendedXAResource {

    private final String name;
    private final PreparePriority preparePriority;
    private final CommitPriority commitPriority;
    private final List<String> callLog;

    public OrderingXAResource(String name, PreparePriority preparePriority,
            CommitPriority commitPriority, List<String> callLog) {
        this.name = name;
        this.preparePriority = preparePriority;
        this.commitPriority = commitPriority;
        this.callLog = callLog;
    }

    @Override
    public PreparePriority getPreparePriority() {
        return preparePriority;
    }

    @Override
    public CommitPriority getCommitPriority() {
        return commitPriority;
    }

    @Override
    public boolean setReadOnly(Xid xid) throws XAException {
        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        callLog.add(name + ":prepare");
        return XA_OK;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        callLog.add(name + ":commit");
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        callLog.add(name + ":rollback");
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
    }

    @Override
    public Xid[] recover(int flags) throws XAException {
        return new Xid[0];
    }

    @Override
    public void forget(Xid xid) throws XAException {
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return 60;
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return this == xaResource;
    }

    @Override
    public String toString() {
        return name;
    }
}
