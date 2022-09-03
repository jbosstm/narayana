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
package com.arjuna.wstx.tests.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.Handler;

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
        } else if (!baseAddress.toLowerCase().startsWith("http://") && !baseAddress.toLowerCase().startsWith("https://")) {
            baseAddress = "http://" + baseAddress;
        }

        if (basePort == null) {
            basePort = "8080";
        }

        return baseAddress + ":" + basePort;
    }

}
