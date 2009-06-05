/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.subordinate;

import junit.framework.TestCase;

import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.resources.spi.XATerminatorExtensions;
import com.arjuna.ats.jta.xa.XidImple;

import javax.transaction.RollbackException;
import javax.transaction.Transaction;
import javax.transaction.Status;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.resource.spi.XATerminator;

public class SubordinateTestCase extends TestCase
{
    // This test class is subclassed by the JTAX version of the tests, so we isolate
    // the module specific tx creation code to this function, which then gets overridden.
    public SubordinateTransaction createTransaction() {
        return new TransactionImple(0); // implicit begin
    }

	public void testCleanupCommit () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final SubordinateTransaction tm = createTransaction();

			tm.doPrepare();
			tm.doCommit();
		}

		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}

	public void testCleanupRollback () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final SubordinateTransaction tm = createTransaction();

			tm.doRollback();
		}

		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}

	public void testCleanupSecondPhaseRollback () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final SubordinateTransaction tm = createTransaction();

			tm.doPrepare();
			tm.doRollback();
		}

		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}

	public void testCleanupOnePhaseCommit () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final SubordinateTransaction tm = createTransaction();

			tm.doOnePhaseCommit();
		}

		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}

    /////////////

    public void testOnePhaseCommitSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.doOnePhaseCommit();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

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
            assertEquals("javax.transaction.RollbackException", e.getCause().getClass().getName());
            assertEquals(XAException.XA_RBROLLBACK, e.errorCode);
            // expected - we tried to commit a rollbackonly tx.
        }
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

    public void testRollbackSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.doRollback();
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

    public void testTwoPhaseCommitSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        assertEquals(TwoPhaseOutcome.PREPARE_READONLY, tm.doPrepare());
        tm.doCommit();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

    public void testTwoPhaseCommitSyncViaXATerminator() throws Exception
    {
        final Xid xid = new XidImple(new Uid());
        final Transaction t = SubordinationManager.getTransactionImporter().importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        t.registerSynchronization(sync);
        final XATerminator xaTerminator = SubordinationManager.getXATerminator();
        assertEquals(XAResource.XA_RDONLY, xaTerminator.prepare(xid));
        // note that unlike the above test we don't call commit - the XA_RDONLY means its finished, per XA semantics.
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

    /////////////

    public void testOnePhaseCommitSyncWithSeparateSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.doBeforeCompletion();
        tm.doOnePhaseCommit();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

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
            assertEquals("javax.transaction.RollbackException", e.getCause().getClass().getName());
            assertEquals(XAException.XA_RBROLLBACK, e.errorCode);
            // expected - we tried to commit a rollbackonly tx.
        }
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

    public void testRollbackSyncWithSeparateSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.doBeforeCompletion();
        tm.doRollback();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }


    public void testTwoPhaseCommitSyncWithSeparateSync() throws Exception
    {
        final SubordinateTransaction tm = createTransaction();
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.doBeforeCompletion();
        assertEquals(TwoPhaseOutcome.PREPARE_READONLY, tm.doPrepare());
        tm.doCommit();
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

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
        // note that unlike the above test we don't call commit - the XA_RDONLY means its finished, per XA semantics.
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, t.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

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
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, t.getStatus());
    }

}
