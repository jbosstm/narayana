/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.compensationManager;

import org.jboss.narayana.compensations.api.CancelOnFailure;
import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationManager;
import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxConfirm;
import org.jboss.narayana.compensations.api.TxLogged;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyTransactionLoggedHandler1;
import org.jboss.narayana.compensations.functional.common.MyRuntimeException;

import jakarta.inject.Inject;

/**
 * @author paul.robinson@redhat.com 24/04/2013
 */
public class CompensationManagerService {

    @Inject
    CompensationManager compensationManager;

    @Inject
    CompensationManagerService2 compensationManagerService2;

    @Compensatable
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void doWork() throws MyRuntimeException {

        compensationManager.setCompensateOnly();
        throw new MyRuntimeException();
    }

    @Compensatable
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void doWorkRecursively() throws MyRuntimeException {

        try {
            compensationManagerService2.doWork();
        } catch (MyRuntimeException e) {
            //try to handle, but the work marked as compensate only.
        }
    }

    @Compensatable
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    @CancelOnFailure
    public void doWorkCompensateIfFail() throws MyRuntimeException {

        throw new MyRuntimeException();
    }

    @Compensatable
    @TxCompensate(DummyCompensationHandler1.class)
    @TxConfirm(DummyConfirmationHandler1.class)
    @TxLogged(DummyTransactionLoggedHandler1.class)
    public void doWorkRecursivelyCompensateIfFail(boolean fail) throws MyRuntimeException {

        try {
            compensationManagerService2.doWorkCompensateIfFails(fail);
        } catch (MyRuntimeException e) {
            //try to handle, but the work marked as compensate only.
        }
    }

}