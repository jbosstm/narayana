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
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.junit.Ignore;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.XATerminatorImple;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailLocation;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailType;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class XATerminatorUnitTest
{
    @Test
    public void test () throws Exception
    {
        XATerminatorImple term = new XATerminatorImple();
        XidImple xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        
        assertTrue(term.beforeCompletion(xid));
        assertEquals(term.prepare(xid), XAResource.XA_RDONLY);
        
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        
        term.commit(xid, true);
        
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        
        term.rollback(xid);
        
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        
        term.recover(XAResource.TMSTARTRSCAN);
        
        try
        {
            term.recover(XAResource.TMSTARTRSCAN);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        term.recover(XAResource.TMENDRSCAN);
        
        term.forget(xid);
    }
    
    @Test
    public void testFail () throws Exception
    {
        XATerminatorImple term = new XATerminatorImple();
        XidImple xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        
        SubordinateTransaction tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.rollback));
        
        try
        {
            term.commit(xid, false);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.heurcom));
        
        try
        {
            term.commit(xid, false);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.heurcom));
        
        term.prepare(xid);
        
        try
        {
            term.commit(xid, false);
        }
        catch (final XAException ex)
        {
            fail();
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.normal));
        
        try
        {
            term.commit(xid, false);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.rollback, FailType.rollback));
        
        try
        {
            term.rollback(xid);
        }
        catch (final XAException ex)
        {
            fail();
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.rollback, FailType.heurcom));
        
        term.prepare(xid);
        
        try
        {
            term.rollback(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.rollback, FailType.normal));
        
        term.prepare(xid);
        
        try
        {
            term.rollback(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.prepare_and_rollback, FailType.normal));

        try
        {
            term.prepare(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.prepare_and_rollback, FailType.heurcom));

        try
        {
            term.prepare(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.prepare_and_rollback, FailType.rollback));

        try
        {
            term.prepare(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
    }
    
    @Test
    public void testUnknownTransaction () throws Exception
    {
        XATerminatorImple term = new XATerminatorImple();
        XidImple xid = new XidImple(new Uid());
        
        try
        {
            term.beforeCompletion(xid);
            
            fail();
        }
        catch (final UnexpectedConditionException ex)
        {
        }
        
        try
        {
            term.prepare(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        try
        {
            term.commit(xid, false);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        try
        {
            term.rollback(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        try
        {
            term.forget(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
    }
    
    @Test
    public void testInvalid () throws Exception
    {
        XATerminatorImple term = new XATerminatorImple();
        XidImple xid = new XidImple(new Uid());
        
        try
        {
            SubordinationManager.getTransactionImporter().importTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            SubordinationManager.getTransactionImporter().recoverTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            SubordinationManager.getTransactionImporter().getImportedTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            SubordinationManager.getTransactionImporter().removeImportedTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        Uid uid = new Uid();
        
        try
        {
            Object obj = SubordinationManager.getTransactionImporter().recoverTransaction(uid);
        
            fail();
        }
        catch (IllegalArgumentException ex)
        {
        }
    }

    @Test
    public void testConcurrentImport () throws Exception {
        AtomicInteger completionCount = new AtomicInteger(0);
        XidImple xid = new XidImple(new Uid());

        final int TASK_COUNT = 400;
        final int THREAD_COUNT = 200;
        final CyclicBarrier gate = new CyclicBarrier(THREAD_COUNT + 1);
        final AtomicInteger gateOut = new AtomicInteger();

        ArrayList<SubordinateTransaction> futures = new ArrayList<SubordinateTransaction>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < TASK_COUNT; i++)
            doAsync(completionCount, gate, i < THREAD_COUNT, executor, xid, futures, gateOut);

        gate.await();

        SubordinateTransaction prevStx = null;



        synchronized (gateOut) {
            while (gateOut.get() < TASK_COUNT) {
                gateOut.wait();
            }
        }

        for (SubordinateTransaction stx : futures) {
            if (stx == null) {
                fail("transaction import returned null for future ");
            } else {
                if (prevStx != null)
                    assertEquals("transaction import for same xid returned a different instance", stx, prevStx);
                else
                    prevStx = stx;
            }
        }

        assertEquals("some transaction import futures did not complete", completionCount.get(), TASK_COUNT);
    }

    /*
     * import a transaction asynchronously to maximise the opportunity for concurrency errors in TransactionImporterImple
     */
    private void doAsync(
            final AtomicInteger completionCount, final CyclicBarrier gate, final boolean wait, ExecutorService executor, final XidImple xid, final ArrayList<SubordinateTransaction> futures, final AtomicInteger gateOut) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (wait)
                        gate.await();
                    SubordinateTransaction stx = SubordinationManager.getTransactionImporter().importTransaction(xid);
                    completionCount.incrementAndGet();

                    synchronized (futures) {
                        futures.add(stx);
                    }
                    gateOut.incrementAndGet();
                    synchronized(gateOut) {
                        gateOut.notify();
                    }
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        });
    }


    @Test
    public void testCommitMid () throws Exception
    {

        TransactionManagerImple tm = new TransactionManagerImple();

        RecordTypeManager.manager().add(new RecordTypeMap() {
            @SuppressWarnings("unchecked")
            public Class getRecordClass ()
            {
                return XAResourceRecord.class;
            }

            public int getType ()
            {
                return RecordType.JTA_RECORD;
            }
        });

        XATerminatorImple xaTerminator = new XATerminatorImple();
        XidImple xid = new XidImple(new Uid());
        final XAResourceImple toCommit = new XAResourceImple(XAResource.XA_OK, XAResource.XA_OK);

        {
            SubordinateTransaction subordinateTransaction = SubordinationManager.getTransactionImporter().importTransaction(xid);
            tm.resume(subordinateTransaction);
            subordinateTransaction.enlistResource(new XAResourceImple(XAResource.XA_RDONLY, XAResource.XA_OK));
            subordinateTransaction.enlistResource(toCommit);
            Transaction suspend = tm.suspend();
        }

        {
            SubordinateTransaction subordinateTransaction = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
            tm.resume(subordinateTransaction);
            subordinateTransaction.doPrepare();
            Transaction suspend = tm.suspend();
        }

        XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        xaRecoveryModule.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String p) throws Exception {
                return false;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                return new XAResource[] {toCommit};
            }
        });
        RecoveryManager.manager().addModule(xaRecoveryModule);
        xaTerminator.doRecover(null, null);

        {
            SubordinateTransaction subordinateTransaction = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
            tm.resume(subordinateTransaction);
            subordinateTransaction.doCommit();
            tm.suspend();
        }
        RecoveryManager.manager().removeModule(xaRecoveryModule, false);

        assertTrue(toCommit.wasCommitted());

        SubordinationManager.getTransactionImporter().removeImportedTransaction(xid);
    }



    private class XAResourceImple implements XAResource {

        private final int prepareFlag;
        private boolean committed;
        private final int commitException;
        private boolean rollbackCalled;
        private Xid xid;

        public XAResourceImple(int prepareFlag, int commitException) {
            this.prepareFlag = prepareFlag;
            this.commitException = commitException;
        }

        @Override
        public void commit(Xid xid, boolean b) throws XAException {
            committed = true;
            if (commitException < 0) {
                throw new XAException(commitException);
            } else {
                this.xid = null;
            }
        }

        boolean wasCommitted() {
            return committed;
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
            this.xid = xid;
            return prepareFlag;
        }

        @Override
        public Xid[] recover(int i) throws XAException {
            if (xid != null) {
                return new Xid[]{xid};
            }
            return new Xid[0];
        }

        public boolean rollbackCalled()  {
            return rollbackCalled;
        }

        @Override
        public void rollback(Xid xid) throws XAException {
            this.xid = null;
            rollbackCalled = true;
        }

        @Override
        public boolean setTransactionTimeout(int i) throws XAException {
            return false;
        }

        @Override
        public void start(Xid xid, int i) throws XAException {

        }
    }
}
