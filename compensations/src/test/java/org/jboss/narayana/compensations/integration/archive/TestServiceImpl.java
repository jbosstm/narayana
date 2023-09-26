/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.integration.archive;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.TxConfirm;

import jakarta.ejb.Stateless;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@Stateless
@WebService(serviceName = TestService.SERVICE_NAME, portName = TestService.PORT_NAME, name = TestService.PORT_NAME, targetNamespace = TestService.NAMESPACE)
public class TestServiceImpl implements TestService {

    @Compensatable
    @TxConfirm(TestConfirmationHandler.class)
    @WebMethod
    public void compensatableMethod() {
        System.out.println(TestServiceImpl.class.getSimpleName() + ".compensatableMethod");
    }

    @WebMethod
    public int getConfirmationHandlerInvocationsCounter() {
        return TestConfirmationHandler.getInvocationsCounter();
    }

}