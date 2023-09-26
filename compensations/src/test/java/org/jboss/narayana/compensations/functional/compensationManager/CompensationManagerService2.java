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
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler2;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler2;
import org.jboss.narayana.compensations.functional.common.DummyTransactionLoggedHandler2;
import org.jboss.narayana.compensations.functional.common.MyRuntimeException;

import jakarta.inject.Inject;

/**
 * @author paul.robinson@redhat.com 24/04/2013
 */
public class CompensationManagerService2 {

    @Inject
    CompensationManager compensationManager;

    @Compensatable
    @TxCompensate(DummyCompensationHandler2.class)
    @TxConfirm(DummyConfirmationHandler2.class)
    @TxLogged(DummyTransactionLoggedHandler2.class)
    public void doWork() throws MyRuntimeException {

        compensationManager.setCompensateOnly();
        throw new MyRuntimeException();
    }

    @Compensatable
    @TxCompensate(DummyCompensationHandler2.class)
    @TxConfirm(DummyConfirmationHandler2.class)
    @TxLogged(DummyTransactionLoggedHandler2.class)
    @CancelOnFailure
    public void doWorkCompensateIfFails(boolean fail) throws MyRuntimeException {

        if (fail) {
            throw new MyRuntimeException();
        }
    }

}