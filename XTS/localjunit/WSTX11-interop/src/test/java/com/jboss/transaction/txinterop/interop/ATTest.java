/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.jboss.transaction.txinterop.interop;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantReadOnly.ParticipantCompletionReadOnlyRules;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

import com.jboss.transaction.txinterop.proxy.ProxyConversation;

/**
 * @author zhfeng
 *
 */
@RunWith(Arquillian.class)
public class ATTest {
    private String participantURI = "http://" + WarDeployment.getLocalHost() +
        ":8080/interop11/ATParticipantService";
    private int testTimeout = 120000;
    private boolean asyncTest = true;
    private String name = "ATTest";
    
    @Inject
    ATTestCase test;

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @BeforeClass
    public static void submitBytemanScript() throws Exception {
        BMScript.submit(ParticipantCompletionReadOnlyRules.RESOURCE_PATH);
    }

    @AfterClass
    public static void removeBytemanScript() {
        BMScript.remove(ParticipantCompletionReadOnlyRules.RESOURCE_PATH);
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
        ParticipantCompletionReadOnlyRules.enableReadOnlyCheck();
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