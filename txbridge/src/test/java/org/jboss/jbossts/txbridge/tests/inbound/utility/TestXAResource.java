/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.inbound.utility;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

/**
 * Implementation of XAResource for use in tx test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
public class TestXAResource extends TestXAResourceCommon implements XAResource {

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        super.commit(xid, b);
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
        super.end(xid, i);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        super.forget(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return super.getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return super.isSameRM(xaResource);
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return super.prepare(xid);
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        return super.recover(i);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        super.rollback(xid);
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return super.setTransactionTimeout(i);
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
        super.start(xid, i);
    }

    @Override
    public String toString() {
        return new String("TestXAResource(" + super.toString() + ")");
    }
}