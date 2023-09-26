/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.recovery.arjunacore.*;
import org.jboss.tm.XAResourceWrapper;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.recovery.RecoverAtomicAction;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.arjunacore.AtomicAction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.TransactionImple;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.RecoveryXAResource;

public class XARecoveryModuleUnitTest
{
    private boolean rolledback;

    @Test
    public void testNull ()
    {
        XARecoveryModule xarm = new XARecoveryModule();
        
        xarm.periodicWorkFirstPass();
        xarm.periodicWorkSecondPass();
        
        assertNotNull(xarm.id());
    }

    
    @Test
    public void testRecoverFromMultipleXAResourceRecovery() throws Exception {
        // Make sure the file doesn't exist
        assertFalse(new File("XARR.txt").exists());

        AtomicAction aa = new AtomicAction();
        aa.begin();
        assertEquals(AddOutcome.AR_ADDED, aa.add(new XAResourceRecord(null, new XARRTestResource(), new XidImple(aa), null)));

        Class<BasicAction> c = BasicAction.class;
        Method method = c.getDeclaredMethod("prepare", boolean.class);
        method.setAccessible(true);
        int result = (Integer) method.invoke(aa, new Object[] { true });
        assertEquals(result, TwoPhaseOutcome.PREPARE_OK);

        // Make sure the file exists
        assertTrue(new File("XARR.txt").exists());

        RecordTypeManager.manager().add(new RecordTypeMap() {
                public Class<XAResourceRecord> getRecordClass ()
                {
                    return XAResourceRecord.class;
                }
                
                public int getType ()
                {
                    return RecordType.JTA_RECORD;
                }
        });
        
        List<String> xarn = new ArrayList<String>();
        xarn.add(NodeNameXAResourceOrphanFilter.RECOVER_ALL_NODES);
        
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(xarn);
        XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        Field safetyIntervalMillis = RecoveryXids.class.getDeclaredField("safetyIntervalMillis");
        safetyIntervalMillis.setAccessible(true);
        safetyIntervalMillis.set(null, 0);
        xaRecoveryModule.addXAResourceRecoveryHelper(new XARROne());
        xaRecoveryModule.addXAResourceRecoveryHelper(new XARRTwo());
        xaRecoveryModule.addXAResourceOrphanFilter(new com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter());
        xaRecoveryModule.addXAResourceOrphanFilter(new com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter());
        RecoveryManager.manager().addModule(xaRecoveryModule);
        
        
        // This is done rather than using the AtomicActionRecoveryModule as the transaction is inflight
        RecoverAtomicAction rcvAtomicAction = new RecoverAtomicAction(aa.get_uid(), ActionStatus.COMMITTED);
        rcvAtomicAction.replayPhase2();
        
        // The XARM would execute next
        xaRecoveryModule.periodicWorkFirstPass();
        xaRecoveryModule.periodicWorkSecondPass();

        // Make sure the file doesn't exist
        assertFalse(new File("XARR.txt").exists());
        
        aa.abort();
    }
    
    @Test
    public void testRecover () throws Exception
    {
        ArrayList<String> r = new ArrayList<String>();
        TransactionImple tx = new TransactionImple(0);
        
        assertTrue(tx.enlistResource(new RecoveryXAResource()));
        
        assertEquals(tx.doPrepare(), TwoPhaseOutcome.PREPARE_OK);
        
        r.add("com.hp.mwtests.ts.jta.recovery.DummyXARecoveryResource");

        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(r);
        
        XARecoveryModule xarm = new XARecoveryModule();

        assertNull(xarm.getNewXAResource( new XAResourceRecord(null, null, new XidImple(), null) ));
        
        for (int i = 0; i < 11; i++)
        {
            xarm.periodicWorkFirstPass();
            xarm.periodicWorkSecondPass();
        }
        
        assertTrue(xarm.getNewXAResource(  new XAResourceRecord(null, null, new XidImple(new Uid()), null) ) == null);
        
        assertNull(xarm.getNewXAResource( new XAResourceRecord(null, null, new XidImple(), null) ));
    }
    
    @Test
    public void testFailures () throws Exception
    {
        XARecoveryModule xarm = new XARecoveryModule();       
        Class<?>[] parameterTypes = new Class[2];
        Uid u = new Uid();
        Xid x = new XidImple();
        
        parameterTypes[0] = Xid.class;
        parameterTypes[1] = Uid.class;
      
        Method m = xarm.getClass().getDeclaredMethod("addFailure", parameterTypes);
        m.setAccessible(true);
      
        Object[] parameters = new Object[2];
        parameters[0] = x;
        parameters[1] = u;
      
        m.invoke(xarm, parameters);
        
        parameterTypes = new Class[1];
        parameterTypes[0] = Xid.class;
        
        parameters = new Object[1];
        parameters[0] = x;
        
        m = xarm.getClass().getDeclaredMethod("previousFailure", parameterTypes);
        m.setAccessible(true);
        
        Uid ret = (Uid) m.invoke(xarm, parameters);
        
        assertEquals(ret, u);
        
        parameterTypes = new Class[2];
        parameterTypes[0] = Xid.class;
        parameterTypes[1] = Uid.class;
        
        parameters = new Object[2];
        parameters[0] = x;
        parameters[1] = u;
        
        m = xarm.getClass().getDeclaredMethod("removeFailure", parameterTypes);
        m.setAccessible(true);
        
        m.invoke(xarm, parameters);
               
        m = xarm.getClass().getDeclaredMethod("clearAllFailures", (Class[]) null);
        m.setAccessible(true);
        
        m.invoke(xarm, (Object[]) null);
    }

    @Test
    public void testXAResourceRecoveryHelperRegistration() {

        TestXAResource testXAResource = new TestXAResource(new XidImple());
        XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        XAResourceRecoveryHelper xaResourceRecoveryHelper
            = new RegistrationTestXAResourceRecoveryHelper().set(testXAResource);

        xaRecoveryModule.addXAResourceRecoveryHelper(xaResourceRecoveryHelper);
        xaRecoveryModule.periodicWorkFirstPass();
        xaRecoveryModule.periodicWorkSecondPass();
        assertEquals("XAResource.recover() has to be called once for each pass, "
                + "the resource was provided by xa resource recovery helper",
                2, testXAResource.recoveryCount());

        xaRecoveryModule.removeXAResourceRecoveryHelper(xaResourceRecoveryHelper);
        xaRecoveryModule.periodicWorkFirstPass();
        xaRecoveryModule.periodicWorkSecondPass();
        assertEquals("Recovery helper should be removed and the test xa resource should not be provided",
                2, testXAResource.recoveryCount());
    }
    
    @Test
    public void testXAResourceRecoveryHelperDeregisterLocking() throws InterruptedException, ExecutionException {

        Xid testXid = new XidImple();
        TestXAResource testXAResource = new TestXAResource(testXid);
        XAResourceRecord recoveryRecord = new XAResourceRecord(null, testXAResource, testXid, null);
        XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        XAResourceRecoveryHelper xaResourceRecoveryHelper
            = new RegistrationTestXAResourceRecoveryHelper().set(testXAResource);

        // registration of the recovery helper to setup the testXAResource during first pass
        xaRecoveryModule.addXAResourceRecoveryHelper(xaResourceRecoveryHelper);

        // start with recovery and going to state ScanState.BETWEEN_PASSES 
        xaRecoveryModule.periodicWorkFirstPass();

        // as we get the XAResource to further use the XARecoveryModule.isHelperInUse
        //   should returning false during removing xarecovery helper
        XAResource newXAResource = xaRecoveryModule.getNewXAResource(recoveryRecord);
        assertEquals("Expecting to get the resource registered by helper because the first pass was run just now",
                testXAResource, newXAResource);

        // simulating two processes - periodic recovery and other stopping TM processing
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> futureRemove = executor
                .submit(() -> xaRecoveryModule.removeXAResourceRecoveryHelper(xaResourceRecoveryHelper));
        Future<?> futureRecovery = executor
                .submit(() -> xaRecoveryModule.periodicWorkSecondPass());
        try {
            futureRemove.get(60, TimeUnit.SECONDS);
            futureRecovery.get(60, TimeUnit.SECONDS);
        } catch (TimeoutException timeoutE) {
            executor.shutdownNow(); // Cancel currently executing tasks
            fail("Remove and recovery calls were not finished. "
                    + "They are potentionally (dead)locking each other.");
        }
    }

    private class RegistrationTestXAResourceRecoveryHelper implements XAResourceRecoveryHelper {
        private XAResource[] xaResourceToReturn;

        @Override
        public boolean initialise(String p) throws Exception
        {
            return false;
        }

        @Override
        public XAResource[] getXAResources() throws Exception
        {
            return xaResourceToReturn;
        }

        public RegistrationTestXAResourceRecoveryHelper set(XAResource... xaResources) {
            if(xaResources == null) {
                xaResourceToReturn = new XAResource[] {};
            }
            xaResourceToReturn = xaResources;
            return this;
        }
    }

    @Test
    public void testXAResourceOrphanFilterRegistration() {

        XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        XAResourceOrphanFilter xaResourceOrphanFilter = new DummyXAResourceOrphanFilter();

        xaRecoveryModule.addXAResourceOrphanFilter(xaResourceOrphanFilter);
        xaRecoveryModule.removeXAResourceOrphanFilter(xaResourceOrphanFilter);
    }
    
    @Test
    public void testXAResourceOrphanFilter () throws Exception
    {
        XAResourceOrphanFilter xaResourceOrphanFilter = new DummyXAResourceOrphanFilter(XAResourceOrphanFilter.Vote.ROLLBACK);
    
        XARecoveryModule xarm = new XARecoveryModule();
        
        xarm.addXAResourceOrphanFilter(xaResourceOrphanFilter);
        
        Class<?>[] parameterTypes = new Class[2];
        
        parameterTypes[0] = NameScopedXAResource.class;
        parameterTypes[1] = Xid.class;
        
        Method m = xarm.getClass().getDeclaredMethod("handleOrphan", parameterTypes);
        m.setAccessible(true);
        
        Object[] parameters = new Object[2];
        parameters[0] = new NameScopedXAResource(new RecoveryXAResource(), null);
        parameters[1] = new XidImple();
        
        m.invoke(xarm, parameters);
    }


    @Test
    public void testCanRepeatFirstPass () throws Exception
    {

        XARecoveryModule xarm = new XARecoveryModule();


        xarm.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String p) throws Exception {
                return false;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                return new XAResource[]{new XAResource() {

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
                        recoverCalled++;
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
                }};
            }
        });

        xarm.periodicWorkFirstPass();
        assertEquals(recoverCalled, 1);
        xarm.periodicWorkSecondPass();
        assertEquals(recoverCalled, 2);
        xarm.periodicWorkFirstPass();
        assertEquals(recoverCalled, 3);
        xarm.periodicWorkFirstPass();
        assertEquals(recoverCalled, 5);
        xarm.periodicWorkSecondPass();
        assertEquals(recoverCalled, 6);
    }

    @Test
    public void testRecoverPassFailure() throws Exception {
        int orphanSafetyInterval = jtaPropertyManager.getJTAEnvironmentBean().getOrphanSafetyInterval();
        List<String> xaRecoveryNodes = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(0);
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(Arrays.asList(new String[]{NodeNameXAResourceOrphanFilter.RECOVER_ALL_NODES}));
        XARecoveryModule xarm = new XARecoveryModule();
        xarm.addXAResourceOrphanFilter(new JTANodeNameXAResourceOrphanFilter());
        xarm.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
            XAResource[] xares = new XAResource[]{new XAResourceWrapper() {
                @Override
                public XAResource getResource() {
                    return null;
                }

                @Override
                public String getProductName() {
                    return null;
                }

                @Override
                public String getProductVersion() {
                    return null;
                }

                @Override
                public String getJndiName() {
                    return "test";
                }

                int count = 0;
                Xid xid = new XidImple(new Uid());

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
                    count++;
                    if (count == 1 || count == 5) {
                        return new Xid[]{xid};
                    } else if (count > 5) {
                        return new Xid[0];
                    } else {
                        throw new XAException();
                    }
                }

                @Override
                public void rollback(Xid xid) throws XAException {
                    if (count == 1) { // This comes from the first end scan
                        throw new XAException(XAException.XA_RETRY);
                    }
                    rolledback = true;
                }

                @Override
                public boolean setTransactionTimeout(int i) throws XAException {
                    return false;
                }

                @Override
                public void start(Xid xid, int i) throws XAException {

                }
            }
            };

            @Override
            public boolean initialise(String p) throws Exception {
                return false;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                return xares;
            }
        });


        // The first two recovery cycles do nothing with the resource (because phase two is getting the exception)
        // When count reaches 6 it sees that the xid has gone and presumes abort so calls rollback and hence assertTrue(rolledback) passes

        // 1st pass: returns one xid (count is 1)
        xarm.periodicWorkFirstPass();
        // 2nd pass: throws an exception (count is 2)
        xarm.periodicWorkSecondPass();
        assertTrue(xarm.getContactedJndiNames().contains("test"));
        assertFalse(rolledback);
        // 1st pass: throws an exception (count is 3)
        xarm.periodicWorkFirstPass();
        // 2nd pass: throws an exception (count is 4)
        xarm.periodicWorkSecondPass();
        assertFalse(xarm.getContactedJndiNames().contains("test"));
        assertFalse(rolledback);
        // 1st pass: returns an empty list of xids (count is 5)
        xarm.periodicWorkFirstPass();
        // 2nd pass: returns an empty list of xids (count is 6)
        xarm.periodicWorkSecondPass();
        assertTrue(xarm.getContactedJndiNames().contains("test"));
        assertTrue(rolledback);

        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(orphanSafetyInterval);
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(xaRecoveryNodes);
    }

    class DummyXAResourceOrphanFilter implements XAResourceOrphanFilter
    {
        public DummyXAResourceOrphanFilter ()
        {
            _vote = null;
        }
        
        public DummyXAResourceOrphanFilter (Vote v)
        {
            _vote = v;
        }
        
        @Override
        public Vote checkXid(Xid xid)
        {
            return _vote;
        }
        
        private Vote _vote;
    }

    private int recoverCalled;

}