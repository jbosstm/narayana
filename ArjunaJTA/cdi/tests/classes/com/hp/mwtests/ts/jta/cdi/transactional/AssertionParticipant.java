package com.hp.mwtests.ts.jta.cdi.transactional;

import junit.framework.Assert;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * @author paul.robinson@redhat.com 08/05/2013
 */
public class AssertionParticipant implements XAResource {

    private static Boolean committed = null;

    public static void enlist() throws Exception {
        Utills.getCurrentTransaction().enlistResource(new AssertionParticipant());
    }

    public static void assertCommitted() {

        if (committed == null) {
            Assert.fail("Neither committed or rolled back");
        }

        if (!committed) {
            Assert.fail("Should have committed, but Rolled back");
        }
    }

    public static void assertRolledBack() {

        if (committed == null) {
            Assert.fail("Neither committed or rolled back");
        }

        if (committed) {
            Assert.fail("Should have rolled back, but committed");
        }
    }

    public static void reset() {
        committed = null;
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        committed = true;
    }

    @Override
    public void end(Xid xid, int i) throws XAException {

    }

    @Override
    public void forget(Xid xid) throws XAException {

    }

    @Override
    public int getTransactionTimeout() throws XAException {

        return 0;
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {

        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {

        return XA_OK;
    }

    @Override
    public Xid[] recover(int i) throws XAException {

        return new Xid[0];
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        committed = false;
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {

        return false;
    }

    @Override
    public void start(Xid xid, int i) throws XAException {

    }
}
