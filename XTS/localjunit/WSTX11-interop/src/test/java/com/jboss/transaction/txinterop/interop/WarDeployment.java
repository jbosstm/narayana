/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package com.jboss.transaction.txinterop.interop;

import org.jboss.jbossts.xts.bytemanSupport.participantReadOnly.ParticipantCompletionReadOnlyRules;
import org.jboss.jbossts.xts.soapfault.SoapFaultPortType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.jboss.transaction.txinterop.interop.states.InteropWaitState;
import com.jboss.transaction.txinterop.proxy.ProxyConversation;
import com.jboss.transaction.txinterop.test.TestRunner;
import com.jboss.transaction.txinterop.webservices.CoordinationContextManager;
import com.jboss.transaction.txinterop.webservices.atinterop.ATInteropConstants;
import com.jboss.transaction.txinterop.webservices.atinterop.client.ATInteropClient;
import com.jboss.transaction.txinterop.webservices.atinterop.generated.InitiatorPortType;
import com.jboss.transaction.txinterop.webservices.atinterop.participant.ParticipantAdapter;
import com.jboss.transaction.txinterop.webservices.atinterop.processors.ATInitiatorProcessor;
import com.jboss.transaction.txinterop.webservices.atinterop.sei.InitiatorPortTypeImpl;
import com.jboss.transaction.txinterop.webservices.atinterop.server.ATInitiatorInitialisation;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropConstants;
import com.jboss.transaction.txinterop.webservices.bainterop.client.BAInteropClient;
import com.jboss.transaction.txinterop.webservices.bainterop.generated.InitiatorService;
import com.jboss.transaction.txinterop.webservices.bainterop.participant.ExitParticipant;
import com.jboss.transaction.txinterop.webservices.bainterop.processors.BAInitiatorProcessor;
import com.jboss.transaction.txinterop.webservices.bainterop.sei.ParticipantPortTypeImpl;
import com.jboss.transaction.txinterop.webservices.bainterop.server.BAInitiatorInitialisation;
import com.jboss.transaction.txinterop.webservices.handlers.CoordinationContextHandler;
import com.jboss.transaction.txinterop.webservices.soapfault.client.SoapFaultClient;

import java.net.UnknownHostException;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * @author zhfeng
 *
 */
public class WarDeployment {
    public static WebArchive getDeployment(){
        return ShrinkWrap.create(WebArchive.class, "interop11.war")
                .addPackage(InteropTestCase.class.getPackage())
                .addPackage(InteropWaitState.class.getPackage())
                .addPackage(ProxyConversation.class.getPackage())
                .addPackage(TestRunner.class.getPackage())
                .addPackage(CoordinationContextManager.class.getPackage())
                .addPackage(ATInteropConstants.class.getPackage())
                .addPackage(ATInteropClient.class.getPackage())
                .addPackage(InitiatorPortType.class.getPackage())
                .addPackage(ParticipantAdapter.class.getPackage())
                .addPackage(ATInitiatorProcessor.class.getPackage())
                .addPackage(InitiatorPortTypeImpl.class.getPackage())
                .addPackage(ATInitiatorInitialisation.class.getPackage())
                .addPackage(BAInteropConstants.class.getPackage())
                .addPackage(BAInteropClient.class.getPackage())
                .addPackage(InitiatorService.class.getPackage())
                .addPackage(ExitParticipant.class.getPackage())
                .addPackage(BAInitiatorProcessor.class.getPackage())
                .addPackage(ParticipantPortTypeImpl.class.getPackage())
                .addPackage(BAInitiatorInitialisation.class.getPackage())
                .addPackage(CoordinationContextHandler.class.getPackage())
                .addPackage(SoapFaultClient.class.getPackage())
                .addPackage(SoapFaultPortType.class.getPackage())
                .addPackage(ParticipantCompletionReadOnlyRules.class.getPackage())
                .addAsResource("interop11/participanthandlers.xml", "com/jboss/transaction/txinterop/webservices/atinterop/sei/participanthandlers.xml")
                .addAsResource("interop11/participanthandlers.xml", "com/jboss/transaction/txinterop/webservices/bainterop/sei/participanthandlers.xml")
                .addAsWebInfResource("interop11/wsdl/interopat.wsdl", "classes/com/jboss/transaction/txinterop/webservices/atinterop/generated/wsdl/interopat.wsdl")
                .addAsWebInfResource("interop11/wsdl/interopat-binding.wsdl", "classes/com/jboss/transaction/txinterop/webservices/atinterop/generated/wsdl/interopat-binding.wsdl")
                .addAsWebInfResource("interop11/wsdl/interopat-initiator-binding.wsdl", "classes/com/jboss/transaction/txinterop/webservices/atinterop/generated/wsdl/interopat-initiator-binding.wsdl")
                .addAsWebInfResource("interop11/wsdl/interopat-participant-binding.wsdl", "classes/com/jboss/transaction/txinterop/webservices/atinterop/generated/wsdl/interopat-participant-binding.wsdl")
                .addAsWebInfResource("interop11/wsdl/interopba.wsdl", "classes/com/jboss/transaction/txinterop/webservices/bainterop/generated/wsdl/interopba.wsdl")
                .addAsWebInfResource("interop11/wsdl/interopba-binding.wsdl", "classes/com/jboss/transaction/txinterop/webservices/bainterop/generated/wsdl/interopba-binding.wsdl")
                .addAsWebInfResource("interop11/wsdl/interopba-initiator-binding.wsdl", "classes/com/jboss/transaction/txinterop/webservices/bainterop/generated/wsdl/interopba-initiator-binding.wsdl")
                .addAsWebInfResource("interop11/wsdl/interopba-participant-binding.wsdl", "classes/com/jboss/transaction/txinterop/webservices/bainterop/generated/wsdl/interopba-participant-binding.wsdl")
                .addAsResource("soapfault/wsdl/soapfault.wsdl", "com/jboss/transaction/txinterop/webservices/soapfault/generated/wsdl/soapfault.wsdl")
                .addAsResource("soapfault/wsdl/soapfault.wsdl", "org/jboss/jbossts/xts/soapfault/wsdl/soapfault.wsdl")
                .addAsResource("soapfault/wsdl/envelope.xsd", "org/jboss/jbossts/xts/soapfault/wsdl/envelope.xsd")
                .addAsResource("processor.xsl", "com/jboss/transaction/txinterop/test/processor.xsl")
                .addAsWebResource("web/index.jsp", "index.jsp")
                .addAsWebResource("web/details.jsp", "details.jsp")
                .addAsWebResource("web/invalidParameters.html", "invalidParameters.html")
                .addAsWebResource("web/results.jsp", "results.jsp")
                .addAsWebInfResource("web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.jts,org.jboss.ws.api,javax.xml.ws.api,org.jboss.xts,org.dom4j,org.jboss.ws.jaxws-client services export,org.jboss.ws.cxf.jbossws-cxf-client services export,com.sun.xml.bind services export\n"), "MANIFEST.MF");
    }

    static String getLocalHost() {
        return isIPv6() ? "[::1]" : "localhost";
    }

    static boolean isIPv6() {
        try {
            if (InetAddress.getLocalHost() instanceof Inet6Address || System.getenv("IPV6_OPTS") != null)
                return true;
        } catch (final UnknownHostException uhe) {
        }

        return false;
    }
}
