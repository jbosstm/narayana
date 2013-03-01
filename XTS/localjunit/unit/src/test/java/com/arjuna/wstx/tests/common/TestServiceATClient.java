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
 * WS-AT aware client.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class TestServiceATClient implements TestServiceAT {

    private static final String WSDL_URL = "http://localhost:8080/test/TestServiceATService/TestServiceAT?wsdl";

    private static final QName SERVICE_NAME = new QName("http://arjuna.com/wstx/tests/common", "TestServiceATService");

    private static final QName PORT_NAME = new QName("http://arjuna.com/wstx/tests/common", "TestServiceAT");

    private TestServiceAT testService;

    public static TestServiceAT getClientWithoutFeature() throws MalformedURLException {
        TestServiceATClient client = new TestServiceATClient();

        Service service = Service.create(new URL(WSDL_URL), SERVICE_NAME);
        client.testService = service.getPort(PORT_NAME, TestServiceAT.class);

        return client;
    }

    public static TestServiceAT getClientWithWSTXFeature(final boolean isWSTXFeatureEnabled) throws MalformedURLException {
        TestServiceATClient client = new TestServiceATClient();

        Service service = Service.create(new URL(WSDL_URL), SERVICE_NAME);
        client.testService = service.getPort(PORT_NAME, TestServiceAT.class, new WSTXFeature(isWSTXFeatureEnabled));

        return client;
    }

    public static TestServiceAT getClientWithManuallyAddedHandler() throws MalformedURLException {
        TestServiceATClient client = new TestServiceATClient();

        Service service = Service.create(new URL(WSDL_URL), SERVICE_NAME);
        client.testService = service.getPort(PORT_NAME, TestServiceAT.class);

        BindingProvider bindingProvider = (BindingProvider) client.testService;
        @SuppressWarnings("rawtypes")
        List<Handler> handlers = new ArrayList<Handler>(1);
        handlers.add(new JaxWSHeaderContextProcessor());
        bindingProvider.getBinding().setHandlerChain(handlers);

        return client;
    }

    private TestServiceATClient() {
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
    public List<String> getTwoPhaseCommitInvocations() {
        return testService.getTwoPhaseCommitInvocations();
    }

    @Override
    public void reset() {
        testService.reset();
    }

}