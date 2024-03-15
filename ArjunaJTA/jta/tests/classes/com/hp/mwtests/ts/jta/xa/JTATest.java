/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.xa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class JTATest {

    private XAException exception;
    protected boolean resource1Rollback;
    protected boolean resource2Rollback;

    @Test
    public void testDuplicateXAREndCalled() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        AtomicBoolean endCalled = new AtomicBoolean(false);

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            public int id = 1;

            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                try {
                    Class<? extends XAResource> aClass = xares.getClass();
                    Field field = aClass.getField("id");
                    int other = field.getInt(xares);
                    if (other == 1) {
                        return true;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    fail("Unexpected XAR");
                }
                return false;
            }

            @Override
            public void start(Xid xid, int flags) throws XAException {
                super.start(xid, flags);
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
                super.end(xid, flags);
            }
        }));
        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            public int id = 1;

            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                try {
                    Field field = xares.getClass().getField("id");
                    int other = field.getInt(xares);
                    if (other == 1) {
                        return true;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    fail("Unexpected XAR");
                }
                return false;
            }

            @Override
            public void start(Xid xid, int flags) throws XAException {
                super.start(xid, flags);
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
                endCalled.set(true);
                super.end(xid, flags);
            }
        }));
        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            public int id = 2;

            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                return false;
            }
        }));
        tm.commit();

        assertTrue(endCalled.get());
    }

    @Test
    public void testDuplicateXAREndCalledFailure() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();
        XAException rmFail = new XAException(XAException.XAER_RMFAIL);
        XAException rbRollback = new XAException(XAException.XA_RBROLLBACK);

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                return true;
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
                super.end(xid, flags);
            }
        }));
        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                return true;
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
                throw rmFail;
            }
        }));
        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public boolean isSameRM(XAResource xares) throws XAException {
                return true;
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
                throw rbRollback;
            }
        }));
        try {
            tm.commit();
            fail("Committed");
        } catch (RollbackException e) {
            assertTrue(Arrays.asList(e.getSuppressed()).contains(rmFail));
            assertTrue(Arrays.asList(e.getSuppressed()).contains(rbRollback));
        }
    }

    @Test
    public void testRMFAILcommit1PC() throws Exception {
        XAResource theResource = new XAResource() {

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
                throw new XAException(XAException.XAER_RMFAIL);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
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
        };

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(theResource));

        try {
            tm.commit();
            fail();
        } catch (jakarta.transaction.HeuristicMixedException e) {
            // Expected
        }
    }

    @Test
    public void test() throws Exception {

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new XARMERRXAResource(false)));
        XARMERRXAResource rollbackCalled = new XARMERRXAResource(true);
        assertTrue(theTransaction.enlistResource(rollbackCalled));

        tm.rollback();
        assertTrue(rollbackCalled.getRollbackCalled());
    }

    /**
     * This is none-spec behaviour that some resource managers perform where they throw a RTE instead of return an XAException
     * This test verifies that RTE will result in rollback in Narayana
     *
     * @throws SecurityException
     * @throws IllegalStateException
     * @throws HeuristicMixedException
     * @throws HeuristicRollbackException
     * @throws SystemException
     * @throws NotSupportedException
     * @throws RollbackException
     */
    @Test
    public void testRollbackRTE() throws SecurityException, IllegalStateException, HeuristicMixedException, HeuristicRollbackException, SystemException, NotSupportedException, RollbackException {

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public int prepare(Xid xid) throws XAException {
                throw new RuntimeException();
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                resource1Rollback = true;
            }
        }));

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {

            @Override
            public void rollback(Xid xid) throws XAException {
                resource2Rollback = true;
            }
        }));

        try {
            tm.commit();
            fail("Should not have committed");
        } catch (RollbackException e) {
            assertTrue(resource1Rollback);
            assertTrue(resource2Rollback);
        }
    }

    @Test
    public void testXARB_INTEGRITYprepare() throws Exception {
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public int prepare(Xid xid) throws XAException {
                throw new XAException(XAException.XA_RBINTEGRITY);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                resource1Rollback = true;
            }
        }));

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {

            @Override
            public void rollback(Xid xid) throws XAException {
                resource2Rollback = true;
            }
        }));

        try {
            tm.commit();
            fail("Should not have committed");
        } catch (RollbackException e) {
            // This is going to pass because of JBTM-3843
            assertFalse(resource1Rollback);
            assertTrue(resource2Rollback);
        }
    }

    @Test
    public void testHeuristicRollbackSuppressedException() throws NotSupportedException, SystemException, IllegalStateException, RollbackException, SecurityException, HeuristicMixedException, HeuristicRollbackException {

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new XAResource() {

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
                exception = new XAException(XAException.XA_HEURRB);
                throw exception;
            }

            @Override
            public void rollback(Xid xid) throws XAException {


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
        }));
        assertTrue(theTransaction.enlistResource(new XAResource() {

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


            }

            @Override
            public void rollback(Xid xid) throws XAException {


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
        }));

        try {
            tm.commit();
            fail();
        } catch (RollbackException e) {
            e.printStackTrace();
            assertTrue(e.getSuppressed()[0] == exception);
        }

    }

    private class XARMERRXAResource implements XAResource {

        private boolean returnRMERROutOfEnd;
        private boolean rollbackCalled;

        public XARMERRXAResource(boolean returnRMERROutOfEnd) {
            this.returnRMERROutOfEnd = returnRMERROutOfEnd;
        }

        public boolean getRollbackCalled() {
            return rollbackCalled;
        }

        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException {
            // TODO Auto-generated method stub

        }

        @Override
        public void end(Xid xid, int flags) throws XAException {
            if (returnRMERROutOfEnd) {
                throw new XAException(XAException.XAER_RMERR);
            }
        }

        @Override
        public void forget(Xid xid) throws XAException {
            // TODO Auto-generated method stub

        }

        @Override
        public int getTransactionTimeout() throws XAException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean isSameRM(XAResource xares) throws XAException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Xid[] recover(int flag) throws XAException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void rollback(Xid xid) throws XAException {
            rollbackCalled = true;
        }

        @Override
        public boolean setTransactionTimeout(int seconds) throws XAException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void start(Xid xid, int flags) throws XAException {
            // TODO Auto-generated method stub

        }

    }

    private abstract class SimpleXAResource implements XAResource {

        @Override
        public void start(Xid xid, int flags) throws XAException {

        }

        @Override
        public boolean setTransactionTimeout(int seconds) throws XAException {

            return false;
        }

        @Override
        public void rollback(Xid xid) throws XAException {
        }

        @Override
        public Xid[] recover(int flag) throws XAException {

            return null;
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            return XAResource.XA_OK;
        }

        @Override
        public boolean isSameRM(XAResource xares) throws XAException {

            return false;
        }

        @Override
        public int getTransactionTimeout() throws XAException {

            return 0;
        }

        @Override
        public void forget(Xid xid) throws XAException {

        }

        @Override
        public void end(Xid xid, int flags) throws XAException {

        }

        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException {

        }
    }
}