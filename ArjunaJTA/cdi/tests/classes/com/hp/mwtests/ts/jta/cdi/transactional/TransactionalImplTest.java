/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional;

import org.jboss.logging.Logger;
import org.junit.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionalException;
import jakarta.transaction.UserTransaction;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 */
@RunWith(Arquillian.class)
public class TransactionalImplTest {
    private static final Logger log = Logger.getLogger(TransactionalImplTest.class);

    @Inject
    UserTransaction userTransaction;

    @Inject
    TestTransactionalBean testTransactionalBean;

    @Inject
    BusinessLogic businessLogic;

    @Inject
    AbstractBusinessLogic abstractBusinessLogic;

    @Deployment
    public static WebArchive createTestArchive() {

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("com.hp.mwtests.ts.jta.cdi.transactional")
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");
    }

    @After
    public void tearDown() {

        AssertionParticipant.reset();
        try {
            userTransaction.rollback();
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    public void testDefaultNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithDefault();
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testDefaultExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Transaction tx = Utills.getCurrentTransaction();
        Utills.assertTransactionActive(true);

        testTransactionalBean.invokeWithDefault(Utills.getCurrentTransaction());
        Utills.assertSameTransaction(tx);

        Utills.assertTransactionActive(true);
        tm.commit();
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testDefaultThrowRuntimeException() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefault(RuntimeException.class);
            Assert.fail("Expected RuntimeException to be thrown, but it was not");
        } catch (RuntimeException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        AssertionParticipant.assertRolledBack();
    }

    @Test
    public void testDefaultThrowException() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefault(Exception.class);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (Exception e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testDefaultThrowError() throws Throwable {
        // java.lang.Error is un-checked exception and rollback is expected

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefault(Error.class);
            Assert.fail("Expected Throwable to be thrown and rollback the transaction, but it was not");
        } catch (Throwable t) {
            log.debug("Expected behaviour: Error was thrown from the method", t);
        }

        Utills.assertTransactionActive(false);
        AssertionParticipant.assertRolledBack();
    }

    @Test
    public void testThrowErrorWithDontRollbackOn() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithRequiresAndDontRollbackOnError(Error.class);
            Assert.fail("Expected Throwable to be thrown but not rolling back the transaction, but it was not thrown");
        } catch (Throwable t) {
            log.debug("Expected behaviour: Error was thrown from the method", t);
        }

        Utills.assertTransactionActive(false);
        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testDefaultThrowErrorWhenAcquiringTxn() throws Exception {
        // java.lang.Error is un-checked exception and rollback is expected

        TransactionManager tm = Utills.getTransactionManager();
        Utills.assertTransactionActive(false);
        tm.begin();

        try {
            Utills.assertTransactionActive(true);
            testTransactionalBean.invokeWithDefault(Error.class);
            Assert.fail("Expected Throwable to be thrown and rollback the transaction, but it was not");
        } catch (Throwable t) {
            log.debug("Expected behaviour: Error was thrown from the method", t);
        }
        tm.rollback();
        AssertionParticipant.assertRolledBack();
    }

    @Test
    public void testThrowErrorWithDontRollbackOnWhenAcquiringTxn() throws Throwable {

        TransactionManager tm = Utills.getTransactionManager();
        Utills.assertTransactionActive(false);
        tm.begin();

        try {
            testTransactionalBean.invokeWithRequiresAndDontRollbackOnError(Error.class);
            Assert.fail("Expected Throwable to be thrown but not rolling back the transaction, but it was not thrown");
        } catch (Throwable t) {
            log.debug("Expected behaviour: Error was thrown from the method", t);
        }

        Utills.assertTransactionActive(true);

        tm.commit();
        AssertionParticipant.assertCommitted();

        Utills.assertTransactionActive(false);
    }

    @Test
    public void testDefaultWithRollbackOn() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefaultAndRollbackOn(TestException.class);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TestException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        AssertionParticipant.assertRolledBack();
    }


    @Test
    public void testDefaultWithDontRollbackOn() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefaultAndDontRollbackOn(TestRuntimeException.class);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TestRuntimeException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }


    @Test
    public void testDefaultWithDoAndDontRollbackOn() throws Throwable {

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefaultAndDoAndDontRollbackOn(TestException.class);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TestException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }


    @Test
    public void testRequiresNewNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithRequiresNew(null);
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }


    @Test
    public void testRequiresNewExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Transaction tx = Utills.getCurrentTransaction();

        Utills.assertTransactionActive(true);
        testTransactionalBean.invokeWithRequiresNew(tx);
        Utills.assertTransactionActive(true);

        tm.commit();
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testMandatoryExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Transaction tx = Utills.getCurrentTransaction();

        Utills.assertTransactionActive(true);
        testTransactionalBean.invokeWithMandatory(tx);
        Utills.assertTransactionActive(true);

        tm.commit();
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testMandatoryNoExistingTX() throws Exception {


        Utills.assertTransactionActive(false);
        try {
            testTransactionalBean.invokeWithMandatory(null);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TransactionalException e) {
            //expected
        }
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testSupportsExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Transaction tx = Utills.getCurrentTransaction();

        Utills.assertTransactionActive(true);
        testTransactionalBean.invokeWithSupports(tx);
        Utills.assertTransactionActive(true);

        tm.commit();
        Utills.assertTransactionActive(false);

        AssertionParticipant.assertCommitted();
    }

    @Test
    public void testSupportsNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithSupports(null);
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNotSupportedExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Utills.assertTransactionActive(true);

        testTransactionalBean.invokeWithNotSupported();

        Utills.assertTransactionActive(true);
        tm.commit();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNotSupportedNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithNotSupported();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNeverExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Utills.assertTransactionActive(true);

        try {
            testTransactionalBean.invokeWithNever();
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TransactionalException e) {
            //expected
        }

        Utills.assertTransactionActive(true);
        tm.commit();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNeverNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithNever();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testClassLevelDefaultNeverExistingTX() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();

        Utills.assertTransactionActive(false);
        tm.begin();
        Utills.assertTransactionActive(true);

        try {
            testTransactionalBean.invokeWithCLassLevelDefault();
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TransactionalException e) {
            //expected
        }

        Utills.assertTransactionActive(true);
        tm.commit();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testClassLevelDefaultNeverNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithCLassLevelDefault();
        Utills.assertTransactionActive(false);
    }

    @Test(expected = IllegalStateException.class)
    public void testUseUserTransactionInRequired() throws Exception {

        testTransactionalBean.invokeWithRequiredUseUserTransaction();
    }

    @Test
    public void testUseUserTransactionInNever() throws Exception {

        testTransactionalBean.invokeWithNeverUseUserTransaction();
    }

    /**
     * <p>
     * Expecting {@link IllegalStateException} as the bean's method calls
     * {@link UserTransaction#getStatus()}. That's not permitted by JTA spec 1.2
     * <p>
     * <code> If an attempt is made to call any method of the UserTransaction interface from within the scope of a bean
     * or method annotated with @Transactional and a Transactional.TxType other than NOT_SUPPORTED or NEVER,
     * an IllegalStateException must be thrown</code>
     */
    @Test(expected = IllegalStateException.class)
    public void testUseUserTransactionInRequiresNew() throws Exception {

        testTransactionalBean.invokeWithRequiresNewUseUserTransaction();
    }

    @Test(expected = IllegalStateException.class)
    public void testUseUserTransactionInMandatory() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();
        tm.begin();

        testTransactionalBean.invokeWithMandatoryUseUserTransaction();
    }

    @Test(expected = IllegalStateException.class)
    public void testUseUserTransactionInSupports() throws Exception {

        TransactionManager tm = Utills.getTransactionManager();
        tm.begin();

        testTransactionalBean.invokeWithSupportsUseUserTransaction();
    }

    @Test
    public void testUseUserTransactionInNotSupported() throws Exception {

        testTransactionalBean.invokeWithNotSupportedUseUserTransaction();
    }

    @Test
    public void testEJB() throws Exception {

        testTransactionalBean.invokeEJB();
    }

    /**
     * Test that business logic annotated with a stereotype that includes @Transactional executes inside a transaction
     * @throws Exception
     */
    @Test(expected = TestException.class)
    public void testStereotype() throws Exception {
        businessLogic.doSomething();
    }

    @Test
    public void testStereotypeOnSubclass() throws Throwable {
        abstractBusinessLogic.doSomethingInAbstractClass(null);
    }

    @Test(expected = TestRuntimeException.class)
    public void testStereotypeOnSubclassWithRuntimeException() throws Throwable {
        abstractBusinessLogic.doSomethingInAbstractClass(new TestRuntimeException());
    }

    @Test(expected = TestException.class)
    public void testStereotypeOnSubclassWithException() throws Throwable {
        abstractBusinessLogic.doSomethingInAbstractClass(new TestException());
    }

}