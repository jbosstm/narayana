/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package io.narayana.spi;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import io.narayana.spi.util.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.*;

import static org.junit.Assert.*;

/*
 * To test recovery do a run than generates a recovery record:
 *  ./build.sh -f ArjunaJTA/spi/pom.xml test -Dtest=io.narayana.spi.SPIUnitTest#testXADSWithFaults -Dspitest.fault=halt
 * followed by a run that will check it recovered ok (but see the note below about recovery only working with postgresql and
 * the need to manually recover h2):
 *  ./build.sh -f ArjunaJTA/spi/pom.xml test -Dtest=io.narayana.spi.SPIUnitTest#testWaitForRecovery -Dspitest.fault=recover
 *
 * But you need to have postgresql as one of your databases by editing the file ArjunaJTA/spi/target/test-classes/db.properties
 * and including the line DB_PREFIXES=DB_H2,DB_PGSQL at the top of the file. This file must also be modified if you would
 * like to test with a different set of databases.
 *
 *  Note that h2 does not support recovery properly so you will need to manually resolve it before running the recovery step:
 *  first start the H2 console. Eg if you have it installed in /usr/local/h2/bin:
 *      java -jar /usr/local/h2/bin/h2*.jar &
 *  this will open a console in your default browser. Log in as user sa/sa
 *      select * from information_schema.in_doubt
 *      followed by  COMMIT TRANSACTION xid
 *  now disconnect (since h2 only allows a single user connection)
 */
public class SPIUnitTest
{
    private String userTransactionJNDIContext = jtaPropertyManager.getJTAEnvironmentBean().getUserTransactionJNDIContext();
    private String transactionManagerJNDIContext =  jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerJNDIContext();

    @BeforeClass
    public static void setUp() throws Exception {
        JndiProvider.start();
        JndiProvider.initBindings();
        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryBackoffPeriod(1);
        TransactionServiceFactory.start(true);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TransactionServiceFactory.stop();
        JndiProvider.stop();
    }

    @Test
    public void testTMRestart() throws Exception {
        UserTransaction txn = (UserTransaction) new InitialContext().lookup(userTransactionJNDIContext);
        TransactionServiceFactory.stop();

        // UserTransaction should still work even thought the TF has been stopped
        txn.begin();
        txn.commit();

        // Validate that JNDI lookups fail when the TF is stopped
        try {
            new InitialContext().lookup(userTransactionJNDIContext);
            fail("User transaction is still bound after stopping the TransactionServiceFactory");
        } catch (NamingException e) {
        }

        // Validate that the TF can be restarted (without a recovery manager)
        TransactionServiceFactory.start(false);
        testCommitXADS();

        TransactionServiceFactory.stop();
        // Validate that the TF can be restarted (with a recovery manager)
        TransactionServiceFactory.start(true);
        testCommitXADS();
    }

    @Test(expected = RollbackException.class)
    public void testTimeout() throws Exception {
        final int timeout = 2;
        UserTransaction txn = (UserTransaction) new InitialContext().lookup(userTransactionJNDIContext);

        txn.setTransactionTimeout(timeout);
        txn.begin();

        Thread.sleep(timeout * 1000 + 1000);
        txn.commit();
        fail("committing a timed out transaction should have thrown a RollbackException");
    }

    @Test
    public void testTransaction() throws Exception {
        UserTransaction txn = (UserTransaction) new InitialContext().lookup(userTransactionJNDIContext);

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
    public void testCommitXADS() throws Exception {
        UserTransaction txn = (UserTransaction) new InitialContext().lookup(userTransactionJNDIContext);
        DbTester dbTester = new DbTester();

        txn.begin();
        dbTester.doInserts();
        txn.commit();
        dbTester.assertRowCounts(true);

        dbTester.dropTables();
    }

    @Test
    public void testAbortXADS() throws Exception {
        UserTransaction txn = (UserTransaction) new InitialContext().lookup(userTransactionJNDIContext);
        DbTester dbTester = new DbTester();

        txn.begin();
        dbTester.doInserts();
        txn.rollback();
        dbTester.assertRowCounts(false);

        dbTester.dropTables();
    }

    @Test
    public void testXADSWithFaults() throws Exception {
        UserTransaction txn = (UserTransaction) new InitialContext().lookup(userTransactionJNDIContext);
        DbTester dbTester = new DbTester();
        String fault = dbTester.getFault();

        System.out.printf("testXADSWithFaults with fault type %s%n", fault);

        txn.begin();
        dbTester.doInserts();

        try {
            txn.commit();
            assertFalse("VM should have halted", "halt".equalsIgnoreCase(fault));
            dbTester.assertRowCounts(true);
        } catch (Exception e) {
            if ("XA_RBROLLBACK".equalsIgnoreCase(fault)) {
                if (e instanceof HeuristicMixedException) {
                    System.out.printf("expected exception: " + e.getMessage());
                } else if (e instanceof RollbackException) {
                    System.out.printf("expected exception: " + e.getMessage());
                } else {
                    dbTester.dropTables();
                    throw e;
                }
            } else {
                System.out.printf("possibly unexpected exception (what fault type did you inject: " + e.getMessage());
            }
        }

        dbTester.dropTables();
    }

    @Test
    public void testWaitForRecovery() throws Exception {
        DbTester dbTester = new DbTester(false);

        if (!"recover".equalsIgnoreCase(dbTester.getFault()))
            return;

        dbTester.clearCounts();

        System.out.println("testWaitForRecovery: waiting for recovery");
        RecoveryManager.manager().scan();

        dbTester.assertRowCounts(true);
    }

    @Test
    public void testSynchronization() throws Exception {
        TestSynchronization synch =  new TestSynchronization() ;

        UserTransaction txn = (UserTransaction) new InitialContext().lookup(userTransactionJNDIContext);
        TransactionManager transactionManager = (TransactionManager) new InitialContext().lookup(transactionManagerJNDIContext);

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
