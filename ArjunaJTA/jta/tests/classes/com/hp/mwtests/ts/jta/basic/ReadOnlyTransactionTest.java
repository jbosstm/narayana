/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;

/**
 * The only possible outcome of a read-only transaction is rollback: commit
 * must roll the transaction back and throw RollbackException, enlisted
 * resources must never see prepare/commit, and the readOnly flag must be
 * visible through TransactionImple, UserTransactionImple and the
 * TransactionSynchronizationRegistry.
 *
 * These tests use the concrete Narayana API (BaseTransaction.begin(boolean),
 * TransactionImple.isReadOnly()) so they are valid whichever version of
 * jakarta.transaction-api is on the classpath.
 */
public class ReadOnlyTransactionTest
{
    private TransactionManagerImple tm;

    @Before
    public void setUp ()
    {
        ThreadActionData.purgeActions();

        tm = (TransactionManagerImple) com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    @Test
    public void testDefaultBeginIsNotReadOnly () throws Exception
    {
        tm.begin();

        assertFalse(((TransactionImple) tm.getTransaction()).isReadOnly());

        tm.commit();

        assertNull(tm.getTransaction());
    }

    @Test
    public void testBeginReadOnly () throws Exception
    {
        tm.begin(true);

        TransactionImple tx = (TransactionImple) tm.getTransaction();

        assertTrue(tx.isReadOnly());
        assertEquals(Status.STATUS_ACTIVE, tx.getStatus());

        tm.rollback();
    }

    @Test
    public void testCommitReadOnlyRollsBackAndDisassociates () throws Exception
    {
        tm.begin(true);

        try
        {
            tm.commit();

            fail("commit of a read-only transaction must throw RollbackException");
        }
        catch (final RollbackException ex)
        {
            assertTrue(ex.getMessage().contains("read-only"));
        }

        assertNull(tm.getTransaction());
    }

    @Test
    public void testTransactionCommitReadOnly () throws Exception
    {
        TransactionImple tx = new TransactionImple(0, true);

        assertTrue(tx.isReadOnly());

        try
        {
            tx.commit();

            fail("commit of a read-only transaction must throw RollbackException");
        }
        catch (final RollbackException ex)
        {
        }

        assertEquals(Status.STATUS_ROLLEDBACK, tx.getStatus());

        ThreadActionData.purgeActions();
    }

    @Test
    public void testEnlistedResourceIsRolledBackNotCommitted () throws Exception
    {
        tm.begin(true);

        RecordingXAResource xares = new RecordingXAResource();

        assertTrue(tm.getTransaction().enlistResource(xares));

        try
        {
            tm.commit();

            fail("commit of a read-only transaction must throw RollbackException");
        }
        catch (final RollbackException ex)
        {
        }

        assertTrue(xares.rolledBack);
        assertFalse(xares.prepared);
        assertFalse(xares.committed);

        assertNull(tm.getTransaction());
    }

    @Test
    public void testRollbackFailureIsSuppressed () throws Exception
    {
        FailingRollbackTransaction tx = new FailingRollbackTransaction();

        try
        {
            tx.commitAndDisassociateForTest();

            fail("commit of a read-only transaction must throw RollbackException");
        }
        catch (final RollbackException ex)
        {
            assertEquals(1, ex.getSuppressed().length);
            assertEquals("rollback failed", ex.getSuppressed()[0].getMessage());
        }

        ThreadActionData.purgeActions();
    }

    @Test
    public void testSynchronizationSeesRollback () throws Exception
    {
        tm.begin(true);

        RecordingSynchronization sync = new RecordingSynchronization();

        tm.getTransaction().registerSynchronization(sync);

        try
        {
            tm.commit();

            fail("commit of a read-only transaction must throw RollbackException");
        }
        catch (final RollbackException ex)
        {
        }

        assertFalse(sync.beforeCompletionCalled);
        assertEquals(Status.STATUS_ROLLEDBACK, sync.afterCompletionStatus);
    }

    @Test
    public void testSetRollbackOnlyReadOnly () throws Exception
    {
        tm.begin(true);

        tm.setRollbackOnly();

        try
        {
            tm.commit();

            fail("commit of a read-only transaction must throw RollbackException");
        }
        catch (final RollbackException ex)
        {
        }

        assertNull(tm.getTransaction());
    }

    @Test
    public void testExplicitRollbackUnchanged () throws Exception
    {
        tm.begin(true);

        tm.rollback();

        assertNull(tm.getTransaction());
    }

    @Test
    public void testUserTransactionIsReadOnly () throws Exception
    {
        UserTransactionImple ut = new UserTransactionImple();

        assertFalse(ut.isReadOnly());

        ut.begin(true);

        assertTrue(ut.isReadOnly());

        try
        {
            ut.commit();

            fail("commit of a read-only transaction must throw RollbackException");
        }
        catch (final RollbackException ex)
        {
        }

        assertFalse(ut.isReadOnly());

        ut.begin();

        assertFalse(ut.isReadOnly());

        ut.commit();
    }

    @Test
    public void testSynchronizationRegistryIsReadOnly () throws Exception
    {
        TransactionSynchronizationRegistryImple tsr = new TransactionSynchronizationRegistryImple();

        try
        {
            tsr.isReadOnly();

            fail("isReadOnly with no transaction must throw IllegalStateException");
        }
        catch (final IllegalStateException ex)
        {
        }

        tm.begin(true);

        assertTrue(tsr.isReadOnly());

        tm.rollback();

        tm.begin();

        assertFalse(tsr.isReadOnly());

        tm.commit();
    }

    /**
     * Rolls back for real, then reports a failure — so the suppression
     * handling in commitAndDisassociate's read-only branch is exercised
     * deterministically.
     */
    private static class FailingRollbackTransaction extends TransactionImple
    {
        FailingRollbackTransaction ()
        {
            super(0, true);
        }

        @Override
        protected void rollbackAndDisassociate ()
                throws IllegalStateException, SecurityException, jakarta.transaction.SystemException
        {
            super.rollbackAndDisassociate();

            throw new IllegalStateException("rollback failed");
        }

        void commitAndDisassociateForTest () throws Exception
        {
            commitAndDisassociate();
        }
    }

    private static class RecordingSynchronization implements jakarta.transaction.Synchronization
    {
        boolean beforeCompletionCalled;
        int afterCompletionStatus = -1;

        @Override
        public void beforeCompletion ()
        {
            beforeCompletionCalled = true;
        }

        @Override
        public void afterCompletion (int status)
        {
            afterCompletionStatus = status;
        }
    }

    private static class RecordingXAResource implements XAResource
    {
        boolean prepared;
        boolean committed;
        boolean rolledBack;

        @Override
        public void start (Xid xid, int flags) throws XAException
        {
        }

        @Override
        public void end (Xid xid, int flags) throws XAException
        {
        }

        @Override
        public int prepare (Xid xid) throws XAException
        {
            prepared = true;

            return XA_OK;
        }

        @Override
        public void commit (Xid xid, boolean onePhase) throws XAException
        {
            committed = true;
        }

        @Override
        public void rollback (Xid xid) throws XAException
        {
            rolledBack = true;
        }

        @Override
        public void forget (Xid xid) throws XAException
        {
        }

        @Override
        public Xid[] recover (int flag) throws XAException
        {
            return null;
        }

        @Override
        public boolean isSameRM (XAResource xares) throws XAException
        {
            return false;
        }

        @Override
        public int getTransactionTimeout () throws XAException
        {
            return 0;
        }

        @Override
        public boolean setTransactionTimeout (int seconds) throws XAException
        {
            return false;
        }
    }
}
