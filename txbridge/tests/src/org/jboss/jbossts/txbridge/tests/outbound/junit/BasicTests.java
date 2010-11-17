/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.txbridge.tests.outbound.junit;

import org.apache.commons.httpclient.HttpMethodBase;

import org.jboss.jbossts.txbridge.tests.outbound.client.TestClient;
import org.jboss.jbossts.txbridge.tests.outbound.service.TestServiceImpl;
import org.jboss.jbossts.txbridge.tests.outbound.utility.TestDurableParticipant;
import org.jboss.jbossts.txbridge.tests.outbound.utility.TestVolatileParticipant;
import org.junit.*;
import static org.junit.Assert.*;

import org.jboss.byteman.agent.submit.Submit;
import org.jboss.byteman.contrib.dtest.*;

import com.arjuna.qa.junit.HttpUtils;

import java.net.URL;

/**
 * Basic (i.e. non-crashrec) test cases for the outbound side of the transaction bridge.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-05
 */
public class BasicTests
{
    private static final String baseURL = "http://localhost:8080/txbridge-outbound-tests-client/testclient";

    private static Instrumentor instrumentor;
    private InstrumentedClass instrumentedTestVolatileParticipant;
    private InstrumentedClass instrumentedTestDurableParticipant;

    @BeforeClass
    public static void beforeClass() throws Exception {
        instrumentor = new Instrumentor(new Submit(), 1199);
        //instrumentor.installHelperJar("/home/jhalli/IdeaProjects/jboss/byteman_trunk/contrib/dtest/build/lib/byteman-dtest.jar");
    }

    @Before
    public void setUp() throws Exception {
        instrumentedTestVolatileParticipant = instrumentor.instrumentClass(TestVolatileParticipant.class);
        instrumentedTestDurableParticipant = instrumentor.instrumentClass(TestDurableParticipant.class);

        instrumentor.injectOnCall(TestServiceImpl.class, "doNothing", "$0.enlistVolatileParticipant(1), $0.enlistDurableParticipant(1)");
    }

    @After
    public void tearDown() throws Exception {
        instrumentor.removeAllInstrumentation();
    }

    private void execute() throws Exception {
        HttpMethodBase request = HttpUtils.accessURL(new URL(baseURL));
        String response = request.getResponseBodyAsString().trim();
        assertEquals("finished", response);
    }

    @Test
    public void testRollback() throws Exception {

        execute();

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodNotCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");
        instrumentedTestVolatileParticipant.assertMethodCalled("rollback");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
        instrumentedTestDurableParticipant.assertMethodCalled("rollback");
    }

    @Test
    public void testCommit() throws Exception {

        instrumentor.injectOnCall(TestClient.class,  "terminateTransaction", "$1 = true"); // shouldCommit=true

        execute();

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodCalled("commit");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodCalled("commit");
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");
    }

    @Test
    public void testBeforeCompletionFailure() throws Exception {

        instrumentor.injectOnCall(TestVolatileParticipant.class, "prepare", "return new com.arjuna.wst.Aborted()");

        instrumentor.injectOnCall(TestClient.class,  "terminateTransaction", "$1 = true"); // shouldCommit=true

        execute();

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
    }

    @Test
    public void testPrepareReadonly() throws Exception {

        instrumentor.injectOnCall(TestDurableParticipant.class, "prepare", "return new com.arjuna.wst.ReadOnly()");

        instrumentor.injectOnCall(TestClient.class,  "terminateTransaction", "$1 = true"); // shouldCommit=true

        execute();

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");
        instrumentedTestVolatileParticipant.assertMethodCalled("commit");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
    }

    @Test
    public void testPrepareFailure() throws Exception {

        instrumentor.injectOnCall(TestDurableParticipant.class, "prepare", "return new com.arjuna.wst.Aborted()");

        instrumentor.injectOnCall(TestClient.class,  "terminateTransaction", "$1 = true"); // shouldCommit=true

        HttpMethodBase request = HttpUtils.accessURL(new URL(baseURL));
        String response = request.getResponseBodyAsString().trim();

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodCalled("rollback");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
    }
}