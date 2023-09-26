/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.rest.bridge.inbound.test.integration;

import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.rest.bridge.inbound.test.common.AdvancedInboundBridgeResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.LoggingRestATResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.LoggingXAResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.json.JsonArray;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class InboundBridgeWithMultipleDeploymentsTestCase extends AbstractTestCase {

    private static final String FIRST_DEPLOYMENT_NAME = "first-rest-tx-bridge-test";

    private static final String SECOND_DEPLOYMENT_NAME = "second-rest-tx-bridge-test";

    private static final String FIRST_DEPLOYMENT_URL = BASE_URL + "/" + FIRST_DEPLOYMENT_NAME;

    private static final String SECOND_DEPLOYMENT_URL = BASE_URL + "/" + SECOND_DEPLOYMENT_NAME;

    private static final String FIRST_RESOURCE_URL = FIRST_DEPLOYMENT_URL + "/"
            + AdvancedInboundBridgeResource.URL_SEGMENT;

    private static final String SECOND_RESOURCE_URL = SECOND_DEPLOYMENT_URL + "/"
            + AdvancedInboundBridgeResource.URL_SEGMENT;

    @Deployment(name = FIRST_DEPLOYMENT_NAME, testable = false, managed = false)
    public static WebArchive createFirstTestArchive() {
        return ShrinkWrap.create(WebArchive.class, FIRST_DEPLOYMENT_NAME + ".war")
                .addAsManifestResource(new StringAsset(ManifestMF), "MANIFEST.MF")
                .addClasses(AdvancedInboundBridgeResource.class, LoggingXAResource.class, LoggingRestATResource.class)
                .addAsWebInfResource("web.xml", "web.xml");
    }

    @Deployment(name = SECOND_DEPLOYMENT_NAME, testable = false, managed = false)
    public static WebArchive createSecondTestArchive() {
        return ShrinkWrap.create(WebArchive.class, SECOND_DEPLOYMENT_NAME + ".war")
                .addAsManifestResource(new StringAsset(ManifestMF), "MANIFEST.MF")
                .addClasses(AdvancedInboundBridgeResource.class, LoggingXAResource.class, LoggingRestATResource.class)
                .addAsWebInfResource("web.xml", "web.xml");
    }

    @Before
    public void before() {
        super.before();
        startContainer();
        resetResourceInvocations(FIRST_RESOURCE_URL);
        resetResourceInvocations(SECOND_RESOURCE_URL);
    }

    @After
    public void after() {
        super.after();
        stopContainer();
    }

    @Test
    public void testCommitWithTwoParticipants() {
        txSupport.startTx();
        postRestATResource(FIRST_RESOURCE_URL);
        postRestATResource(SECOND_RESOURCE_URL);
        txSupport.commitTx();

        JsonArray firstLoggingXAResourceInvocations = getResourceInvocations(FIRST_RESOURCE_URL);
        JsonArray secondLoggingXAResourceInvocations = getResourceInvocations(SECOND_RESOURCE_URL);

        assertJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.start", 1);
        assertJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.end", 1);
        assertJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.prepare", 1);
        assertJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.commit", 1);
        assertJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.rollback", 0);

        assertJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.start", 1);
        assertJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.end", 1);
        assertJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.prepare", 1);
        assertJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.commit", 1);
        assertJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.rollback", 0);

        boolean isSameRMCalled = containsJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.isSameRM", 1);
        isSameRMCalled = containsJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.isSameRM", 1) || isSameRMCalled;
        assertTrue("One of the XAResources had to be invoked with call XAResource.isSameRM "
                + "but no one was called with that", isSameRMCalled);
    }


    @Test
    public void testRollbackWithTwoParticipants() {
        txSupport.startTx();
        postRestATResource(FIRST_RESOURCE_URL);
        postRestATResource(SECOND_RESOURCE_URL);
        txSupport.rollbackTx();

        JsonArray firstLoggingXAResourceInvocations = getResourceInvocations(FIRST_RESOURCE_URL);
        JsonArray secondLoggingXAResourceInvocations = getResourceInvocations(SECOND_RESOURCE_URL);

        assertJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.start", 1);
        assertJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.end", 1);
        assertJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.prepare", 0);
        assertJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.commit", 0);
        assertJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.rollback", 1);

        assertJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.start", 1);
        assertJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.end", 1);
        assertJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.prepare", 0);
        assertJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.commit", 0);
        assertJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.rollback", 1);

        boolean isSameRMCalled = containsJsonArray(firstLoggingXAResourceInvocations, "LoggingXAResource.isSameRM", 1);
        isSameRMCalled = containsJsonArray(secondLoggingXAResourceInvocations, "LoggingXAResource.isSameRM", 1) || isSameRMCalled;
        assertTrue("One of the XAResources had to be invoked with call XAResource.isSameRM "
                + "but no one was called with that", isSameRMCalled);
    }

    @Override
    protected void startContainer(final String vmArguments) {
        if (!containerController.isStarted(CONTAINER_NAME)) {
            clearObjectStore();
            if (vmArguments == null) {
                containerController.start(CONTAINER_NAME);
            } else {
                final Config config = new Config();
                config.add("javaVmArguments", vmArguments);
                containerController.start(CONTAINER_NAME, config.map());
            }

            deployer.deploy(FIRST_DEPLOYMENT_NAME);
            deployer.deploy(SECOND_DEPLOYMENT_NAME);
        }
    }

    @Override
    protected void stopContainer() {
        deployer.undeploy(FIRST_DEPLOYMENT_NAME);
        deployer.undeploy(SECOND_DEPLOYMENT_NAME);
        containerController.stop(CONTAINER_NAME);
        containerController.kill(CONTAINER_NAME);
    }

}