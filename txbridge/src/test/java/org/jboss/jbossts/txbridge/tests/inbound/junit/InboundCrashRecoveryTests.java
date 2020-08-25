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
package org.jboss.jbossts.txbridge.tests.inbound.junit;

import org.jboss.arquillian.container.test.api.*;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.jbossts.txbridge.inbound.BridgeDurableParticipant;
import org.jboss.jbossts.txbridge.tests.common.AbstractCrashRecoveryTests;
import org.jboss.jbossts.txbridge.tests.inbound.client.TestClient;
import org.jboss.jbossts.txbridge.tests.inbound.service.TestServiceImpl;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestSynchronization;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestXAResource;

import org.jboss.jbossts.txbridge.tests.inbound.utility.TestXAResourceRecovered;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestXAResourceRecoveryHelper;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.*;

import org.jboss.byteman.contrib.dtest.*;
import org.junit.runner.RunWith;

import com.arjuna.ats.internal.arjuna.recovery.PeriodicRecovery;

import java.net.URL;
import java.util.concurrent.Executors;


/**
 * Crash Recovery test cases for the inbound side of the transaction bridge.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-05
 * @author Ivo Studensky (istudens@redhat.com)
 */
@RunWith(Arquillian.class)
@RunAsClient
public class InboundCrashRecoveryTests extends AbstractCrashRecoveryTests {
    private static Logger log = Logger.getLogger(InboundCrashRecoveryTests.class);

    @Deployment(name = INBOUND_SERVICE_DEPLOYMENT_NAME, managed = false, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> createServiceArchive() {
        return getInboundServiceArchive();
    }

    @Deployment(name = INBOUND_CLIENT_DEPLOYMENT_NAME, managed = false, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> createClientArchive() {
        return getInboundClientArchive();
    }


    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    private InstrumentedClass instrumentedTestSynchronization;
    private InstrumentedClass instrumentedTestXAResource;


    @Before
    public void setUp() throws Exception {
        cleanTxStore();

        // start up the appserver
        String javaVmArguments = System.getProperty("server.jvm.args").trim();
        log.trace("javaVmArguments = " + javaVmArguments);
        controller.start(CONTAINER, new Config().add("javaVmArguments", javaVmArguments).map());
        // deploy the tests
        deployer.deploy(INBOUND_SERVICE_DEPLOYMENT_NAME);
        deployer.deploy(INBOUND_CLIENT_DEPLOYMENT_NAME);

        instrumentor.setRedirectedSubmissionsFile(null);
        instrumentedTestSynchronization = instrumentor.instrumentClass(TestSynchronization.class);
        instrumentedTestXAResource = instrumentor.instrumentClass(TestXAResource.class);

        instrumentor.injectOnCall(TestServiceImpl.class, "doNothing", "$0.enlistSynchronization(1), $0.enlistXAResource(1)");
    }

    @After
    public void tearDown() throws Exception {
        try {
            instrumentor.removeAllInstrumentation();
        } finally {
            // un-deploy the tests
            deployer.undeploy(INBOUND_CLIENT_DEPLOYMENT_NAME);
            deployer.undeploy(INBOUND_SERVICE_DEPLOYMENT_NAME);
            // shut down the appserver
            controller.stop(CONTAINER);
        }
    }

    @Override
    protected void instrumentationOnServerReboot() throws Exception {
        instrumentedTestSynchronization = instrumentor.instrumentClass(TestSynchronization.class);
        instrumentedTestXAResource = instrumentor.instrumentClass(TestXAResourceRecovered.class);
    }


    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCrashOneLog(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$2 = true"); // shouldCommit=true
        instrumentor.crashAtMethodExit(TestXAResource.class, "prepare");

        execute(baseURL + TestClient.URL_PATTERN, false);

        instrumentedTestSynchronization.assertKnownInstances(1);
        instrumentedTestSynchronization.assertMethodCalled("beforeCompletion");
        instrumentedTestSynchronization.assertMethodNotCalled("afterCompletion");

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("prepare");
        instrumentedTestXAResource.assertMethodNotCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");

        rebootServer(controller);

        doRecovery();
        doRecovery();

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("recover");
        instrumentedTestXAResource.assertMethodCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");
    }

    /**
     * <ul>
     *   <li>A test client is deployed on the WFLY</li>
     *   <li>Test case calls HTTP call to the client</li>
     *   <li>The client starts JTA transaction</li>
     *   <li>The client invokes WebService call to the WFLY. The WS is the single participant of the original transaction.</li>
     *   <li>The WebService did some work - in this case it enlists
     *       2 mock <code>TestXAResource</code>s in the business method</li>
     *   <li>The call comes back to client which commits the transaction</li>
     *   <li>The 1PC is about to start but 1PC is disabled (JBTM-3138) for the WCSF participant.
     *       The prepare and commit calls hits the WS participant.</li>
     *   <li>The WS participant process the 2PC on subordinate resources:<br/>
     *       - <code>BridgeDurableParticipant</code><br/>
     *       - <code>BridgeVolatileParticipant</code><br/>
     *       - Two <code>TestXAResource</code></br></li>
     *   <li>The commit phase starts. The first <code>TestXAResource</code>s is committed,
     *       before the second <code>TestXAResource</code> could be committed the JVM crashes</li>
     *   <li>The recovery is expected to commit the second <code>TestXAResource</code> later on</li>
     * </ul>
     */
    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCrashCommitOnePhase(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestServiceImpl.class, "doNothing", "$0.enlistXAResource(2)");
        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$2 = true"); // shouldCommit=true
        instrumentor.installRule(RuleConstructor.createRule("crashOnSecondTestResource")
            .onClass(TestXAResource.class)
            .inMethod("commit")
            .atEntry()
            .ifCondition("NOT flag(\"testxaresourcecrash\")")
            .doAction("debug(\"killing TestXAResource\"); killJVM()"));

        execute(baseURL + TestClient.URL_PATTERN, false);

        instrumentedTestXAResource.assertSumMethodCallCount("prepare", 2);
        instrumentedTestXAResource.assertMethodNotCalled("rollback");
        // commit called twice, first TestXAResource commits,
        // the second enters the commit and immediately kills JVM
        instrumentedTestXAResource.assertMethodCalled("commit");

        rebootServer(controller);

        doRecovery();
        doRecovery();

        instrumentedTestXAResource.assertMethodNotCalled("prepare");
        instrumentedTestXAResource.assertMethodCalled("recover");
        instrumentedTestXAResource.assertMethodNotCalled("rollback");
        instrumentedTestXAResource.assertSumMethodCallCount("commit", 2);
    }

    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCrashTwoLogs(@ArquillianResource URL baseURL) throws Exception {

        InstrumentedClass durableParticipant = instrumentor.instrumentClass(BridgeDurableParticipant.class);

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$2 = true"); // shouldCommit=true
        instrumentor.crashAtMethodExit(BridgeDurableParticipant.class, "prepare");

        execute(baseURL + TestClient.URL_PATTERN, false);

        durableParticipant.assertMethodCalled("prepare");
        durableParticipant.assertMethodNotCalled("rollback");
        durableParticipant.assertMethodNotCalled("commit");

        instrumentedTestSynchronization.assertKnownInstances(1);
        instrumentedTestSynchronization.assertMethodCalled("beforeCompletion");
        instrumentedTestSynchronization.assertMethodNotCalled("afterCompletion");

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("prepare");
        instrumentedTestXAResource.assertMethodNotCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");

        rebootServer(controller);

        doRecovery();
        doRecovery();

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("recover");
        instrumentedTestXAResource.assertMethodCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");
    }

    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCrashThreeLogs(@ArquillianResource URL baseURL) throws Exception {

        InstrumentedClass durableParticipant = instrumentor.instrumentClass(BridgeDurableParticipant.class);

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$2 = true"); // shouldCommit=true
        instrumentor.crashAtMethodExit("^XTSATRecoveryManager", "writeParticipantRecoveryRecord");

        execute(baseURL + TestClient.URL_PATTERN, false);

        durableParticipant.assertMethodCalled("prepare");
        durableParticipant.assertMethodNotCalled("rollback");
        durableParticipant.assertMethodNotCalled("commit");

        instrumentedTestSynchronization.assertKnownInstances(1);
        instrumentedTestSynchronization.assertMethodCalled("beforeCompletion");
        instrumentedTestSynchronization.assertMethodNotCalled("afterCompletion");

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("prepare");
        instrumentedTestXAResource.assertMethodNotCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");

        rebootServer(controller);

        doRecovery();
        doRecovery();

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("recover");
        instrumentedTestXAResource.assertMethodCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");
    }

    /**
     * <p>
     * The test checks that the xts inbound bridge does not rollback a resource of an in-flight transaction.
     * </p>
     * <p>
     * The test is meant to run as
     * <ul>
     *  <li>the test calls the WS call, the call goes with the transaction context and the inbound bridge is involved</li>
     *  <li>the call then commits a transaction where participates the TestXAResource</li>
     *  <li>the TestXAResource is prepared and then the test makes the Durable Bridge participant waiting with help of Byteman</li>
     *  <li>the recovery is triggered and as there is in-flight transaction the inbound txbridge recovery manager must not abort it</li>
     *  <li>in the meantime there is running a participant completion task (com.arjuna.webservices11.wsat.sei.CoordinatorPortTypeImpl),
     *     This is run by TaskManager periodically and trying to finish the XTS participants.
     *     The first time the recovery let the task went through prepare phase and on the second time (when the recovery is run for the second time)
     *     the task makes the participant to be committed.
     *  </li>
     *  <li>the recovery itself does not commit but it preserve the participant not to be rolled-back</li>
     * </ul>
     * </p>
     */
    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testRecoveryLivingTransactions(@ArquillianResource URL baseURL) throws Exception {

        InstrumentedClass durableParticipant = instrumentor.instrumentClass(BridgeDurableParticipant.class);
        InstrumentedClass instrumentedTestXAResourceRecovered = instrumentor.instrumentClass(TestXAResourceRecovered.class);

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$2 = true"); // shouldCommit=true
        instrumentor.injectOnExit(BridgeDurableParticipant.class, "prepare", "waitFor(\"recoveryProcessed\")");

        Executors.newSingleThreadExecutor().submit(() ->
            executeWithRuntimeException(baseURL + TestClient.URL_PATTERN, false));

        instrumentor.injectOnExit(PeriodicRecovery.class, "doWorkInternal", "signalWake(\"recoveryProcessed\", flag(\"alreadyProcessed\"))");

        doRecovery();
        doRecovery();

        durableParticipant.assertMethodCalled("prepare");
        durableParticipant.assertMethodNotCalled("rollback");
        durableParticipant.assertMethodCalled("commit");

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("prepare");
        instrumentedTestXAResource.assertMethodNotCalled("rollback");
        instrumentedTestXAResourceRecovered.assertMethodCalled("commit");
        instrumentedTestXAResourceRecovered.assertMethodNotCalled("rollback");
    }

    /**
     * This is an alternative test to {@link #testRecoveryOfLiveTransaction(URL)}
     * which expect the recover does not roll-back but signal byteman waiting
     * at different location.
     */
    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testRecoveryOfLiveTransaction(final @ArquillianResource URL baseURL) throws Exception {
        InstrumentedClass durableParticipant = instrumentor.instrumentClass(BridgeDurableParticipant.class);
        InstrumentedClass instrumentedTestXAResourceRecovered = instrumentor.instrumentClass(TestXAResourceRecovered.class);

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$2 = true"); // shouldCommit=true
        instrumentor.injectOnCall(TestXAResource.class, "commit", "waitFor(\"recoveryProcessed\")");
        instrumentor.injectOnCall(TestXAResourceRecoveryHelper.class, "recover", "waitFor(\"preparedXid\")");

        instrumentor.injectOnExit(TestXAResource.class, "prepare", "signalWake(\"preparedXid\"), true");
        // This should create a transaction that has a SystemException in CompletionStub:74
        executeWithRuntimeException(baseURL + TestClient.URL_PATTERN, false);

        // Prevent recovery needing to see another prepare call
        instrumentor.injectOnCall(TestXAResourceRecoveryHelper.class, "recover", "signalWake(\"preparedXid\")");
        // Recovery should be leaving this alone as it is live
        log.info("requesting recovery for a live transaction");
        doRecovery();

        durableParticipant.assertMethodCalled("prepare");
        durableParticipant.assertMethodNotCalled("rollback");
        durableParticipant.assertMethodCalled("commit");

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("prepare");
        instrumentedTestXAResource.assertMethodNotCalled("rollback");
        instrumentedTestXAResource.assertMethodCalled("commit"); // The transaction should still be waiting for recoveryProcessed
        instrumentedTestXAResourceRecovered.assertMethodNotCalled("commit"); // We don't want recovery to have committed this Xid as it was live
        instrumentedTestXAResourceRecovered.assertMethodNotCalled("rollback");

        // Allow the original transaction to release for cleanup including removal of islive
        instrumentor.injectOnExit(PeriodicRecovery.class, "doWorkInternal", "signalWake(\"recoveryProcessed\", true)");
        doRecovery();
    }

    // TODO: add test for 4log case i.e. commit

}
