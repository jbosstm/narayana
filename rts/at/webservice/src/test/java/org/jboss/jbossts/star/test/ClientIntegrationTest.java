/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.test;

import org.jboss.jbossts.star.util.TxSupport;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientIntegrationTest extends BaseTest {
    SpecTest specTest = new SpecTest();

    @BeforeClass
    public static void startServer() throws Exception {
        startContainer(TxSupport.TXN_MGR_URL, "org.jboss.jbossts.star.test", BaseTest.TransactionalResource.class);
    }

    @Test
    public void testTransactionUrls() throws Exception {
        specTest.testTransactionUrls();
    }
    @Test
    public void testTransactionTimeout() throws Exception {
        specTest.testTransactionTimeout();
    }
     @Test
    public void testRollback() throws Exception {
         specTest.testRollback();
     }

    @Test
    public void testEnlistResource() throws Exception {
        specTest.testEnlistResource();
    }

    @Test
    public void testHeuristic() throws Exception {
        specTest.testHeuristic();
    }
    @Test
    public void testSpec6() throws Exception {
        specTest.testSpec6();
    }
    @Test
    public void testParticipantStatus() throws Exception {
        specTest.testParticipantStatus();
    }
    @Test
    public void testCannotEnlistDuring2PC() throws Exception {
        specTest.testCannotEnlistDuring2PC();
    }
    @Test // recovery
    public void testRecoveryURL() throws Exception {
        specTest.testRecoveryURLTwoPhaseAwareWithNotification();
        specTest.testRecoveryURLTwoPhaseAwareWithoutNotification();
        specTest.testRecoveryURLTwoPhaseUnawareWithNotification();
        specTest.testRecoveryURLTwoPhaseUnawareWithoutNotification();
    }

}