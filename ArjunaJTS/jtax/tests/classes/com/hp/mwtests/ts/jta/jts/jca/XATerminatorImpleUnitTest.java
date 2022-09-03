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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;
import com.arjuna.ats.internal.jta.Implementationsx;
import com.arjuna.ats.internal.jta.recovery.arjunacore.RecoveryXids;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporter;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.jts.jca.XATerminatorImple;
import com.arjuna.ats.internal.jta.utils.jts.XidUtils;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.Implementations;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteServerTransaction;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.extensions.AtomicTransaction;
import com.hp.mwtests.ts.jta.jts.TestXAResource;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

import org.junit.Assert;
import org.junit.Test;

import javax.management.ObjectName;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class XATerminatorImpleUnitTest extends TestBase
{
    private Xid failedResourceXid;

    @Test
    public void testXARMERR () throws Exception {
        Uid uid = new Uid();
        XidImple xid = new XidImple(uid);
        TransactionImporter imp = SubordinationManager.getTransactionImporter();

        SubordinateTransaction subordinateTransaction = imp.importTransaction(xid);
        Uid savingUid = getImportedSubordinateTransactionUid(subordinateTransaction);

        subordinateTransaction.enlistResource(new TestXAResource() {
            @Override
            public void commit(Xid xid, boolean b) throws XAException {
                this.xid = null;
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                return 0;
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                fail("Resource was rolled back");
            }
        });

        subordinateTransaction.enlistResource(new TestXAResource() {
            @Override
            public void commit(Xid xid, boolean b) throws XAException {
                throw new XAException(XAException.XA_HEURHAZ);
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                failedResourceXid = xid;
                return 0;
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                fail("Resource was rolled back");
            }
        });

        XATerminatorImple xa = new XATerminatorImple();
        xa.prepare(xid);
        try {
            xa.commit(xid, false);
            fail("Expecting heuristic mixed exception being thrown on commit");
        } catch (final XAException ex) {
            assertEquals(XAException.XA_HEURMIX, ex.errorCode);
        }
        try {
            xa.commit(xid, false);
        } catch (XAException e) {
            assertEquals(XAException.XA_RETRY, e.errorCode);
        }

        ObjStoreBrowser osb = new ObjStoreBrowser();
        osb.viewSubordinateAtomicActions(true);
        osb.setExposeAllRecordsAsMBeans(true);
        osb.start();
        osb.probe();

        Set<ObjectName> participants = JMXServer.getAgent().queryNames(osb.getObjStoreBrowserMBeanName() + ",itype=" + com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator.ServerTransaction.getType().substring(1) +",uid="+savingUid.stringForm().replaceAll(":", "_")+",puid=*", null);
        assertEquals(1, participants.size());
        JMXServer.getAgent().getServer().invoke(participants.iterator().next(), "clearHeuristic", null, null);
        xa.recover(XAResource.TMSTARTRSCAN);
        xa.recover(XAResource.TMENDRSCAN);



        Set<ObjectName> xaResourceRecords = JMXServer.getAgent().queryNames(osb.getObjStoreBrowserMBeanName() + ",itype=" + XAResourceRecord.typeName().substring(1) +",uid=*", null);
        for (ObjectName xaResourceRecord : xaResourceRecords) {

            Object getGlobalTransactionId = JMXServer.getAgent().getServer().getAttribute(xaResourceRecord, "GlobalTransactionId");
            Object getBranchQualifier = JMXServer.getAgent().getServer().getAttribute(xaResourceRecord, "BranchQualifier");

            if (Arrays.equals(failedResourceXid.getGlobalTransactionId(), (byte[]) getGlobalTransactionId) && Arrays.equals(failedResourceXid.getBranchQualifier(), (byte[]) getBranchQualifier)) {

                Object getHeuristicValue = JMXServer.getAgent().getServer().getAttribute(xaResourceRecord, "HeuristicValue");
                assertTrue(getHeuristicValue.equals(6));
                JMXServer.getAgent().getServer().invoke(xaResourceRecord, "clearHeuristic", null, null);
            }

        }
        XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        xaRecoveryModule.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String p) throws Exception {
                return false;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                return new XAResource[] {
                        new TestXAResource()  {
                            public Xid[] recover(int var) throws XAException {
                                if (var == XAResource.TMSTARTRSCAN) {
                                    if (failedResourceXid != null) {
                                        return new Xid[]{failedResourceXid};
                                    }
                                }
                                return new Xid[0];
                            }
                            @Override
                            public void commit(Xid xid, boolean b) throws XAException {
                                failedResourceXid = null;
                            }

                            @Override
                            public int prepare(Xid xid) throws XAException {
                                return 0;
                            }

                            @Override
                            public void rollback(Xid xid) throws XAException {
                                fail("Resource was rolled back");
                            }
                        }
                };
            }
        });
        xaRecoveryModule.periodicWorkFirstPass();
        Field safetyIntervalMillis = RecoveryXids.class.getDeclaredField("safetyIntervalMillis");
        safetyIntervalMillis.setAccessible(true);
        Object o1 = safetyIntervalMillis.get(null);
        safetyIntervalMillis.set(null, 0);
        try {
            xaRecoveryModule.periodicWorkSecondPass();
        } finally {
            safetyIntervalMillis.set(null, o1);
        }

        xa.recover(XAResource.TMSTARTRSCAN);
        try {
            xa.commit(xid, false);
            Assert.fail("Expecting XAException being thrown indicating more recover to be called");
        } catch (XAException expected) {
            Assert.assertTrue("On commit XAException error code indicating more recover call is expected but it's " + expected.errorCode,
                XAException.XA_RETRY == expected.errorCode || XAException.XAER_RMFAIL == expected.errorCode);
        } finally {
            xa.recover(XAResource.TMENDRSCAN);
        }
        assertNull(failedResourceXid);
    }

    @Test
    public void testXARetry () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();

        SubordinateTransaction subordinateTransaction = imp.importTransaction(xid);

        XATerminatorImple xa = new XATerminatorImple();
        subordinateTransaction.enlistResource(new XAResource() {
            @Override
            public void commit(Xid xid, boolean b) throws XAException {

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

            @Override
            public void rollback(Xid xid) throws XAException {

            }

            @Override
            public boolean setTransactionTimeout(int i) throws XAException {
                return false;
            }

            @Override
            public void start(Xid xid, int i) throws XAException {

            }
        });

        subordinateTransaction.enlistResource(new XAResource() {
            boolean firstAttempt = true;
            @Override
            public void commit(Xid xid, boolean b) throws XAException {
                if (firstAttempt) {
                    try {
                        failedResourceXid = xid;
                        throw new XAException(XAException.XA_RETRY);
                    } finally {
                        firstAttempt = false;
                    }
                }
                if (failedResourceXid != null) {
                    failedResourceXid = null;
                } else {
                    throw new XAException(XAException.XAER_PROTO);
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

            @Override
            public void rollback(Xid xid) throws XAException {

            }

            @Override
            public boolean setTransactionTimeout(int i) throws XAException {
                return false;
            }

            @Override
            public void start(Xid xid, int i) throws XAException {

            }
        });

        assertEquals(xa.prepare(xid), XAResource.XA_OK);

        try
        {
            xa.commit(xid, false);
            fail("Expecting XATerminator throwing XAException as commit should fail"
                + " as TestXAResource was instructed to throw an exception");
        }
        catch (final XAException ex)
        {
            assertEquals("XATerminator commit should throw XAER_RMFAIL when commit fails",
                XAException.XAER_RMFAIL, ex.errorCode);
        }
        Implementationsx.initialise();
        xa.recover(XAResource.TMSTARTRSCAN);
        assertNotNull(failedResourceXid);
        try {
            xa.commit(xid, false);
        } catch (XAException expected) {
            Assert.assertTrue("On commit XAException error code indicating more recover call is expected but it's " + expected.errorCode,
                XAException.XA_RETRY == expected.errorCode || XAException.XAER_RMFAIL == expected.errorCode);
        } finally {
            xa.recover(XAResource.TMENDRSCAN);
        }
        assertNull(failedResourceXid);
    }

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

    @Test
    public void testDoRecover () throws Exception {
        Implementations.initialise();
        Implementationsx.initialise();

        Xid xid1 = XidUtils.getXid(new Uid(), false);
        Xid xid2 = XidUtils.getXid(new Uid(), false);

        TransactionImporter imp = SubordinationManager.getTransactionImporter();

        SubordinateTransaction subTxn1 = imp.importTransaction(xid1);
        SubordinateTransaction subTxn2 = imp.importTransaction(xid2);

        XATerminatorImple xaTerminator = new XATerminatorImple();
        subTxn1.enlistResource(new XAResourceImple(XAResource.XA_OK));
        subTxn2.enlistResource(new XAResourceImple(XAResource.XA_OK));

        try {
            subTxn1.doPrepare();
            subTxn2.doPrepare();

            Xid[] xidsAll = xaTerminator.doRecover(null, null);

            Assert.assertNotNull("expecting some unfinished transactions to be returned but they are null", xidsAll);
            Assert.assertTrue("expecting some unfinished transactions to be returned but there is none", xidsAll.length > 1);
        } finally {
            subTxn1.doCommit();
            subTxn2.doCommit();
        }
    }

    @Test
    public void testMoveToAssumedComplete() throws Exception {
        Implementations.initialise();
        Implementationsx.initialise();

        Uid uid = new Uid();
        Xid xid = XidUtils.getXid(uid, false);

        XATerminatorImple xaTerminator = new XATerminatorImple();
        TransactionImporter importer = SubordinationManager.getTransactionImporter();
        SubordinateTransaction subordinateTransaction = importer.importTransaction(xid);

        Uid subordinateTransactionUid = getImportedSubordinateTransactionUid(subordinateTransaction);

        com.hp.mwtests.ts.jta.subordinate.TestXAResource xares = new com.hp.mwtests.ts.jta.subordinate.TestXAResource();
        xares.setCommitException(new XAException(XAException.XAER_RMFAIL));
        subordinateTransaction.enlistResource(xares);
        subordinateTransaction.doPrepare();
        boolean commitFailed = subordinateTransaction.doCommit();
        Assert.assertFalse("Commit should fail as XAResource defined XAException on commit being thrown", commitFailed);

        int assumedCompletedRetryOriginalValue = jtsPropertyManager.getJTSEnvironmentBean().getCommitedTransactionRetryLimit();
        jtsPropertyManager.getJTSEnvironmentBean().setCommitedTransactionRetryLimit(1);
        try {
            SubordinateTransaction recoveredTxn = importer.recoverTransaction(subordinateTransactionUid);
            Xid xidRecovered = recoveredTxn.baseXid();
            Assert.assertEquals("recovered subordinate xid should be equal to imported one", xid, xidRecovered);

            Runnable runCommitExpectingException = () -> {
                try {
                    xaTerminator.recover(XAResource.TMSTARTRSCAN); // importing transaction
                    xaTerminator.recover(XAResource.TMENDRSCAN);
                    xaTerminator.commit(xid, false); // try to commit the imported transaction
                    Assert.fail("XAException is expected to be thrown as txn was not yet moved to assumed complete state");
                } catch (XAException expected) {
                    Assert.assertTrue("Commit expect to throw exception indicating that othe commit call is expected, but error code is " + expected.errorCode,
                        XAException.XA_RETRY == expected.errorCode || XAException.XAER_RMFAIL == expected.errorCode);
                }
            };
            runCommitExpectingException.run(); // replay first
            runCommitExpectingException.run(); // assume completed check first time

            xaTerminator.recover(XAResource.TMSTARTRSCAN); // importing transaction
            xaTerminator.recover(XAResource.TMENDRSCAN);
            xaTerminator.commit(xid, false); // moving to assumed completed state
        } finally {
            jtsPropertyManager.getJTSEnvironmentBean().setCommitedTransactionRetryLimit(assumedCompletedRetryOriginalValue);
        }

        try {
            importer.recoverTransaction(subordinateTransactionUid);
            Assert.fail("Transaction '" + subordinateTransaction + "' should fail to recover as it should be moved "
                + "to category AssumedCompleteServerTrasactions");
        } catch (IllegalArgumentException expected) {
        }

        ObjectStoreIterator objectStoreIterator = new ObjectStoreIterator(StoreManager.getRecoveryStore(),
            AssumedCompleteServerTransaction.typeName());

        List<Uid> assumedCompletedUids = new ArrayList<Uid>();
        Uid iteratedUid = objectStoreIterator.iterate();
        while(Uid.nullUid().notEquals(iteratedUid)) {
            assumedCompletedUids.add(iteratedUid);
            iteratedUid = objectStoreIterator.iterate();
        }
        Assert.assertTrue("the subordinate transaction has to be moved under assumed completed in object store",
            assumedCompletedUids.contains(subordinateTransactionUid));
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

    /**
     * This is required because it JTS records are stored with a dynamic _savingUid
     * Normally they are recovered using XATerminator but for this test I would like to stick to testing
     * transaction importer
     */
    private Uid getImportedSubordinateTransactionUid(SubordinateTransaction subordinateTransaction) throws Exception {
        Field field = TransactionImple.class.getDeclaredField("_theTransaction");
        field.setAccessible(true);
        Object o = field.get(subordinateTransaction);
        field = AtomicTransaction.class.getDeclaredField("_theAction");
        field.setAccessible(true);
        o = field.get(o);
        field = ControlWrapper.class.getDeclaredField("_controlImpl");
        field.setAccessible(true);
        o = field.get(o);
        field = ControlImple.class.getDeclaredField("_transactionHandle");
        field.setAccessible(true);
        o = field.get(o);
        field = ServerTransaction.class.getDeclaredField("_savingUid");
        field.setAccessible(true);
        Uid subordinateTransactionUid = (Uid) field.get(o);
        return subordinateTransactionUid;
    }
}
