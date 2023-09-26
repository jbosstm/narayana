/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.distributed;

import com.arjuna.mw.wst11.client.WSTXFeature;
import org.jboss.narayana.common.URLUtils;
import org.jboss.narayana.compensations.api.Compensatable;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.URL;

public class TestServiceClient implements TestService {

    private TestService testService;

    public TestServiceClient() throws Exception {

        URLUtils urlUtils = new URLUtils();
        URL wsdlLocation = new URL(urlUtils.getBaseUrl() + ":" + urlUtils.getBasePort()
                + "/test/TestServiceService/TestService?wsdl");
        QName serviceName = new QName("http://www.jboss.com/functional/compensations/distributed/", "TestServiceService");
        QName portName = new QName("http://www.jboss.com/functional/compensations/distributed/", "TestServiceService");

        Service service = Service.create(wsdlLocation, serviceName);
        testService = service.getPort(portName, TestService.class, new WSTXFeature());
    }

    @Override
    @Compensatable(distributed = true)
    public void saveData(Boolean throwException) {

        testService.saveData(throwException);
    }

    @Override
    @Compensatable(distributed = true)
    public void saveDataCancelOnFailure(Boolean throwException) {

        testService.saveDataCancelOnFailure(throwException);
    }

    @Override
    public void resetHandlerFlags() {

        testService.resetHandlerFlags();
    }

    @Override
    public boolean wasCompensationHandlerInvoked() {

        return testService.wasCompensationHandlerInvoked();
    }

    @Override
    public boolean wasTransactionLoggedHandlerInvoked() {

        return testService.wasTransactionLoggedHandlerInvoked();
    }

    @Override
    public boolean wasTransactionConfirmedHandlerInvoked() {

        return testService.wasTransactionConfirmedHandlerInvoked();
    }
}