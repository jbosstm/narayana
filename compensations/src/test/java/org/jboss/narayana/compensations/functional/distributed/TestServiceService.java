/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.functional.distributed;

import org.jboss.narayana.compensations.api.CancelOnFailure;
import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxConfirm;
import org.jboss.narayana.compensations.api.TxLogged;
import org.jboss.narayana.compensations.functional.common.DataCompensationHandler;
import org.jboss.narayana.compensations.functional.common.DataConfirmationHandler;
import org.jboss.narayana.compensations.functional.common.DataTxLoggedHandler;
import org.jboss.narayana.compensations.functional.common.DummyData;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;


/**
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@Stateless
@WebService(serviceName = "TestServiceService", portName = "TestServiceService",
        name = "TestService", targetNamespace = "http://www.jboss.com/functional/compensations/distributed/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class TestServiceService implements TestService {

    @Inject
    DummyData state;

    @Compensatable
    @TxConfirm(DataConfirmationHandler.class)
    @TxLogged(DataTxLoggedHandler.class)
    @TxCompensate(DataCompensationHandler.class)
    public void saveData(Boolean throwRuntimeException) {

        state.setValue("value");
        DataConfirmationHandler.expectedDataValue = "value";
        DataCompensationHandler.expectedDataValue = "value";
        DataTxLoggedHandler.expectedDataValue = "value";

        if (throwRuntimeException) {
            throw new RuntimeException("Test instructed the service to throw a RuntimeException");
        }

    }

    @Compensatable
    @TxConfirm(DataConfirmationHandler.class)
    @TxLogged(DataTxLoggedHandler.class)
    @TxCompensate(DataCompensationHandler.class)
    @CancelOnFailure
    public void saveDataCancelOnFailure(Boolean throwRuntimeException) {

        state.setValue("value");
        DataConfirmationHandler.expectedDataValue = "value";
        DataCompensationHandler.expectedDataValue = "value";
        DataTxLoggedHandler.expectedDataValue = "value";

        if (throwRuntimeException) {
            throw new RuntimeException("Test instructed the service to throw a RuntimeException");
        }

    }

    @WebMethod
    public void resetHandlerFlags() {

        DataConfirmationHandler.reset();
        DataTxLoggedHandler.reset();
        DataCompensationHandler.reset();
    }

    @Override
    public boolean wasTransactionConfirmedHandlerInvoked() {

        return DataConfirmationHandler.getDataAvailable();
    }

    @Override
    public boolean wasTransactionLoggedHandlerInvoked() {

        return DataTxLoggedHandler.getDataAvailable();
    }

    @Override
    public boolean wasCompensationHandlerInvoked() {

        return DataCompensationHandler.getDataAvailable();
    }
}