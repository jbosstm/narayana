/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.inbound.utility;

import org.jboss.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Basic implementation of XAResource for use in tx test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 * @author Ivo Studensky (istudens@redhat.com)
 */
public abstract class TestXAResourceCommon implements XAResource {
    private static Logger log = Logger.getLogger(TestXAResourceCommon.class);

    private int txTimeout;

    private Xid currentXid;

    private int prepareReturnValue = XAResource.XA_OK;

    public void commit(Xid xid, boolean b) throws XAException {
        log.trace("TestXAResourceCommon.commit(Xid=" + xid + ", b=" + b + ")");
        if (!xid.equals(currentXid)) {
            log.trace("TestXAResourceCommon.commit - wrong Xid!");
        }

        currentXid = null;
        TestXAResourceRecoveryHelper.getInstance().removeLog(xid);
    }

    public void end(Xid xid, int i) throws XAException {
        log.trace("TestXAResourceCommon.end(Xid=" + xid + ", b=" + i + ")");
    }

    public void forget(Xid xid) throws XAException {
        log.trace("TestXAResourceCommon.forget(Xid=" + xid + ")");
        if (!xid.equals(currentXid)) {
            log.trace("TestXAResourceCommon.forget - wrong Xid!");
        }
        currentXid = null;
    }

    public int getTransactionTimeout() throws XAException {
        log.trace("TestXAResourceCommon.getTransactionTimeout() [returning " + txTimeout + "]");
        return txTimeout;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        log.trace("TestXAResourceCommon.isSameRM(xaResource=" + xaResource + ")");
        return false;
    }

    public int prepare(Xid xid) throws XAException {
        log.trace("TestXAResourceCommon.prepare(Xid=" + xid + ") returning " + prepareReturnValue);

        if (prepareReturnValue == XA_OK) {
            TestXAResourceRecoveryHelper.getInstance().logPrepared(xid);
        }
        return prepareReturnValue;
    }

    public Xid[] recover(int i) throws XAException {
        log.trace("TestXAResourceCommon.recover(i=" + i + ")");
        return new Xid[0];
    }

    public void rollback(Xid xid) throws XAException {
        log.trace("TestXAResourceCommon.rollback(Xid=" + xid + ")");
        if (!xid.equals(currentXid)) {
            log.trace("TestXAResourceCommon.rollback - wrong Xid!");
        }
        currentXid = null;
        TestXAResourceRecoveryHelper.getInstance().removeLog(xid);
    }

    public boolean setTransactionTimeout(int i) throws XAException {
        log.trace("TestXAResourceCommon.setTransactionTimeout(i=" + i + ")");
        txTimeout = i;
        return true;
    }

    public void start(Xid xid, int i) throws XAException {
        log.trace("TestXAResourceCommon.start(Xid=" + xid + ", i=" + i + ")");
        if (currentXid != null) {
            log.trace("TestXAResourceCommon.start - wrong Xid!");
        }
        currentXid = xid;
    }

    public String toString() {
        return new String("TestXAResourceCommon(" + txTimeout + ", " + currentXid + ")");
    }
}