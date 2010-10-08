/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010
 * @author JBoss Inc.
 */
package org.jboss.jbossts.star.test;

import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.jbossts.star.test.BaseTest;
import org.jboss.jbossts.star.test.SpecTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientIntegrationTest extends BaseTest {
    SpecTest specTest = new SpecTest();

    @BeforeClass
    public static void startServer() throws Exception {
        startContainer(TxSupport.DEF_TX_URL, "org.jboss.jbossts.star.test", BaseTest.TransactionalResource.class);
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
        specTest.testRecoveryURL();
    }

}
