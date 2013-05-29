package com.arjuna.wstx.tests.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;
import com.arjuna.mw.wst11.client.WSTXFeature;

/**
 * WS-BA client.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class TestServiceBAClient implements TestServiceBA {

    private static final String WSDL_URL = getBaseUrl() + "/test/TestServiceBAService/TestServiceBA?wsdl";

    private static final QName SERVICE_NAME = new QName("http://arjuna.com/wstx/tests/common", "TestServiceBAService");

    private static final QName PORT_NAME = new QName("http://arjuna.com/wstx/tests/common", "TestServiceBA");

    private TestServiceBA testService;

    public static TestServiceBA getClientWithoutFeature() throws MalformedURLException {
        TestServiceBAClient client = new TestServiceBAClient();

        Service service = Service.create(new URL(WSDL_URL), SERVICE_NAME);
        client.testService = service.getPort(PORT_NAME, TestServiceBA.class);

        return client;
    }

    public static TestServiceBA getClientWithWSTXFeature(final boolean isWSTXFeatureEnabled) throws MalformedURLException {
        TestServiceBAClient client = new TestServiceBAClient();

        Service service = Service.create(new URL(WSDL_URL), SERVICE_NAME);
        client.testService = service.getPort(PORT_NAME, TestServiceBA.class, new WSTXFeature(isWSTXFeatureEnabled));

        return client;
    }

    public static TestServiceBA getClientWithManuallyAddedHandler() throws MalformedURLException {
        TestServiceBAClient client = new TestServiceBAClient();

        Service service = Service.create(new URL(WSDL_URL), SERVICE_NAME);
        client.testService = service.getPort(PORT_NAME, TestServiceBA.class);

        BindingProvider bindingProvider = (BindingProvider) client.testService;
        @SuppressWarnings("rawtypes")
        List<Handler> handlers = new ArrayList<Handler>(1);
        handlers.add(new JaxWSHeaderContextProcessor());
        bindingProvider.getBinding().setHandlerChain(handlers);

        return client;
    }

    private TestServiceBAClient() {
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
    public List<String> getBusinessActivityInvocations() {
        return testService.getBusinessActivityInvocations();
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
        }

        if (basePort == null) {
            basePort = "8080";
        }

        return baseAddress + ":" + basePort;
    }

}