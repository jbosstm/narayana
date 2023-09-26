/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.junit;

import org.jboss.arquillian.container.test.api.*;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.jbossts.txbridge.outbound.BridgeXAResource;
import org.jboss.jbossts.txbridge.tests.common.AbstractCrashRecoveryTests;
import org.jboss.jbossts.txbridge.tests.outbound.client.TestClient;
import org.jboss.jbossts.txbridge.tests.outbound.service.TestServiceImpl;
import org.jboss.jbossts.txbridge.tests.outbound.utility.TestDurableParticipant;
import org.jboss.jbossts.txbridge.tests.outbound.utility.TestVolatileParticipant;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.*;

import org.jboss.byteman.contrib.dtest.*;
import org.junit.runner.RunWith;

import java.net.URL;


/**
 * Crash Recovery test cases for the outbound side of the transaction bridge.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-05
 * @author Ivo Studensky (istudens@redhat.com)
 */
@RunWith(Arquillian.class)
@RunAsClient
public class OutboundCrashRecoveryTests extends AbstractCrashRecoveryTests {
    private static Logger log = Logger.getLogger(OutboundCrashRecoveryTests.class);

    @Deployment(name = OUTBOUND_SERVICE_DEPLOYMENT_NAME, managed = false, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> createServiceArchive() {
        return getOutboundServiceArchive();
    }

    @Deployment(name = OUTBOUND_CLIENT_DEPLOYMENT_NAME, managed = false, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> createClientArchive() {
        return getOutboundClientArchive();
    }


    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    private InstrumentedClass instrumentedTestVolatileParticipant;
    private InstrumentedClass instrumentedTestDurableParticipant;


    @Before
    public void setUp() throws Exception {
        cleanTxStore();

        // start up the appserver
        String javaVmArguments = System.getProperty("server.jvm.args").trim();
        log.trace("javaVmArguments = " + javaVmArguments);
        controller.start(CONTAINER, new Config().add("javaVmArguments", javaVmArguments).map());
        // deploy the tests
        deployer.deploy(OUTBOUND_SERVICE_DEPLOYMENT_NAME);
        deployer.deploy(OUTBOUND_CLIENT_DEPLOYMENT_NAME);

        instrumentor.setRedirectedSubmissionsFile(null);
        instrument(instrumentor);

        instrumentor.injectOnCall(TestServiceImpl.class, "doNothing", "$0.enlistVolatileParticipant(1), $0.enlistDurableParticipant(1)");
    }

    @After
    public void tearDown() throws Exception {
        try {
            instrumentor.removeAllInstrumentation();
        } finally {
            // un-deploy the tests
            deployer.undeploy(OUTBOUND_CLIENT_DEPLOYMENT_NAME);
            deployer.undeploy(OUTBOUND_SERVICE_DEPLOYMENT_NAME);
            // shut down the appserver
            controller.stop(CONTAINER);
        }
    }

    @Override
    public void instrument(Instrumentor instrumentor) throws Exception {
        instrumentedTestVolatileParticipant = instrumentor.instrumentClass(TestVolatileParticipant.class);
        instrumentedTestDurableParticipant = instrumentor.instrumentClass(TestDurableParticipant.class);
    }

    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCrashOneLog(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$1 = true"); // shouldCommit=true

        instrumentor.crashAtMethodExit("^XTSATRecoveryManager", "writeParticipantRecoveryRecord");

        execute(baseURL + TestClient.URL_PATTERN, false);

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");

        rebootServer(controller);

        doRecovery();
        doRecovery();

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
    }

    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCrashTwoLogs(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$1 = true"); // shouldCommit=true

        instrumentor.crashAtMethodExit(BridgeXAResource.class, "prepare");

        execute(baseURL + TestClient.URL_PATTERN, false);

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");

        rebootServer(controller);

        doRecovery();
        doRecovery();

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
    }

    // this one requires <property name="commitOnePhase">false</property> on CoordinatorEnvironmentBean
    @Test
    @Ignore
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCrashThreeLogs(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$1 = true"); // shouldCommit=true

        instrumentor.crashAtMethodEntry(BridgeXAResource.class, "commit");

        execute(baseURL + TestClient.URL_PATTERN, false);

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");

        rebootServer(controller);

        doRecovery();
        doRecovery();

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodCalled("commit");
    }

}