/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.functional.compensatable;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationTransactionType;
import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxConfirm;
import org.jboss.narayana.compensations.api.TxLogged;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyTransactionLoggedHandler1;

import jakarta.inject.Inject;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Compensatable(CompensationTransactionType.NEVER)
public class CompensatableBean {

    public static boolean isDistributed = false;

    @Inject
    SingleService singleService;

    public void invokeWithCLassLevelDefault() throws Exception {

        Utills.assertTransactionActive(false);
    }

    @Compensatable
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void invokeWithDefault() throws Exception {

        Utills.assertTransactionActive(true);
    }

    @Compensatable
    public void invokeWithDefault(Throwable firstServiceException, Throwable secondServiceException) throws Throwable {

        Utills.assertTransactionActive(true);

        singleService.testSingle1(firstServiceException);
        singleService.testSingle2(secondServiceException);
    }

    @Compensatable(cancelOn = TestException.class)
    public void invokeWithDefaultAndCancelOn(Throwable firstServiceException, Throwable secondServiceException)
            throws Throwable {

        Utills.assertTransactionActive(true);

        singleService.testSingle1(firstServiceException);
        singleService.testSingle2(secondServiceException);
    }

    @Compensatable(dontCancelOn = TestRuntimeException.class)
    public void invokeWithDefaultAndDontCancelOn(Throwable firstServiceException, Throwable secondServiceException)
            throws Throwable {

        Utills.assertTransactionActive(true);

        singleService.testSingle1(firstServiceException);
        singleService.testSingle2(secondServiceException);
    }

    @Compensatable(dontCancelOn = TestException.class, cancelOn = TestException.class)
    public void invokeWithDefaultAndDoAndDontCancelOn(Throwable firstServiceException, Throwable secondServiceException)
            throws Throwable {

        Utills.assertTransactionActive(true);

        singleService.testSingle1(firstServiceException);
        singleService.testSingle2(secondServiceException);
    }

    @Compensatable(CompensationTransactionType.REQUIRES_NEW)
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void invokeWithRequiresNew(final Object oldTransaction) throws Exception {

        Utills.assertTransactionActive(true);
        Utills.assertDifferentTransaction(oldTransaction);
    }

    @Compensatable(CompensationTransactionType.MANDATORY)
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void invokeWithMandatory(final Object expectedTransaction) throws Exception {

        Utills.assertTransactionActive(true);
        Utills.assertSameTransaction(expectedTransaction);
    }

    @Compensatable(CompensationTransactionType.SUPPORTS)
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void invokeWithSupports() throws Exception {

        Utills.assertTransactionActive(false);
        Utills.assertSameTransaction(null);
    }

    @Compensatable(CompensationTransactionType.SUPPORTS)
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void invokeWithSupports(final Object expectedTransaction) throws Exception {

        Utills.assertTransactionActive(true);
        Utills.assertSameTransaction(expectedTransaction);
    }

    @Compensatable(CompensationTransactionType.NOT_SUPPORTED)
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void invokeWithNotSupported() throws Exception {

        Utills.assertTransactionActive(false);
    }

    @Compensatable(CompensationTransactionType.NEVER)
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void invokeWithNever() throws Exception {

        Utills.assertTransactionActive(false);
    }

}