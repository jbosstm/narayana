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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SimpleTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.jca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.Implementationsx;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jts.Implementations;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporter;
import com.arjuna.ats.internal.jta.transaction.jts.jca.XATerminatorImple;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class XATerminatorImpleUnitTest extends TestBase
{
    @Test
    public void testPrepareCommit () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();
        
        assertTrue(xa.beforeCompletion(xid));
        
        assertEquals(xa.prepare(xid), XAResource.XA_RDONLY);
        
        try
        {
            xa.commit(xid, false);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        imp.importTransaction(xid);
        
        xa.commit(xid, true);
    }
    
    @Test
    public void testOnePhaseCommit () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();

        xa.commit(xid, true);
    }
    
    @Test
    public void testPrepareAbort () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();
        
        assertEquals(xa.prepare(xid), XAResource.XA_RDONLY);
        
        try
        {
            xa.rollback(xid);
        }
        catch (final XAException ex)
        {
        }
    }
    
    @Test
    public void testAbort () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();

        xa.rollback(xid);
    }
    
    @Test
    public void testForget () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();

        xa.forget(xid);
    }
    
    @Test
    public void testRecover () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();

        xa.recover(XAResource.TMSTARTRSCAN);
        
        try
        {
            xa.recover(XAResource.TMSTARTRSCAN);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        xa.recover(XAResource.TMENDRSCAN);
    }
    
    @Test
    public void testNull () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();    
        XATerminatorImple xa = new XATerminatorImple();

        try
        {
            xa.beforeCompletion(xid);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            xa.prepare(xid);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            xa.commit(xid, false);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            xa.commit(xid, true);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            xa.rollback(xid);
            
            fail();
        }
        catch (final Exception ex)
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

        ArrayList<CompletableFuture<SubordinateTransaction>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < TASK_COUNT; i++)
            futures.add(doAsync(completionCount, gate, i < THREAD_COUNT, executor, xid));

        gate.await();

        SubordinateTransaction prevStx = null;

        for (CompletableFuture<SubordinateTransaction> future : futures) {
            SubordinateTransaction stx = future.get();
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
    private CompletableFuture<SubordinateTransaction> doAsync(
            final AtomicInteger completionCount, final CyclicBarrier gate, final boolean wait, ExecutorService executor, final XidImple xid) {
        return CompletableFuture.supplyAsync(new Supplier<SubordinateTransaction>() {
            @Override
            public SubordinateTransaction get() {
                try {
                    if (wait)
                        gate.await();

                    TransactionImporter imp = SubordinationManager.getTransactionImporter();
                    SubordinateTransaction stx = imp.importTransaction(xid);
                    completionCount.incrementAndGet();

                    return stx;
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        }, executor);
    }

    @Test
    public void testImportMultipleTx () throws XAException, RollbackException, SystemException {
        Implementations.initialise();
        Implementationsx.initialise();

        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();

        SubordinateTransaction subordinateTransaction = imp.importTransaction(xid);

        XATerminatorImple xa = new XATerminatorImple();

        XAResourceImple xar1 = new XAResourceImple(XAResource.XA_OK);
        XAResourceImple xar2 = new XAResourceImple(XAException.XAER_RMFAIL);
        subordinateTransaction.enlistResource(xar1);
        subordinateTransaction.enlistResource(xar2);

        xa.prepare(xid);
        try {
            xa.commit(xid, false);
            fail("Did not expect to pass");
        } catch (XAException xae) {
            assertTrue(xae.errorCode == XAException.XAER_RMFAIL);
        }

        Xid[] xids = xa.recover(XAResource.TMSTARTRSCAN);
        assertTrue(Arrays.binarySearch(xids, xid, new Comparator<Xid>() {
            @Override
            public int compare(Xid o1, Xid o2) {
                if (((XidImple)o1).equals(o2)) {
                    return 0;
                } else {
                    return -1;
                }
            }
        }) != -1);
        xa.rollback(xid); // Will throw a heuristic. The doRollback should be allowed but when we realise that the XAR1 is commited it shouldn't be allowed. Maybe we should also not shutdown the first XAR or maybe we need to rely on bottom up.
        assertTrue(xar2.rollbackCalled());
        xa.recover(XAResource.TMENDRSCAN);

    }

    private class XAResourceImple implements XAResource {

        private int commitException = 0;
        private boolean rollbackCalled;

        public XAResourceImple(int commitException) {
            this.commitException = commitException;
        }

        @Override
        public void commit(Xid xid, boolean b) throws XAException {
            if (commitException < 0) {
                throw new XAException(commitException);
            }
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
            return 0;
        }

        @Override
        public Xid[] recover(int i) throws XAException {
            return new Xid[0];
        }

        public boolean rollbackCalled()  {
            return rollbackCalled;
        }

        @Override
        public void rollback(Xid xid) throws XAException {
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
