/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.distributed;

import jakarta.ejb.Remote;
import jakarta.jws.WebMethod;

@Remote
public interface TestService {

    @WebMethod
    public void saveData(Boolean throwException);

    @WebMethod
    public void saveDataCancelOnFailure(Boolean throwException);

    @WebMethod
    public void resetHandlerFlags();

    @WebMethod
    public boolean wasCompensationHandlerInvoked();

    @WebMethod
    public boolean wasTransactionLoggedHandlerInvoked();

    @WebMethod
    public boolean wasTransactionConfirmedHandlerInvoked();

}