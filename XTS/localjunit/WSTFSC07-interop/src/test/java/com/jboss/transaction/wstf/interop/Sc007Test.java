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

package com.jboss.transaction.wstf.interop;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantReadOnly.ParticipantCompletionReadOnlyRules;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jboss.transaction.wstf.proxy.ProxyConversation;

import java.net.UnknownHostException;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * @author zhfeng
 *
 */
@RunWith(Arquillian.class)
public class Sc007Test {
    private String participantURI = "http://" + getLocalHost() + ":8080/sc007/ParticipantService";
    
    @Inject
    Sc007TestCase test;
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "sc007.war")
                .addPackage("com.jboss.transaction.wstf.interop")
                .addPackage("com.jboss.transaction.wstf.interop.states")
                .addPackage("com.jboss.transaction.wstf.proxy")
                .addPackage("com.jboss.transaction.wstf.test")
                .addPackage("com.jboss.transaction.wstf.webservices")
                .addPackage("com.jboss.transaction.wstf.webservices.handlers")
                .addPackage("com.jboss.transaction.wstf.webservices.sc007")
                .addPackage("com.jboss.transaction.wstf.webservices.sc007.client")
                .addPackage("com.jboss.transaction.wstf.webservices.sc007.generated")
                .addPackage("com.jboss.transaction.wstf.webservices.sc007.participant")
                .addPackage("com.jboss.transaction.wstf.webservices.sc007.processors")
                .addPackage("com.jboss.transaction.wstf.webservices.sc007.sei")
                .addPackage("com.jboss.transaction.wstf.webservices.sc007.server")
                .addPackage("com.jboss.transaction.wstf.webservices.soapfault.client")
                .addPackage("org.jboss.jbossts.xts.soapfault")
                .addPackage("org.jboss.jbossts.xts.bytemanSupport.participantReadOnly")
                .addAsResource("sc007/participanthandlers.xml", "com/jboss/transaction/wstf/webservices/sc007/sei/participanthandlers.xml")
                .addAsWebInfResource("sc007/wsdl/sc007.wsdl", "classes/com/jboss/transaction/wstf/webservices/sc007/generated/wsdl/sc007.wsdl")                          
                .addAsResource("soapfault/wsdl/soapfault.wsdl", "org/jboss/jbossts/xts/soapfault/soapfault.wsdl")
                .addAsResource("soapfault/wsdl/envelope.xsd", "org/jboss/jbossts/xts/soapfault/envelope.xsd")
                .addAsWebResource("web/index.jsp", "index.jsp")
                .addAsWebResource("web/details.jsp", "details.jsp")
                .addAsWebResource("web/invalidParameters.html", "invalidParameters.html")
                .addAsWebResource("web/results.jsp", "results.jsp")
                .addAsResource("com/jboss/transaction/wstf/test/processor.xsl")
                .addAsWebInfResource("web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.jts,org.jboss.ws.api,javax.xml.ws.api,org.jboss.xts,org.dom4j,org.jboss.ws.jaxws-client services export,org.jboss.ws.cxf.jbossws-cxf-client services export,com.sun.xml.bind services export\n"), "MANIFEST.MF");
    }

    @BeforeClass()
    public static void submitBytemanScript() throws Exception {
        BMScript.submit(ParticipantCompletionReadOnlyRules.RESOURCE_PATH);
    }

    @AfterClass()
    public static void removeBytemanScript() {
        BMScript.remove(ParticipantCompletionReadOnlyRules.RESOURCE_PATH);
    }
    
    @Before
    public void setUp() {
        test.setParticipantURI(participantURI);
        String conversationId = ProxyConversation.createConversation();
        test.setConversationId(conversationId);
    }
    
    @Test
    public void test1_1() throws Exception {
        test.test1_1();
    }
    
    @Test
    public void test1_2() throws Exception {
        test.test1_2();
    }
    
    @Test
    public void test2_1() throws Exception {
        test.test2_1();
    }
    
    @Test
    public void test2_2() throws Exception {
        test.test2_2();
    }
    
    @Test
    public void test3_1() throws Exception {
        test.test3_1();
    }
    
    @Test
    public void test3_2() throws Exception {
        test.test3_2();
    }
    
    @Test
    public void test3_3() throws Exception {
        test.test3_3();
    }
    
    @Test
    public void test3_4() throws Exception {
        ParticipantCompletionReadOnlyRules.enableReadOnlyCheck();
        test.test3_4();
    }
    
    @Test
    public void test3_5() throws Exception {
        test.test3_5();
    }
    
    @Test
    public void test3_6() throws Exception {
        test.test3_6();
    }
    
    @Test
    public void test3_7() throws Exception {
        test.test3_7();
    }
    
    @Test
    public void test3_8() throws Exception {
        test.test3_8();
    }
    
    @Test
    public void test3_9() throws Exception {
        test.test3_9();
    }
    
    @Test
    public void test3_10() throws Exception {
        test.test3_10();
    }
    
    @Test
    public void test3_11() throws Exception {
        test.test3_11();
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
