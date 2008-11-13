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
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TxImporter;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.XATerminatorImple;
import com.arjuna.ats.jta.xa.XidImple;

import javax.transaction.RollbackException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.resource.spi.XATerminator;

public class SubordinateTestCase extends TestCase
{
	public void testCleanupCommit () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final TransactionImple tm = new TransactionImple(0); // implicit begin

			tm.doPrepare();
			tm.doCommit();
		}

		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}

	public void testCleanupRollback () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final TransactionImple tm = new TransactionImple(0); // implicit begin

			tm.doRollback();
		}

		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}

	public void testCleanupSecondPhaseRollback () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final TransactionImple tm = new TransactionImple(0); // implicit begin

			tm.doPrepare();
			tm.doRollback();
		}

		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}

	public void testCleanupOnePhaseCommit () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final TransactionImple tm = new TransactionImple(0); // implicit begin

			tm.doOnePhaseCommit();
		}

		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}

    /////////////

    public void testOnePhaseCommitSync() throws Exception
    {
        final TransactionImple tm = new TransactionImple(0);
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
        final TransactionImple tm = TxImporter.importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        final XATerminator xaTerminator = new XATerminatorImple();
        xaTerminator.commit(xid, true);
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

    public void testOnePhaseCommitSyncWithRollbackOnly() throws Exception
    {
        final TransactionImple tm = new TransactionImple(0);
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
        final TransactionImple tm = TxImporter.importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.setRollbackOnly();
        final XATerminator xaTerminator = new XATerminatorImple();
        try {
            xaTerminator.commit(xid, true);
            tm.doOnePhaseCommit();
            fail("did not get expected rollback exception");
        } catch(XAException e) {
            assertEquals("javax.transaction.RollbackException", e.getCause().getClass().getName());
            assertEquals(XAException.XA_RBROLLBACK, e.errorCode);
            // expected - we tried to commit a rollbackonly tx.
        }
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

    public void testRollbackSync() throws Exception
    {
        final TransactionImple tm = new TransactionImple(0);
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
        final TransactionImple tm = TxImporter.importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        final XATerminator xaTerminator = new XATerminatorImple();
        xaTerminator.rollback(xid);
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }

    public void testTwoPhaseCommitSync() throws Exception
    {
        final TransactionImple tm = new TransactionImple(0);
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
        final TransactionImple tm = TxImporter.importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        final XATerminator xaTerminator = new XATerminatorImple();
        assertEquals(XAResource.XA_RDONLY, xaTerminator.prepare(xid));
        // note that unlike the above test we don't call commit - the XA_RDONLY means its finished, per XA semantics.
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

    public void testTwoPhaseCommitSyncWithXAOK() throws Exception
    {
        final TransactionImple tm = new TransactionImple(0);
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
        final TransactionImple tm = TxImporter.importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        final TestXAResource xaResource = new TestXAResource();
        xaResource.setPrepareReturnValue(XAResource.XA_OK);
        tm.enlistResource(xaResource);
        final XATerminator xaTerminator = new XATerminatorImple();
        assertEquals(XAResource.XA_OK, xaTerminator.prepare(xid));
        xaTerminator.commit(xid, false);
        assertTrue(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_COMMITTED, tm.getStatus());
    }

    public void testTwoPhaseCommitSyncWithRollbackOnly() throws Exception
    {
        final TransactionImple tm = new TransactionImple(0);
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
        final TransactionImple tm = TxImporter.importTransaction(xid);
        final TestSynchronization sync = new TestSynchronization();
        tm.registerSynchronization(sync);
        tm.setRollbackOnly();
        final XATerminator xaTerminator = new XATerminatorImple();

        try {
            xaTerminator.prepare(xid);
        } catch(XAException e) {
            assertEquals(XAException.XA_RBROLLBACK, e.errorCode);
            // expected - we tried to prepare a rollbackonly tx.
        }
        // no need to call rollback - the XA_RBROLLBACK code indicates it's been done.
        assertFalse(sync.isBeforeCompletionDone());
        assertTrue(sync.isAfterCompletionDone());
        assertEquals(javax.transaction.Status.STATUS_ROLLEDBACK, tm.getStatus());
    }
}
