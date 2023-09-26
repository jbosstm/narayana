/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.integration.archive;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@WebService(name = TestService.PORT_NAME, targetNamespace = "http://www.narayana.io/compensationsApi/test")
public interface TestService {

    String NAMESPACE = "http://www.narayana.io/compensationsApi/test";

    String SERVICE_NAME = "TestServiceService";

    String PORT_NAME = "TestService";

    @WebMethod
    void compensatableMethod();

    @WebMethod
    int getConfirmationHandlerInvocationsCounter();

}