/*
 * SPDX short identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.transaction.Transaction;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.Implementations;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporter;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.XATerminatorImple;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailLocation;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

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

    @Test
    public void testRecovery() throws Exception {
        Implementations.initialise();
        XATerminatorImple xa = new XATerminatorImple();
        Xid[] recover = xa.recover(XAResource.TMSTARTRSCAN);
        int initialLength = recover == null ? 0 : recover.length;
        xa.recover(XAResource.TMENDRSCAN);

        
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();

        SubordinateTransaction importTransaction = imp.importTransaction(xid);

        importTransaction.enlistResource(new XAResource() {

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
                throw new XAException(XAException.XA_RETRY);
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
        });

        assertTrue(xa.beforeCompletion(xid));

        assertEquals(xa.prepare(xid), XAResource.XA_OK);

        try {
            xa.commit(xid, false);
            fail();
        } catch (XAException e) {
            assertTrue(e.errorCode == XAException.XAER_RMFAIL);
        }

        Xid[] recover2 = xa.recover(XAResource.TMSTARTRSCAN);
        assertTrue(recover2.length == initialLength+1);
        try {
            xa.commit(xid, false);
            fail();
        } catch (XAException e) {
            assertTrue("Wrong errorcode" + e.errorCode, e.errorCode == XAException.XAER_RMFAIL);
        }
        xa.recover(XAResource.TMENDRSCAN);

        // Feed the recovery manager with something it can recover with
        RecoveryModule module = new XARecoveryModule() {
            @Override
            public XAResource getNewXAResource(final XAResourceRecord xaResourceRecord) {
                return new XAResource() {

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
                };
            }
        };
        RecoveryManager.manager().addModule(module);
        try {
            Xid[] recover3 = xa.recover(XAResource.TMSTARTRSCAN);
            assertTrue(recover3.length == recover2.length);
            xa.commit(xid, false);
            xa.recover(XAResource.TMENDRSCAN);

            Xid[] recover4 = xa.recover(XAResource.TMSTARTRSCAN);
            assertTrue(recover4 == null || recover4.length == initialLength);
            xa.recover(XAResource.TMENDRSCAN);
        } finally {
            RecoveryManager.manager().removeModule(module, false);
        }
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
                    SubordinateTransaction stx = SubordinationManager.getTransactionImporter().importTransaction(xid);
                    completionCount.incrementAndGet();

                    return stx;
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        }, executor);
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
        XAResourceImple toCommit = new XAResourceImple(XAResource.XA_OK, XAResource.XA_OK);

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





    @Test
    public void testImportMultipleTx () throws XAException, RollbackException, SystemException {
        Implementations.initialise();

        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();

        SubordinateTransaction subordinateTransaction = imp.importTransaction(xid);

        XATerminatorImple xa = new XATerminatorImple();

        XAResourceImple xar1 = new XAResourceImple(XAResource.XA_OK, XAResource.XA_OK);
        XAResourceImple xar2 = new XAResourceImple(XAResource.XA_OK, XAException.XAER_RMFAIL);
        subordinateTransaction.enlistResource(xar1);
        subordinateTransaction.enlistResource(xar2);

        xa.prepare(xid);
        try {
            xa.commit(xid, false);
            fail("Did not expect to pass");
        } catch (XAException xae) {
            assertTrue(xae.errorCode == XAException.XAER_RMFAIL);
        }

        XARecoveryModule xarm = new XARecoveryModule();
        xarm.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String p) throws Exception {
                return false;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                return new XAResource[]{xar2};
            }
        });
        RecoveryManager.manager().addModule(xarm);

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
        xa.rollback(xid);
        assertTrue(xar2.rollbackCalled());
        xa.recover(XAResource.TMENDRSCAN);

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