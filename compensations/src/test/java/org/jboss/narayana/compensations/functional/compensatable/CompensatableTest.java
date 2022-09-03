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
package org.jboss.narayana.compensations.functional.compensatable;

import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.compensations.api.InvalidTransactionException;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.narayana.compensations.api.TransactionRequiredException;
import org.jboss.narayana.compensations.api.TransactionalException;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler2;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler2;
import org.jboss.narayana.compensations.internal.BAControllerFactory;
import org.jboss.narayana.compensations.internal.CompensationManagerImpl;
import org.jboss.narayana.compensations.internal.CompensationManagerState;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jakarta.inject.Inject;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public abstract class CompensatableTest {

    @Inject
    CompensatableBean testTransactionalBean;

    @Inject
    StereotypeBean stereotypeBean;


    @Before
    public void before() {

        DummyCompensationHandler1.reset();
        DummyConfirmationHandler1.reset();
        DummyCompensationHandler2.reset();
        DummyConfirmationHandler2.reset();
    }

    @After
    public void after() {

        try {
            BAControllerFactory.getInstance().cancelBusinessActivity();
        } catch (Throwable t) {

        }
    }

    @Test
    public void testDefaultNoExistingTX() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithDefault();
        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());
    }

    @Test
    public void testDefaultExistingTX() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        final Object txContext = BAControllerFactory.getInstance().getCurrentTransaction();

        testTransactionalBean.invokeWithDefault();
        Utills.assertSameTransaction(txContext);

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());
    }

    @Test
    public void testRequiresNewNoExistingTX() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithRequiresNew(null);
        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());
    }

    @Test
    public void testRequiresNewExistingTX() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        final Object txContext = BAControllerFactory.getInstance().getCurrentTransaction();

        testTransactionalBean.invokeWithRequiresNew(txContext);

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());
    }

    @Test
    public void testMandatoryNoExistingTX() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        try {
            testTransactionalBean.invokeWithMandatory(null);
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TransactionalException e) {
            //expected
            Assert.assertTrue(e.getCause() instanceof TransactionRequiredException);

        }
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testMandatoryExistingTX() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        final Object txContext = BAControllerFactory.getInstance().getCurrentTransaction();

        testTransactionalBean.invokeWithMandatory(txContext);

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());
    }

    @Test
    public void testSupportsNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithSupports();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testSupportsExistingTX() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        final Object txContext = BAControllerFactory.getInstance().getCurrentTransaction();

        testTransactionalBean.invokeWithSupports(txContext);

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNotSupportedNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithNotSupported();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNotSupportedExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        testTransactionalBean.invokeWithNotSupported();

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNeverNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithNever();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testNeverExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        try {
            testTransactionalBean.invokeWithNever();
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TransactionalException e) {
            //expected
            Assert.assertTrue(e.getCause() instanceof InvalidTransactionException);
        }

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testClassLevelDefaultNeverNoExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithCLassLevelDefault();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testClassLevelDefaultNeverExistingTX() throws Exception {

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        try {
            testTransactionalBean.invokeWithCLassLevelDefault();
            Assert.fail("Expected Exception to be thrown, but it was not");
        } catch (TransactionalException e) {
            //expected
            Assert.assertTrue(e.getCause() instanceof InvalidTransactionException);
        }

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);
    }

    @Test
    public void testDefaultThrowRuntimeExceptionNoExistingTX() throws Throwable {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefault(null, new TestRuntimeException());
            Assert.fail("Expected TestRuntimeException to be thrown, but it was not");
        } catch (TestRuntimeException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        Assert.assertTrue(DummyCompensationHandler1.getCalled());
        Assert.assertFalse(DummyConfirmationHandler1.getCalled());

        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertFalse(DummyConfirmationHandler2.getCalled());
    }

    @Test
    public void testDefaultThrowRuntimeExceptionExistingTX() throws Throwable {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        try {
            testTransactionalBean.invokeWithDefault(null, new TestRuntimeException());
            Assert.fail("Expected TestRuntimeException to be thrown, but it was not");
        } catch (TestRuntimeException e) {
            //expected
        }

        Utills.assertTransactionActive(true);
        try {
            completeBusinessActivity();
        } catch (TransactionCompensatedException e) {
            //expected
        }
        Utills.assertTransactionActive(false);

        Assert.assertTrue(DummyCompensationHandler1.getCalled());
        Assert.assertFalse(DummyConfirmationHandler1.getCalled());

        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertFalse(DummyConfirmationHandler2.getCalled());
    }

    @Test
    public void testDefaultThrowExceptionNoExistingTX() throws Throwable {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefault(null, new TestException());
            Assert.fail("Expected TestException to be thrown, but it was not");
        } catch (TestException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());

        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertTrue(DummyConfirmationHandler2.getCalled());
    }

    @Test
    public void testDefaultThrowExceptionExistingTX() throws Throwable {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        try {
            testTransactionalBean.invokeWithDefault(null, new TestException());
            Assert.fail("Expected TestException to be thrown, but it was not");
        } catch (TestException e) {
            //expected
        }

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());

        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertTrue(DummyConfirmationHandler2.getCalled());
    }

    @Test
    public void testDefaultWithCancelOn() throws Throwable {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefaultAndCancelOn(null, new TestException());
            Assert.fail("Expected TestException to be thrown, but it was not");
        } catch (TestException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        Assert.assertTrue(DummyCompensationHandler1.getCalled());
        Assert.assertFalse(DummyConfirmationHandler1.getCalled());

        // Different than RuntimeException because cancelOn exceptions are handled by CompensationInterceptorBase
        Assert.assertTrue(DummyCompensationHandler2.getCalled());
        Assert.assertFalse(DummyConfirmationHandler2.getCalled());
    }

    @Test
    public void testDefaultWithDontCancelOn() throws Throwable {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefaultAndDontCancelOn(null, new TestRuntimeException());
            Assert.fail("Expected TestRuntimeException to be thrown, but it was not");
        } catch (TestRuntimeException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());

        // Second participant was compensated because RuntimeException was handled by the participant handler first.
        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertFalse(DummyConfirmationHandler2.getCalled());
    }

    @Test
    public void testDefaultWithDoAndDontRollbackOn() throws Throwable {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        Utills.assertTransactionActive(false);

        try {
            testTransactionalBean.invokeWithDefaultAndDoAndDontCancelOn(null, new TestException());
            Assert.fail("Expected TestException to be thrown, but it was not");
        } catch (TestException e) {
            //expected
        }

        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());

        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertTrue(DummyConfirmationHandler2.getCalled());
    }

    @Test(expected = TestException.class)
    public void testStereotype() throws TestException {

        stereotypeBean.doSomething();
    }

    private void beginBusinessActivity() throws Exception {

        BAControllerFactory.getInstance().beginBusinessActivity();
        CompensationManagerImpl.resume(new CompensationManagerState());
    }

    private void closeBusinessActivity() throws Exception {

        BAControllerFactory.getInstance().closeBusinessActivity();
        CompensationManagerImpl.suspend();
    }

    private void completeBusinessActivity() throws Exception {

        if (!CompensationManagerImpl.isCompensateOnly()) {
            try {
                BAControllerFactory.getInstance().closeBusinessActivity();
            } catch (Exception e) {
                throw new TransactionCompensatedException("Failed to close transaction", e);
            }
        } else {
            BAControllerFactory.getInstance().cancelBusinessActivity();
            throw new TransactionCompensatedException("Transaction was marked as 'compensate only'");
        }

        CompensationManagerImpl.suspend();
    }

}
