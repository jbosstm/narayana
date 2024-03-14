/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package io.narayana.spi.usertx;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.utils.JNDIManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import org.jboss.tm.TransactionManagerLocator;
import org.jboss.tm.usertx.UserTransactionRegistry;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.InitialContext;
import jakarta.transaction.*;
import java.util.Hashtable;

import static org.junit.Assert.*;

public class SPIUnitTest {
    private static InitialContext initialContext;

    public static void initEnv() throws Exception {
        // TODO add a JTS verson of the test
        final JTAEnvironmentBean jtaEnvironmentBean = jtaPropertyManager.getJTAEnvironmentBean();

        jtaEnvironmentBean.setTransactionManagerClassName(com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.class.getName());

        final com.arjuna.ats.jbossatx.jta.TransactionManagerService service = new com.arjuna.ats.jbossatx.jta.TransactionManagerService();
        final ServerVMClientUserTransaction userTransaction = new ServerVMClientUserTransaction(service.getTransactionManager());
        final TransactionSynchronizationRegistry tsr = new com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple();
        final UserTransactionRegistry userTransactionRegistry = new UserTransactionRegistry();

        userTransactionRegistry.addProvider(userTransaction);
        service.setTransactionSynchronizationRegistry(tsr);

        jtaEnvironmentBean.setUserTransaction(userTransaction);
        jtaEnvironmentBean.setTransactionManager(service.getTransactionManager());
        jtaEnvironmentBean.setTransactionSynchronizationRegistry(tsr);

        JNDIManager.bindJTATransactionManagerImplementation(initialContext);
        JNDIManager.bindJTATransactionSynchronizationRegistryImplementation(initialContext);
        JNDIManager.bindJTAUserTransactionImplementation(initialContext);
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

        initEnv();

        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryBackoffPeriod(1);
    }

    @After
    public void cleanThread() {
        TransactionManager tm = TransactionManagerLocator.locateTransactionManager();

        try {
            Transaction txn = tm.getTransaction();

            if (txn != null)
                txn.rollback();
        } catch (SystemException ignore) {
        } catch (Error ignore) {
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

    static private class TestSynchronization implements Synchronization {
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
