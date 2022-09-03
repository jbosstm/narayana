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
