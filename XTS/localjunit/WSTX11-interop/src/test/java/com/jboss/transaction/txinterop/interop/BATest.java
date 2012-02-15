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
public class BATest {
    private String participantURI = "http://localhost:8080/interop11/BAParticipantService";
    private int testTimeout = 120000;
    private boolean asyncTest = true;
    private String name = "BATest";
    
    @Inject
    BATestCase test;
    
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
    public void testBA1_1() throws Exception {
        test.testBA1_1();
    }
    
    @Test
    public void testBA1_2() throws Exception {
        test.testBA1_2();
    }
    
    @Test
    public void testBA1_3() throws Exception {
        test.testBA1_3();
    }
    
    @Test
    public void testBA1_4() throws Exception {
        test.testBA1_4();
    }
    
    @Test
    public void testBA1_5() throws Exception {
        test.testBA1_5();
    }
    
    @Test
    public void testBA1_6() throws Exception {
        test.testBA1_6();
    }
    
    @Test
    public void testBA1_8() throws Exception {
        test.testBA1_8();
    }
    
    @Test
    public void testBA1_9() throws Exception {
        test.testBA1_9();
    }
    
    @Test
    public void testBA1_10() throws Exception {
        test.testBA1_10();
    }
    
    @Test
    public void testBA1_11() throws Exception {
        test.testBA1_11();
    }
    
    @After
    public void tearDown() {
        String conversationId = test.getConversationId();
        ProxyConversation.removeConversation(conversationId) ;
        test.setConversationId(null);
    }
}
