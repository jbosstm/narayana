/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.subordinate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.resource.spi.XATerminator;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.jta.resources.spi.XATerminatorExtensions;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.TransactionImple;
import com.arjuna.ats.jta.xa.XidImple;

public class SubordinateTestCase
{
    // This test class is subclassed by the JTAX version of the tests, so we isolate
    // the module specific tx creation code to this function, which then gets overridden.
    public SubordinateTransaction createTransaction() {
        return new TransactionImple(0); // implicit begin
    }

    @Test
	public void testCleanupCommit () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final SubordinateTransaction tm = createTransaction();

			assertEquals(TwoPhaseOutcome.PREPARE_READONLY, tm.doPrepare());
			// don't call commit for read only case.
		}

		assertEquals(ActionManager.manager().getNumberOfInflightTransactions(), 0);
	}

    @Test
	public void testCleanupRollback () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final SubordinateTransaction tm = createTransaction();

			tm.doRollback();
		}

		assertEquals(ActionManager.manager().getNumberOfInflightTransactions(), 0);
	}

    @Test
	public void testCleanupSecondPhaseRollback () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final SubordinateTransaction tm = createTransaction();

			assertEquals(TwoPhaseOutcome.PREPARE_READONLY, tm.doPrepare());
			// don't call rollback for read only case
		}

		assertEquals(ActionManager.manager().getNumberOfInflightTransactions(), 0);
	}

    @Test
	public void testCleanupOnePhaseCommit () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final SubordinateTransaction tm = createTransaction();

			tm.doOnePhaseCommit();
		}

		assertEquals(ActionManager.manager().getNumberOfInflightTransactions(), 0);
	}

    /////////////

    @Test
    public void testOnePhaseCommitSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.doOnePhaseCommit();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

    @Test
    public void testOnePhaseCommitSyncViaXATerminator() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        xaTerminator.commit(xid, true);
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

    @Test
    public void testOnePhaseCommitSyncWithRollbackOnly() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.setRollbackOnly();
        try {
            tm.doOnePhaseCommit();
            fail("did not get expected rollback exception");
        } catch(RollbackException e) {
            // expected - we tried to commit a rollbackonly tx.
        }
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

    @Test
    public void testOnePhaseCommitSyncWithRollbackOnlyViaXATerminator() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        t.setRollbackOnly();
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        try {
            xaTerminator.commit(xid, true);
            ((TransactionImple)t).doOnePhaseCommit();
            fail("did not get expected rollback exception");
        } catch(XAException e) {
            assertEquals("jakarta.transaction.RollbackException", e.getCause().getClass().getName());
            assertEquals(XAException.XA_RBROLLBACK, e.errorCode);
            // expected - we tried to commit a rollbackonly tx.
        }
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

    @Test
    public void testRollbackSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.doRollback();
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

    @Test
    public void testRollbackSyncViaXATerminator() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        xaTerminator.rollback(xid);
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        assertEquals(TwoPhaseOutcome.PREPARE_READONLY, tm.doPrepare());
        // don't call commit for read only case
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncViaXATerminator() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        assertEquals(XAResource.XA_RDONLY, xaTerminator.prepare(xid));
        // don't call commit for read only case
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncWithXAOK() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        final TestXAResource xaResource = new TestXAResource();
        xaResource.setPrepareReturnValue(XAResource.XA_OK);
        tm.enlistResource(xaResource);
        assertEquals(TwoPhaseOutcome.PREPARE_OK, tm.doPrepare());
        tm.doCommit();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncWithXAOKViaXATerminator() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        final TestXAResource xaResource = new TestXAResource();
        xaResource.setPrepareReturnValue(XAResource.XA_OK);
        t.enlistResource(xaResource);
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        assertEquals(XAResource.XA_OK, xaTerminator.prepare(xid));
        xaTerminator.commit(xid, false);
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncWithRollbackOnly() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.setRollbackOnly();
        assertEquals(TwoPhaseOutcome.PREPARE_NOTOK, tm.doPrepare());
        tm.doRollback();
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncWithRollbackOnlyViaXATerminator() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        t.setRollbackOnly();
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();

        try {
            xaTerminator.prepare(xid);
        } catch(XAException e) {
            assertEquals(XAException.XA_RBROLLBACK, e.errorCode);
            // expected - we tried to prepare a rollbackonly tx.
        }
        // no need to call rollback - the XA_RBROLLBACK code indicates its been done.
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

    /////////////

    @Test
    public void testOnePhaseCommitSyncWithSeparateSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.doBeforeCompletion();
        tm.doOnePhaseCommit();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

    @Test
    public void testOnePhaseCommitSyncViaXATerminatorWithSeparateSync() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        final XATerminatorExtensions xaTerminatorExtensions = (XATerminatorExtensions)xaTerminator;
        xaTerminatorExtensions.beforeCompletion(xid);
        xaTerminator.commit(xid, true);
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

    @Test
    public void testOnePhaseCommitSyncWithRollbackOnlyWithSeparateSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.setRollbackOnly();
        tm.doBeforeCompletion();
        try {
            tm.doOnePhaseCommit();
            fail("did not get expected rollback exception");
        } catch(RollbackException e) {
            // expected - we tried to commit a rollbackonly tx.
        }
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

    @Test
    public void testOnePhaseCommitSyncWithRollbackOnlyViaXATerminatorWithSeparateSync() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        t.setRollbackOnly();
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        final XATerminatorExtensions xaTerminatorExtensions = (XATerminatorExtensions)xaTerminator;
        xaTerminatorExtensions.beforeCompletion(xid);
        try {
            xaTerminator.commit(xid, true);
            ((TransactionImple)t).doOnePhaseCommit();
            fail("did not get expected rollback exception");
        } catch(XAException e) {
            assertEquals("jakarta.transaction.RollbackException", e.getCause().getClass().getName());
            assertEquals(XAException.XA_RBROLLBACK, e.errorCode);
            // expected - we tried to commit a rollbackonly tx.
        }
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

    @Test
    public void testRollbackSyncWithSeparateSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.doBeforeCompletion();
        tm.doRollback();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

    @Test
    public void testRollbackSyncViaXATerminatorWithSeparateSync() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        final XATerminatorExtensions xaTerminatorExtensions = (XATerminatorExtensions)xaTerminator;
        xaTerminatorExtensions.beforeCompletion(xid);
        xaTerminator.rollback(xid);
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncWithSeparateSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.doBeforeCompletion();
        assertEquals(TwoPhaseOutcome.PREPARE_READONLY, tm.doPrepare());
        // don't call commit for read only case
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncViaXATerminatorWithSeparateSync() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        final XATerminatorExtensions xaTerminatorExtensions = (XATerminatorExtensions)xaTerminator;
        xaTerminatorExtensions.beforeCompletion(xid);
        assertEquals(XAResource.XA_RDONLY, xaTerminator.prepare(xid));
        // don't call commit for read only case
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncWithXAOKWithSeparateSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        final TestXAResource xaResource = new TestXAResource();
        xaResource.setPrepareReturnValue(XAResource.XA_OK);
        tm.enlistResource(xaResource);
        tm.doBeforeCompletion();
        assertEquals(TwoPhaseOutcome.PREPARE_OK, tm.doPrepare());
        tm.doCommit();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncWithXAOKViaXATerminatorWithSeparateSync() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        final TestXAResource xaResource = new TestXAResource();
        xaResource.setPrepareReturnValue(XAResource.XA_OK);
        t.enlistResource(xaResource);
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        final XATerminatorExtensions xaTerminatorExtensions = (XATerminatorExtensions)xaTerminator;
        xaTerminatorExtensions.beforeCompletion(xid);
        assertEquals(XAResource.XA_OK, xaTerminator.prepare(xid));
        xaTerminator.commit(xid, false);
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncWithRollbackOnlyWithSeparateSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.setRollbackOnly();
        tm.doBeforeCompletion();
        assertEquals(TwoPhaseOutcome.PREPARE_NOTOK, tm.doPrepare());
        tm.doRollback();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

    @Test
    public void testTwoPhaseCommitSyncWithRollbackOnlyViaXATerminatorWithSeparateSync() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        t.setRollbackOnly();
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        final XATerminatorExtensions xaTerminatorExtensions = (XATerminatorExtensions)xaTerminator;
        xaTerminatorExtensions.beforeCompletion(xid);

        try {
            xaTerminator.prepare(xid);
        } catch(XAException e) {
            assertEquals(XAException.XA_RBROLLBACK, e.errorCode);
            // expected - we tried to prepare a rollbackonly tx.
        }
        // no need to call rollback - the XA_RBROLLBACK code indicates its been done.
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(jakarta.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

    @Test
    public void testFailOnCommitOnePhase () throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);

        final TestXAResource xaResource = new TestXAResource();
        // provoke commit into failing with TwoPhaseOutcome.FINISH_ERROR
        // warning: this is sensitive to the impl exception handling in
        // XAResourceRecord.topLevelCommit
        xaResource.setCommitException(new XAException(XAException.XA_HEURRB));  // should cause an exception!

        t.enlistResource(xaResource);

        final XATerminator xaTerminator = SubordinationManager.getXATerminator();

        try
        {
            xaTerminator.commit(xid, true);
        }
        catch (final XAException ex)
        {
            // success!

            return;
        }

        assertTrue("commit should throw an exception and not get to here", false);
    }

    /**
     * <p>
     * Running subordinate one phase commit where the subordinate transaction
     * contains two resources. The subordinate transaction was considered to be convenient for 1PC
     * but the resources on behalf runs 2PC.
     * </p>
     * <p>
     * Top-level transaction contains a XAResource which represents a subordinate transaction.
     * As there is only one then 1PC is run. But under the subordinate transaction there are
     * two XA resources. Thus those two run 2PC.
     * When failure during commit happens there has to be announced to the top-level
     * transaction that failure happens. As top-level run only commit and did not store
     * any information to object storage the heuristic is correct outcome.
     * </p>
     */
    @Test
    public void testFailOnCommitRmFailTwoResourcesOnePhase () throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);

        final TestXAResource xaResource1 = new TestXAResource();
        final TestXAResource xaResource2 = new TestXAResource();
        xaResource2.setCommitException(new XAException(XAException.XAER_RMFAIL));

        t.enlistResource(xaResource1);
        t.enlistResource(xaResource2);

        final XATerminator xaTerminator = SubordinationManager.getXATerminator();

        try
        {
            xaTerminator.commit(xid, true);
            fail("1PC commit should throw an exception and not get to here");
        }
        catch (final XAException ex)
        {
            assertEquals("RMFAIL means commit to be retried during 2PC. On 1PC we consider it as a heuristic failure.",
                    ex.getCause().getClass(), HeuristicMixedException.class);
        }
    }

    @Test
    public void testFailOnCommitRetry () throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);

        final TestXAResource xaResource = new TestXAResource();

        xaResource.setCommitException(new XAException(XAException.XA_RETRY));

        t.enlistResource(xaResource);

        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        Xid[] recover1 = xaTerminator.recover(XAResource.TMSTARTRSCAN);
        xaTerminator.recover(XAResource.TMENDRSCAN);
        
        xaTerminator.prepare(xid);

        /*
         * This should not cause problems. The transaction really has committed, or will once
         * recovery kicks off. So nothing for the parent to do. The subordinate log will
         * maintain enough information to drive recovery locally if we get to the point of
         * issuing a commit call from parent to child.
         */

        try {
            xaTerminator.commit(xid, false);
            fail("Expected an error");
        } catch (XAException e) {
            assertTrue(e.errorCode == XAException.XAER_RMFAIL);
        }
        
        Xid[] recover2 = xaTerminator.recover(XAResource.TMSTARTRSCAN);
        xaTerminator.recover(XAResource.TMENDRSCAN);

        if (recover1 == null) {
            recover1 = new Xid[0];
        }
        int difference = recover2.length - recover1.length;
        assertTrue("" + difference, difference == 1);
    }

    @Test
    public void testFailOnCommit() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);

        final TestXAResource xaResource = new TestXAResource();
        // provoke commit into failing with TwoPhaseOutcome.FINISH_ERROR
        // warning: this is sensitive to the impl exception handling in
        // XAResourceRecord.topLevelCommit
        xaResource.setCommitException(new XAException(XAException.XA_HEURHAZ));  // throw a little spice into things!

        t.enlistResource(xaResource);

        final XATerminator xaTerminator = SubordinationManager.getXATerminator();

        try
        {
            xaTerminator.prepare(xid);
            xaTerminator.commit(xid, false);
        }
        catch (final XAException ex)
        {
            // success!!

            return;
        }

        assertTrue("commit should throw an exception and not get to here", false);
    }

    @Test
    public void testPrepareRollback() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        assertEquals(TwoPhaseOutcome.PREPARE_READONLY, tm.doPrepare());
        tm.doRollback(); // Due to the readonly we allow the massage - this matches doPhase2Abort in ServerTransaction
    }
}