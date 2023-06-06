/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactionScoped;

import org.junit.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;
import javax.naming.InitialContext;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 */
@RunWith(Arquillian.class)
public class TransactionScopedTest {
    @Inject
    UserTransaction userTransaction;

    @Inject
    TestCDITransactionScopeBean testTxAssociationChangeBean;

    @Inject
    TestCDITransactionScopeBean2 testTxAssociationChangeBean2;

    private static Set<String> timestamps = new TreeSet<String>();

    @Deployment
    public static JavaArchive createTestArchive() {

        return  ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClass(TestCDITransactionScopeBean.class)
                .addClass(TestCDITransactionScopeBean2.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @After
    public void tearDown() {

        try {
            userTransaction.rollback();
        } catch (Exception e) {
            // do nothing
        }
    }

    private synchronized void addTimestamp(String message) {
        timestamps.add(String.format("%d - %s (%s)%n", System.nanoTime(), Thread.currentThread().getId(), message));
    }

    //Based on test case from JTA 1.2 spec
    private void testTxAssociationChange() throws Exception {
        TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager(new InitialContext());

        addTimestamp("begin1");
        userTransaction.begin(); //tx1 begun
        assertTrue(testTxAssociationChangeBean.isPostConstructCalled());
        assertTrue(testTxAssociationChangeBean2.isPostConstructCalled());

        testTxAssociationChangeBean.setValue(1);
        testTxAssociationChangeBean2.setValue(10);

        addTimestamp("suspend");
        Transaction transaction = transactionManager.suspend();

        assertContextUnavailable();

        addTimestamp("begin2");
        userTransaction.begin(); //tx2 begun
        Assert.assertEquals(0, testTxAssociationChangeBean.getValue());
        Assert.assertEquals(0, testTxAssociationChangeBean2.getValue());

        testTxAssociationChangeBean.setValue(2);
        testTxAssociationChangeBean2.setValue(20);

        addTimestamp("commit1");
        userTransaction.commit();

        assertContextUnavailable();

        addTimestamp("resume");
        transactionManager.resume(transaction);
        Assert.assertEquals(1, testTxAssociationChangeBean.getValue());
        Assert.assertEquals(10, testTxAssociationChangeBean2.getValue());

        addTimestamp("commit2");
        userTransaction.commit();

        assertContextUnavailable();
        addTimestamp("finished");
    }

    @Test
    // run the test sequentially to ensure that state from different scopes do not interfere with each other
    public void testTxAssociationChangeSequentially() throws Exception {
        final int TEST_RUNS = 2;
        final int EXPECTED_MINIMUM_DESTROY_CNT = TEST_RUNS + TestCDITransactionScopeBean.getPreDestroyCnt();

        // run the test multiple times (sequentially)
        for (int i = 0; i < TEST_RUNS; i++) {
            try {
                testTxAssociationChange();
            } catch (Exception e) {
                throw new Exception("Sequential testTxAssociationChange failed with exception", e);
            }
        }

        // ensure that preDestroy was called on the bean at least TEST_RUNS times
        assertTrue(EXPECTED_MINIMUM_DESTROY_CNT <= TestCDITransactionScopeBean.getPreDestroyCnt());
    }

    @Test
    // run the test concurrently to ensure that state from different scopes do not interfere with each other
    public void testTxAssociationChangeConcurrently() throws Exception {
        final int TEST_RUNS = 5;
        final int EXPECTED_MINIMUM_DESTROY_CNT = TEST_RUNS + TestCDITransactionScopeBean.getPreDestroyCnt();

        ExecutorService executor = Executors.newFixedThreadPool(TEST_RUNS);
        Collection<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();

        timestamps.clear();
        // run the test multiple times (concurrently)
        for (int i = 0; i < TEST_RUNS; i++) {
            tasks.add(executor.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    testTxAssociationChange();
                    return true;
                }
            }));
        }

        // see if each individual test run completed successfully
        for (Future<Boolean> task : tasks) {
            try {
                if (!task.get())
                    throw new Exception("Concurrent testTxAssociationChange failed");
            } catch (Exception e) {
                throw new Exception("Concurrent testTxAssociationChange failed with exception", e);
            }
        }

        // ensure that preDestroy was called on the bean at least TEST_RUNS times
        assertTrue(EXPECTED_MINIMUM_DESTROY_CNT <= TestCDITransactionScopeBean.getPreDestroyCnt());

        for (String s : timestamps)
            System.out.print(s);
    }

    private void assertContextUnavailable() {

        try {
            testTxAssociationChangeBean.getValue();
            Assert.fail("Accessing bean should have thrown a ContextNotActiveException as it should not be available");
        } catch (ContextNotActiveException e) {
            //Expected
        }
    }
}