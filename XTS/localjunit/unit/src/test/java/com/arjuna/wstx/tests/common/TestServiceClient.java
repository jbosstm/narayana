/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wstx.tests.common;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import com.arjuna.mw.wst11.client.WSTXFeature;

/**
 * WS-AT and WS-BA unaware client.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class TestServiceClient implements TestService {

    private static final String WSDL_URL = getBaseUrl() + "/test/TestServiceService/TestService?wsdl";

    private static final QName SERVICE_NAME = new QName("http://arjuna.com/wstx/tests/common", "TestServiceService");

    private static final QName PORT_NAME = new QName("http://arjuna.com/wstx/tests/common", "TestService");

    private TestService testService;

    public static TestService getClientWithoutFeature() throws MalformedURLException {
        TestServiceClient client = new TestServiceClient();

        Service service = Service.create(new URL(WSDL_URL), SERVICE_NAME);
        client.testService = service.getPort(PORT_NAME, TestService.class);

        return client;
    }

    public static TestService getClientWithWSTXFeature(final boolean isWSTXFeatureEnabled) throws MalformedURLException {
        TestServiceClient client = new TestServiceClient();

        Service service = Service.create(new URL(WSDL_URL), SERVICE_NAME);
        client.testService = service.getPort(PORT_NAME, TestService.class, new WSTXFeature(isWSTXFeatureEnabled));

        return client;
    }

    private TestServiceClient() {
    }

    @Override
    public void increment() {
        testService.increment();
    }

    @Override
    public int getCounter() {
        return testService.getCounter();
    }

    @Override
    public void reset() {
        testService.reset();
    }

    private static String getBaseUrl() {
        String baseAddress = System.getProperty("jboss.bind.address");
        String basePort = System.getProperty("jboss.bind.port");

        if (baseAddress == null) {
            baseAddress = "http://localhost";
        } else if (!baseAddress.toLowerCase().startsWith("http://") && !baseAddress.toLowerCase().startsWith("https://")) {
            baseAddress = "http://" + baseAddress;
        }

        if (basePort == null) {
            basePort = "8080";
        }

        return baseAddress + ":" + basePort;
    }

}
