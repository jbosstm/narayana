/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.jta.xa;

import org.junit.Test;

import jakarta.transaction.RollbackException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JTARMErrorCommitTest {

    private boolean aborted;
    private Set<Integer> abortedIds = new HashSet<Integer>();

    @Test
    public void test() throws Exception {

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();
        assertTrue(theTransaction.enlistResource(new SimpleXAResource(1, true)));
        assertTrue(theTransaction.enlistResource(new SimpleXAResource(2, false)));

        try {
            tm.commit();
            fail();
        } catch (RollbackException e) {
        } finally {
            assertTrue(abortedIds.contains(2));
            assertTrue(aborted);
            assertTrue(abortedIds.size() == 1);
        }
    }

    private class SimpleXAResource implements XAResource {

        private final int id;
        private boolean fail;

        public SimpleXAResource(int id, boolean fail) {
            this.id = id;
            this.fail = fail;
        }

        @Override
        public void start(Xid xid, int flags) throws XAException {
        }

        @Override
        public void end(Xid xid, int flags) throws XAException {
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            return 0;
        }

        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException {
            if (fail) {
                throw new XAException(XAException.XAER_RMERR);
            }
        }

        @Override
        public void rollback(Xid xid) throws XAException {
            if (!fail) {
                aborted = true;
            }
            abortedIds.add(id);
        }

        @Override
        public void forget(Xid xid) throws XAException {
        }

        @Override
        public Xid[] recover(int flag) throws XAException {
            return null;
        }

        @Override
        public boolean isSameRM(XAResource xaRes) throws XAException {
            return false;
        }

        @Override
        public int getTransactionTimeout() throws XAException {
            return 0;
        }

        @Override
        public boolean setTransactionTimeout(int seconds) throws XAException {
            return false;
        }
    }
}
