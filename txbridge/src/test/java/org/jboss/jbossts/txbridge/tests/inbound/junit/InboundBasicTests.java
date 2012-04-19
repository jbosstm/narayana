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
import org.jboss.jbossts.txbridge.tests.common.AbstractBasicTests;
import org.jboss.jbossts.txbridge.tests.inbound.client.TestClient;
import org.jboss.jbossts.txbridge.tests.inbound.service.TestServiceImpl;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestSynchronization;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestXAResource;

import org.jboss.shrinkwrap.api.Archive;
import org.junit.*;

import org.jboss.byteman.contrib.dtest.*;
import org.junit.runner.RunWith;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.net.URL;

/**
 * Basic (i.e. non-crashrec) test cases for the inbound side of the transaction bridge.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-05
 * @author Ivo Studensky (istudens@redhat.com)
 */
@RunWith(Arquillian.class)
@RunAsClient
public class InboundBasicTests extends AbstractBasicTests {

    @Deployment(name = INBOUND_SERVICE_DEPLOYMENT_NAME, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> createServiceArchive() {
        return getInboundServiceArchive();
    }

    @Deployment(name = INBOUND_CLIENT_DEPLOYMENT_NAME, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> createClientArchive() {
        return getInboundClientArchive();
    }


    @ArquillianResource
    private ContainerController controller;

    private InstrumentedClass instrumentedTestSynchronization;
    private InstrumentedClass instrumentedTestXAResource;


    @Before
    public void setUp() throws Exception {
        // start up the appserver
        controller.start(CONTAINER);

        instrumentor.setRedirectedSubmissionsFile(null);

        instrumentedTestSynchronization = instrumentor.instrumentClass(TestSynchronization.class);
        instrumentedTestXAResource = instrumentor.instrumentClass(TestXAResource.class);

        instrumentor.injectOnCall(TestServiceImpl.class, "doNothing", "$0.enlistSynchronization(1), $0.enlistXAResource(1)");
    }

    @After
    public void tearDown() throws Exception {
        instrumentor.removeAllInstrumentation();

        // shut down the appserver
        controller.stop(CONTAINER);
    }


    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testRollback(@ArquillianResource URL baseURL) throws Exception {
        execute(baseURL.toString() + TestClient.URL_PATTERN);

        instrumentedTestSynchronization.assertKnownInstances(1);
        instrumentedTestSynchronization.assertMethodNotCalled("beforeCompletion");
        instrumentedTestSynchronization.assertMethodCalled("afterCompletion");

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodNotCalled("commit");
        instrumentedTestXAResource.assertMethodCalled("rollback");
    }

    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCommit(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$2 = true"); // shouldCommit=true

        execute(baseURL.toString() + TestClient.URL_PATTERN);

        instrumentedTestSynchronization.assertKnownInstances(1);
        instrumentedTestSynchronization.assertMethodCalled("beforeCompletion");
        instrumentedTestSynchronization.assertMethodCalled("afterCompletion");

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodNotCalled("rollback");
        instrumentedTestXAResource.assertMethodCalled("commit");
    }

    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testBeforeCompletionFailure(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectFault(TestSynchronization.class, "beforeCompletion", RuntimeException.class, new Object[]{"injected BeforeCompletion fault"});

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$2 = true"); // shouldCommit=true

        execute(baseURL.toString() + TestClient.URL_PATTERN);

        instrumentedTestSynchronization.assertKnownInstances(1);
        instrumentedTestSynchronization.assertMethodCalled("beforeCompletion");
        instrumentedTestSynchronization.assertMethodCalled("afterCompletion");

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");
    }

    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testPrepareReadonly(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectOnCall(TestXAResource.class, "prepare", "return " + XAResource.XA_RDONLY);

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$2 = true"); // shouldCommit=true

        execute(baseURL.toString() + TestClient.URL_PATTERN);

        instrumentedTestSynchronization.assertKnownInstances(1);
        instrumentedTestSynchronization.assertMethodCalled("beforeCompletion");
        instrumentedTestSynchronization.assertMethodCalled("afterCompletion");

        instrumentedTestXAResource.assertKnownInstances(1);
        instrumentedTestXAResource.assertMethodNotCalled("rollback");
        instrumentedTestXAResource.assertMethodNotCalled("commit");
    }

    @Test
    @OperateOnDeployment(INBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testPrepareFailure(@ArquillianResource URL baseURL) throws Exception {

        instrumentor.injectFault(TestXAResource.class, "prepare", XAException.class, new Object[]{XAException.XA_RBROLLBACK});

        instrumentor.injectOnCall(TestClient.class, "terminateTransaction", "$2 = true"); // shouldCommit=true

        execute(baseURL.toString() + TestClient.URL_PATTERN);

        instrumentedTestSynchronization.assertKnownInstances(1);
        instrumentedTestSynchronization.assertMethodCalled("beforeCompletion");
        instrumentedTestSynchronization.assertMethodCalled("afterCompletion");

        instrumentedTestXAResource.assertKnownInstances(1);

        //instrumentedTestXAResource.assertMethodNotCalled("rollback");
        // TODO hmm, XA_RBROLLBACK winds up on pending list, so is called at abortPhase2. bug?

        instrumentedTestXAResource.assertMethodNotCalled("commit");

    }
}