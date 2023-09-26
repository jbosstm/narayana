/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.junit;

import org.jboss.arquillian.container.test.api.*;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.jbossts.txbridge.tests.common.AbstractBasicTests;
import org.jboss.jbossts.txbridge.tests.outbound.client.TestClient;
import org.jboss.jbossts.txbridge.tests.outbound.service.TestServiceImpl;
import org.jboss.jbossts.txbridge.tests.outbound.utility.TestDurableParticipant;
import org.jboss.jbossts.txbridge.tests.outbound.utility.TestVolatileParticipant;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.*;

import org.jboss.byteman.contrib.dtest.*;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.mwlabs.wst.at.participants.VolatileTwoPhaseCommitParticipant;

import java.net.URL;

/**
 * Basic (i.e. non-crashrec) test cases for the outbound side of the transaction bridge.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-05
 * @author Ivo Studensky (istudens@redhat.com)
 */
@RunWith(Arquillian.class)
@RunAsClient
public class OutboundBasicTests extends AbstractBasicTests {

    @Deployment(name = OUTBOUND_SERVICE_DEPLOYMENT_NAME, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> createServiceArchive() {
        return getOutboundServiceArchive();
    }

    @Deployment(name = OUTBOUND_CLIENT_DEPLOYMENT_NAME, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> createClientArchive() {
        return getOutboundClientArchive();
    }


    @ArquillianResource
    private ContainerController controller;

    private InstrumentedClass instrumentedTestVolatileParticipant;
    private InstrumentedClass instrumentedTestDurableParticipant;

    @Before
    public void setUp() throws Exception {
        // start up the appserver
        controller.start(CONTAINER);

        instrumentor.setRedirectedSubmissionsFile(null);

        instrumentedTestVolatileParticipant = instrumentor.instrumentClass(TestVolatileParticipant.class);
        instrumentedTestDurableParticipant = instrumentor.instrumentClass(TestDurableParticipant.class);

        instrumentor.injectOnCall(TestServiceImpl.class, "doNothing", "$0.enlistVolatileParticipant(1), $0.enlistDurableParticipant(1)");
    }

    @After
    public void tearDown() throws Exception {
        instrumentor.removeAllInstrumentation();

        // shut down the appserver
        controller.stop(CONTAINER);
    }


    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testRollback(@ArquillianResource URL baseURL) throws Exception {

        execute(baseURL.toString() + TestClient.URL_PATTERN);

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
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCommit(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$1 = true"); // shouldCommit=true

        execute(baseURL.toString() + TestClient.URL_PATTERN);

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
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testBeforeCompletionFailure(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestVolatileParticipant.class, "prepare", "return new com.arjuna.wst.Aborted()");

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$1 = true"); // shouldCommit=true

        execute(baseURL.toString() + TestClient.URL_PATTERN);

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
    }

    /**
     * Reproducing trouble of possible NullPointerException being thrown from the BasicAction#doAbort.
     * See JBTM-2948 for more details.
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testSynchronizationFailure(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectFault(VolatileTwoPhaseCommitParticipant.class, "beforeCompletion",
            RuntimeException.class, new Object[]{"injected BeforeCompletion fault"});
        instrumentor.injectFault(com.arjuna.wst11.stub.ParticipantStub.class, "rollback",
            com.arjuna.wst.SystemException.class, new Object[]{"injected bridge participant fault"});

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$1 = true"); // shouldCommit=true

        InstrumentedClass basicActionInstrumented = instrumentor.instrumentClass(BasicAction.class);

        String output = execute(baseURL.toString() + TestClient.URL_PATTERN);

        Assert.assertTrue("Failure was injected, rollback was expected", output.contains("RollbackException"));
        // if clean from NPE then the cause of the doAbort call can be saved as deffered exception
        // at least one instrumented BasicAction has to do so
        for(InstrumentedInstance ic: basicActionInstrumented.getInstances()) {
            if(ic.getInvocationCount("addDeferredThrowables") > 0)
                return;
        }
        Assert.fail("There was thrown NullPointerException on BasicAction#doAbort, consult server.log");
    }

    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testPrepareReadonly(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestDurableParticipant.class, "prepare", "return new com.arjuna.wst.ReadOnly()");

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$1 = true"); // shouldCommit=true

        execute(baseURL.toString() + TestClient.URL_PATTERN);

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");
        instrumentedTestVolatileParticipant.assertMethodCalled("commit");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
    }

    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testPrepareFailure(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestDurableParticipant.class, "prepare", "return new com.arjuna.wst.Aborted()");

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$1 = true"); // shouldCommit=true

        execute(baseURL.toString() + TestClient.URL_PATTERN);

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodCalled("rollback");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
    }

}