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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


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
        DataConfirmationHandler.expectedDataValue="value";
        DataCompensationHandler.expectedDataValue="value";
        DataTxLoggedHandler.expectedDataValue="value";

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
        DataConfirmationHandler.expectedDataValue="value";
        DataCompensationHandler.expectedDataValue="value";
        DataTxLoggedHandler.expectedDataValue="value";

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
