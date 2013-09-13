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

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.compensations.api.InvalidTransactionException;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.narayana.compensations.api.TransactionRequiredException;
import org.jboss.narayana.compensations.api.TransactionalException;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler2;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler2;
import org.jboss.narayana.compensations.functional.common.DummyTransactionLoggedHandler1;
import org.jboss.narayana.compensations.functional.common.DummyTransactionLoggedHandler2;
import org.jboss.narayana.compensations.impl.CompensationManagerImpl;
import org.jboss.narayana.compensations.impl.CompensationManagerState;
import org.jboss.narayana.txframework.impl.TXDataMapImpl;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.HashMap;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class CompensatableTest {

    @Inject
    CompensatableBean testTransactionalBean;


    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("org.jboss.narayana.compensations.functional.common")
                .addPackage("org.jboss.narayana.compensations.functional.compensatable")
                .addClass(ParticipantCompletionCoordinatorRules.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }

    @BeforeClass()
    public static void submitBytemanScript() throws Exception {
        BMScript.submit(ParticipantCompletionCoordinatorRules.RESOURCE_PATH);
    }

    @AfterClass()
    public static void removeBytemanScript() {
        BMScript.remove(ParticipantCompletionCoordinatorRules.RESOURCE_PATH);
    }

    @Before
    public void before() {
        DummyCompensationHandler1.reset();
        DummyConfirmationHandler1.reset();
        DummyTransactionLoggedHandler1.reset();
        DummyCompensationHandler2.reset();
        DummyConfirmationHandler2.reset();
        DummyTransactionLoggedHandler2.reset();
    }

    @After
    public void after() {
        try {
            UserBusinessActivityFactory.userBusinessActivity().cancel();
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
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());
    }

    @Test
    public void testDefaultExistingTX() throws Exception {
        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        final TxContext txContext = BusinessActivityManagerFactory.businessActivityManager().currentTransaction();

        testTransactionalBean.invokeWithDefault();
        Utills.assertSameTransaction(txContext);

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());
    }

    @Test
    public void testRequiresNewNoExistingTX() throws Exception {
        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        testTransactionalBean.invokeWithRequiresNew(null);
        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());
    }

    @Test
    public void testRequiresNewExistingTX() throws Exception {
        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        Utills.assertTransactionActive(false);
        beginBusinessActivity();
        Utills.assertTransactionActive(true);

        final TxContext txContext = BusinessActivityManagerFactory.businessActivityManager().currentTransaction();

        testTransactionalBean.invokeWithRequiresNew(txContext);

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());
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

        final TxContext txContext = BusinessActivityManagerFactory.businessActivityManager().currentTransaction();

        testTransactionalBean.invokeWithMandatory(txContext);

        Utills.assertTransactionActive(true);
        closeBusinessActivity();
        Utills.assertTransactionActive(false);

        Assert.assertFalse(DummyCompensationHandler1.getCalled());
        Assert.assertTrue(DummyConfirmationHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());
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

        final TxContext txContext = BusinessActivityManagerFactory.businessActivityManager().currentTransaction();

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
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());

        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertFalse(DummyConfirmationHandler2.getCalled());
        Assert.assertFalse(DummyTransactionLoggedHandler2.getCalled());
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
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());

        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertFalse(DummyConfirmationHandler2.getCalled());
        Assert.assertFalse(DummyTransactionLoggedHandler2.getCalled());
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
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());

        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertTrue(DummyConfirmationHandler2.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler2.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler2.getSuccess());
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
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());

        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertTrue(DummyConfirmationHandler2.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler2.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler2.getSuccess());
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
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());

        // Different than RuntimeException because cancelOn exceptions are handled by CompensationInterceptorBase
        Assert.assertTrue(DummyCompensationHandler2.getCalled());
        Assert.assertFalse(DummyConfirmationHandler2.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler2.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler2.getSuccess());
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
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());

        // Second participant was compensated because RuntimeException was handled by the participant handler first.
        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertFalse(DummyConfirmationHandler2.getCalled());
        Assert.assertFalse(DummyTransactionLoggedHandler2.getCalled());
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
        Assert.assertTrue(DummyTransactionLoggedHandler1.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler1.getSuccess());

        Assert.assertFalse(DummyCompensationHandler2.getCalled());
        Assert.assertTrue(DummyConfirmationHandler2.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler2.getCalled());
        Assert.assertTrue(DummyTransactionLoggedHandler2.getSuccess());
    }

    private void beginBusinessActivity() throws WrongStateException, SystemException {
        UserBusinessActivityFactory.userBusinessActivity().begin();
        CompensationManagerImpl.resume(new CompensationManagerState());
        TXDataMapImpl.resume(new HashMap());
    }

    private void closeBusinessActivity() throws WrongStateException, UnknownTransactionException, TransactionRolledBackException, SystemException {
        UserBusinessActivityFactory.userBusinessActivity().close();
        CompensationManagerImpl.suspend();
        TXDataMapImpl.suspend();
    }

    private void completeBusinessActivity() throws WrongStateException, UnknownTransactionException, SystemException {
        if (!CompensationManagerImpl.isCompensateOnly()) {
            try {
                UserBusinessActivityFactory.userBusinessActivity().close();
            } catch (TransactionRolledBackException e) {
                throw new TransactionCompensatedException("Failed to close transaction", e);
            }
        } else {
            UserBusinessActivityFactory.userBusinessActivity().cancel();
            throw new TransactionCompensatedException("Transaction was marked as 'compensate only'");
        }

        CompensationManagerImpl.suspend();
        TXDataMapImpl.suspend();
    }

}