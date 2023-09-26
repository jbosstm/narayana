/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.compensationScoped;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxConfirm;
import org.jboss.narayana.compensations.api.TxLogged;
import org.jboss.narayana.compensations.functional.common.MyRuntimeException;

import jakarta.inject.Inject;

/**
 * @author paul.robinson@redhat.com 24/04/2013
 */
public class Service {

    @Inject
    DataPojo myData;

    @Compensatable
    @TxCompensate(MyCompensationHandler.class)
    @TxLogged(MyTransactionLoggedHandler.class)
    @TxConfirm(MyConfirmationHandler.class)
    public void doWork(String setValue) throws MyRuntimeException {

        myData.setData(setValue);
    }

}