/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.integration.archive;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestServiceClient implements TestService {

    private TestService client;

    public TestServiceClient(String deploymentName) {
        this.client = createWebServiceClient(deploymentName);
    }

    public void compensatableMethod() {
        client.compensatableMethod();
    }

    public int getConfirmationHandlerInvocationsCounter() {
        return client.getConfirmationHandlerInvocationsCounter();
    }

    private TestService createWebServiceClient(String deploymentName) {
        try {
            URL wsdlLocation = new URL("http://localhost:8080/" + deploymentName + "/" + TestService.SERVICE_NAME + "/"
                    + TestService.PORT_NAME + "?wsdl");
            QName serviceName = new QName(TestService.NAMESPACE, TestService.SERVICE_NAME);
            QName portName = new QName(TestService.NAMESPACE, TestService.PORT_NAME);

            Service service = Service.create(wsdlLocation, serviceName);
            return service.getPort(portName, TestService.class);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating Web Service client", e);
        }
    }

}