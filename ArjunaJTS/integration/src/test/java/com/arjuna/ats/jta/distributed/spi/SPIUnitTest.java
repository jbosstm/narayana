/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.distributed.spi;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.distributed.JndiProvider;
import com.arjuna.ats.jta.utils.JNDIManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.orbportability.internal.utils.PostInitLoader;
import org.jboss.tm.listener.EventType;
import org.jboss.tm.listener.TransactionListenerRegistry;
import org.jboss.tm.listener.TransactionListenerRegistryLocator;
import org.jboss.tm.usertx.UserTransactionRegistry;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.InitialContext;
import jakarta.transaction.*;
import java.util.*;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.omg.CORBA.ORBPackage.InvalidName;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class SPIUnitTest {
    private static InitialContext initialContext;
    private static ORB myORB = null;
    private static RootOA myOA = null;

    private static void initORB() throws InvalidName {
        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[]{}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        new PostInitLoader(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb"), myORB);
    }

    public static void initJTS() throws Exception {
        initORB();

        JTAEnvironmentBean jtaEnvironmentBean = jtaPropertyManager.getJTAEnvironmentBean();

        jtaEnvironmentBean.setTransactionManagerClassName(com.arjuna.ats.jbossatx.jts.TransactionManagerDelegate.class.getName());

        JNDIManager.bindJTATransactionManagerImplementation(initialContext);

        jtaEnvironmentBean.setTransactionSynchronizationRegistryClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionSynchronizationRegistryImple.class.getName());

        JNDIManager.bindJTATransactionSynchronizationRegistryImplementation(initialContext);

        final com.arjuna.ats.jbossatx.jts.TransactionManagerService service = new com.arjuna.ats.jbossatx.jts.TransactionManagerService();
        final ServerVMClientUserTransaction userTransaction = new ServerVMClientUserTransaction(service.getTransactionManager());

        jtaEnvironmentBean.setUserTransaction(userTransaction);

        JNDIManager.bindJTAUserTransactionImplementation(initialContext);

        UserTransactionRegistry userTransactionRegistry = new UserTransactionRegistry();
        userTransactionRegistry.addProvider(userTransaction);
    }

    private UserTransaction getUserTransaction() {
        return jtaPropertyManager.getJTAEnvironmentBean().getUserTransaction();
    }

    private TransactionManager getTransactionManager() {
        return jtaPropertyManager.getJTAEnvironmentBean().getTransactionManager();
    }

    @BeforeClass
    public static void setUp() throws Exception {
        Hashtable props = JndiProvider.start();

        initialContext =  new InitialContext(props);

        initJTS();

        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryBackoffPeriod(1);
    }

    @After
    public void cleanThread() {
        TransactionManager tm = getTransactionManager();

        try {
            Transaction txn = tm.getTransaction();

            if (txn != null)
                txn.rollback();
        } catch (SystemException e) {
        } catch (Error error) {
        }
    }

    @Test
    public void testListener() {
        TransactionManager tm = getTransactionManager();
        Transaction prevTxn = null;

        // transaction-to-thread listeners were deactivated by default in JBTM-3166
        boolean wasListenersEnabled = jtaPropertyManager.getJTAEnvironmentBean().isTransactionToThreadListenersEnabled();
        try {
        	jtaPropertyManager.getJTAEnvironmentBean().setTransactionToThreadListenersEnabled(true);
            TransactionListenerRegistry tlr = TransactionListenerRegistryLocator.getTransactionListenerRegistry();
            TxListener listener = new TxListener(tlr);

            try {
                prevTxn = tm.suspend();
            } catch (SystemException e) {
            } catch (Error e) {
                System.out.printf("Error %s%n", e.getMessage());
            }
            tm.begin();

            tlr.addListener(tm.getTransaction(), listener, EnumSet.allOf(EventType.class));

            tm.commit();

            if (!listener.hasEvents())
                throw new RuntimeException("listener did not get any events");
        } catch (Throwable e) {
            throw new RuntimeException("TransactionListenerRegistry test failure: ", e);
        } finally {
        	jtaPropertyManager.getJTAEnvironmentBean().setTransactionToThreadListenersEnabled(wasListenersEnabled);
            if (prevTxn != null) {
                try {
                    tm.resume(prevTxn);
                } catch (InvalidTransactionException | SystemException e) {
                    throw new RuntimeException("testListener resume failed: ", e);
                }
            }
        }
    }

    @Test(expected = RollbackException.class)
    public void testTimeout() throws Exception {
        final int timeout = 2;
        UserTransaction txn = getUserTransaction();

        txn.setTransactionTimeout(timeout);
        txn.begin();

        Thread.sleep(timeout * 1000 + 1000);
        txn.commit();
        fail("committing a timed out transaction should have thrown a RollbackException");
    }

    @Test
    public void testTransaction() throws Exception {
        UserTransaction txn = getUserTransaction();

        txn.begin();

        assertNotNull(txn);
        assertEquals(Status.STATUS_ACTIVE, txn.getStatus());

        txn.commit();

        // the transaction should have been disassociated
        assertEquals(Status.STATUS_NO_TRANSACTION, txn.getStatus());

        txn.begin();
        txn.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, txn.getStatus());
    }

    @Test
    public void testSynchronization() throws Exception {
        TestSynchronization synch =  new TestSynchronization() ;

        UserTransaction txn = getUserTransaction();
        TransactionManager transactionManager = getTransactionManager();

        txn.begin();

        transactionManager.getTransaction().registerSynchronization(synch);

        txn.commit();

        assertTrue(synch.beforeCalled);
        assertTrue(synch.afterCalled);
    }

    class TestSynchronization implements Synchronization {
        boolean beforeCalled = false;
        boolean afterCalled = false;

        @Override
        public void beforeCompletion() {
            beforeCalled = true;
        }

        @Override
        public void afterCompletion(int i) {
            afterCalled = true;
        }
    }
}