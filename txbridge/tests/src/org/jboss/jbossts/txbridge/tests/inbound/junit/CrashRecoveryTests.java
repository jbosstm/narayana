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

import org.jboss.jbossts.txbridge.inbound.BridgeDurableParticipant;
import org.jboss.jbossts.txbridge.tests.common.AbstractCrashRecoveryTests;
import org.jboss.jbossts.txbridge.tests.inbound.client.TestClient;
import org.jboss.jbossts.txbridge.tests.inbound.service.TestServiceImpl;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestSynchronization;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestXAResource;

import org.jboss.jbossts.txbridge.tests.inbound.utility.TestXAResourceRecovered;
import org.junit.*;

import org.jboss.byteman.contrib.dtest.*;


/**
 * Crash Recovery test cases for the inbound side of the transaction bridge.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-05
 */
public class CrashRecoveryTests extends AbstractCrashRecoveryTests
{
    private static final String baseURL = "http://localhost:8080/txbridge-inbound-tests-client/testclient";

    private InstrumentedClass instrumentedTestSynchronization;
    private InstrumentedClass instrumentedTestXAResource;
    

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        instrumentedTestSynchronization = instrumentor.instrumentClass(TestSynchronization.class);
        instrumentedTestXAResource = instrumentor.instrumentClass(TestXAResource.class);

        instrumentor.injectOnCall(TestServiceImpl.class, "doNothing", "$0.enlistSynchronization(1), $0.enlistXAResource(1)");
    }

    @Override
    protected void instrumentationOnServerReboot() throws Exception
    {
        instrumentedTestSynchronization = instrumentor.instrumentClass(TestSynchronization.class);
        instrumentedTestXAResource = instrumentor.instrumentClass(TestXAResourceRecovered.class);
    }

    @Test
    public void testCrashOneLog() throws Exception {

        instrumentor.injectOnCall(TestClient.class,  "terminateTransaction", "$2 = true"); // shouldCommit=true
        instrumentor.crashAtMethodExit(TestXAResource.class, "prepare");
        
        execute(baseURL, false);

        instrumentedTestSynchronization.assertKnownInstances(1);
        instrumentedTestSynchronization.assertMethodCalled("beforeCompletion");
        instrumentedTestSynchronization.assertMethodNotCalled("afterCompletion");

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("prepare");
        instrumentedTestXAResource.assertMethodNotCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");

        rebootServer();

        doRecovery();
        doRecovery();

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("recover");
        instrumentedTestXAResource.assertMethodCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");
    }

    @Test
    public void testCrashTwoLogs() throws Exception {

        InstrumentedClass durableParticipant = instrumentor.instrumentClass(BridgeDurableParticipant.class);

        instrumentor.injectOnCall(TestClient.class,  "terminateTransaction", "$2 = true"); // shouldCommit=true
        instrumentor.crashAtMethodExit(BridgeDurableParticipant.class, "prepare");

        execute(baseURL, false);

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

        rebootServer();

        doRecovery();
        doRecovery();

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("recover");
        instrumentedTestXAResource.assertMethodCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");
    }

    @Test
    public void testCrashThreeLogs() throws Exception {

        InstrumentedClass durableParticipant = instrumentor.instrumentClass(BridgeDurableParticipant.class);

        instrumentor.injectOnCall(TestClient.class,  "terminateTransaction", "$2 = true"); // shouldCommit=true
        instrumentor.crashAtMethodExit("^XTSATRecoveryManager", "writeParticipantRecoveryRecord");

        execute(baseURL, false);

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

        rebootServer();

        doRecovery();
        doRecovery();

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("recover");
        instrumentedTestXAResource.assertMethodCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");
    }

    // TODO: add test for 4log case i.e. commit

}
