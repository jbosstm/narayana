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

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jboss.transaction.txinterop.proxy.ProxyConversation;

/**
 * @author zhfeng
 *
 */
@RunWith(Arquillian.class)
public class ATTest {
    private String participantURI = "http://localhost:8080/interop11/ATParticipantService";
    private int testTimeout = 120000;
    private boolean asyncTest = true;
    private String name = "ATTest";
    
    @Inject
    ATTestCase test;

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }
    
    @Before
    public void setUp() {
        test.setParticipantURI(participantURI);
        test.setTestTimeout(testTimeout);
        test.setAsyncTest(asyncTest);
        test.setName(name);
        String conversationId = ProxyConversation.createConversation();
        test.setConversationId(conversationId);
    }
    
    @Test
    public void testAT1_1() throws Exception {
       test.testAT1_1();
    }
    
    @Test
    public void testAT1_2() throws Exception {
        test.testAT1_2();
    }
    
    @Test
    public void testAT2_1() throws Exception {
        test.testAT2_1();
    }
    
    @Test
    public void testAT2_2() throws Exception {
        test.testAT2_2();
    }
    
    @Test
    public void testAT3_1() throws Exception {
        test.testAT3_1();
    }
    
    @Test
    public void testAT3_2() throws Exception {
        test.testAT3_2();
    }
    
    @Test
    public void testAT3_3() throws Exception {
        test.testAT3_3();
    }
    
    @Test
    public void testAT4_1() throws Exception {
        test.testAT4_1();
    }
    
    @Test
    public void testAT4_2() throws Exception {
        test.testAT4_2();
    }
    
    @Test
    public void testAT5_1() throws Exception {
        test.testAT5_1();
    }
    
    @Test
    public void testAT5_2() throws Exception {
        test.testAT5_2();
    }
    
    @Test
    public void testAT5_3() throws Exception {
        test.testAT5_3();
    }
    
    @Test
    public void testAT5_4() throws Exception {
        test.testAT5_4();
    }
    
    @Test
    public void testAT5_5() throws Exception {
        test.testAT5_5();
    }
    
    @Test
    public void testAT5_6() throws Exception {
        test.testAT5_6();
    }
    
    @After
    public void tearDown() {
        String conversationId = test.getConversationId();
        ProxyConversation.removeConversation(conversationId) ;
        test.setConversationId(null);
    }
}
