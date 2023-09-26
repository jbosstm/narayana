/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.rest.bridge.inbound.test.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.util.TxStatusMediaType;
import org.jboss.narayana.rest.bridge.inbound.test.common.AdvancedInboundBridgeResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.LoggingRestATResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.LoggingXAResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.json.JsonArray;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@RunWith(Arquillian.class)
public class InboundBridgeTestCase extends AbstractTestCase {

    @Deployment(name = DEPLOYMENT_NAME, testable = false, managed = false)
    public static WebArchive createDeployment() {
        return getEmptyWebArchive()
                .addClasses(AdvancedInboundBridgeResource.class, LoggingXAResource.class, LoggingRestATResource.class)
                .addAsWebInfResource("web.xml", "web.xml");
    }

    @Before
    public void before() {
        super.before();
        startContainer();
        resetResourceInvocations(ADVANCED_INBOUND_BRIDGE_RESOURCE_URL);
        resetResourceInvocations(LOGGING_REST_AT_RESOURCE_INVOCATIONS_URL);
    }

    @After
    public void after() {
        super.after();
        stopContainer();
    }

    @Test
    public void testCommit() {
        txSupport.startTx();
        postRestATResource(ADVANCED_INBOUND_BRIDGE_RESOURCE_URL);
        txSupport.commitTx();

        JsonArray JsonArray = getResourceInvocations(ADVANCED_INBOUND_BRIDGE_RESOURCE_URL);

        assertJsonArray(JsonArray, "LoggingXAResource.start", 1);
        assertJsonArray(JsonArray, "LoggingXAResource.end", 1);
        assertJsonArray(JsonArray, "LoggingXAResource.prepare", 1);
        assertJsonArray(JsonArray, "LoggingXAResource.commit", 1);
        assertJsonArray(JsonArray, "LoggingXAResource.rollback", 0);
    }

    @Test
    public void testRollback() {
        txSupport.startTx();
        postRestATResource(ADVANCED_INBOUND_BRIDGE_RESOURCE_URL);
        txSupport.rollbackTx();

        JsonArray JsonArray = getResourceInvocations(ADVANCED_INBOUND_BRIDGE_RESOURCE_URL);

        assertJsonArray(JsonArray, "LoggingXAResource.start", 1);
        assertJsonArray(JsonArray, "LoggingXAResource.end", 1);
        assertJsonArray(JsonArray, "LoggingXAResource.prepare", 0);
        assertJsonArray(JsonArray, "LoggingXAResource.commit", 0);
        assertJsonArray(JsonArray, "LoggingXAResource.rollback", 1);
    }

    @Test
    public void testCommitWithTwoParticipants() {
        txSupport.startTx();
        enlistRestATParticipant(LOGGING_REST_AT_RESOURCE_URL);
        postRestATResource(ADVANCED_INBOUND_BRIDGE_RESOURCE_URL);
        txSupport.commitTx();

        JsonArray loggingRestATResourceInvocations = getResourceInvocations(LOGGING_REST_AT_RESOURCE_INVOCATIONS_URL);
        JsonArray loggingXAResourceInvocations = getResourceInvocations(ADVANCED_INBOUND_BRIDGE_RESOURCE_URL);

        Assert.assertEquals(2, loggingRestATResourceInvocations.size());
        Assert.assertEquals("LoggingRestATResource.terminateParticipant(" + TxStatusMediaType.TX_PREPARED + ")",
                loggingRestATResourceInvocations.get(0).toString().replaceAll("^\"(.*)\"$", "$1"));
        Assert.assertEquals("LoggingRestATResource.terminateParticipant(" + TxStatusMediaType.TX_COMMITTED + ")",
                loggingRestATResourceInvocations.get(1).toString().replaceAll("^\"(.*)\"$", "$1"));

        assertJsonArray(loggingXAResourceInvocations, "LoggingXAResource.start", 1);
        assertJsonArray(loggingXAResourceInvocations, "LoggingXAResource.end", 1);
        assertJsonArray(loggingXAResourceInvocations, "LoggingXAResource.prepare", 1);
        assertJsonArray(loggingXAResourceInvocations, "LoggingXAResource.commit", 1);
        assertJsonArray(loggingXAResourceInvocations, "LoggingXAResource.rollback", 0);
    }

    @Test
    public void testRollbackWithTwoParticipants() {
        txSupport.startTx();
        enlistRestATParticipant(LOGGING_REST_AT_RESOURCE_URL);
        postRestATResource(ADVANCED_INBOUND_BRIDGE_RESOURCE_URL);
        txSupport.rollbackTx();

        JsonArray loggingRestATResourceInvocations = getResourceInvocations(LOGGING_REST_AT_RESOURCE_INVOCATIONS_URL);
        JsonArray loggingXAResourceInvocations = getResourceInvocations(ADVANCED_INBOUND_BRIDGE_RESOURCE_URL);

        Assert.assertEquals(1, loggingRestATResourceInvocations.size());
        Assert.assertEquals("LoggingRestATResource.terminateParticipant(" + TxStatusMediaType.TX_ROLLEDBACK + ")",
                loggingRestATResourceInvocations.get(0).toString().replaceAll("^\"(.*)\"$", "$1"));

        assertJsonArray(loggingXAResourceInvocations, "LoggingXAResource.start", 1);
        assertJsonArray(loggingXAResourceInvocations, "LoggingXAResource.end", 1);
        assertJsonArray(loggingXAResourceInvocations, "LoggingXAResource.prepare", 0);
        assertJsonArray(loggingXAResourceInvocations, "LoggingXAResource.commit", 0);
        assertJsonArray(loggingXAResourceInvocations, "LoggingXAResource.rollback", 1);
    }
}