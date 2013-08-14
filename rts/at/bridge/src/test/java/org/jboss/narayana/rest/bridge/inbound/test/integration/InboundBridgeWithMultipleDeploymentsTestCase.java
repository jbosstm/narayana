/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.narayana.rest.bridge.inbound.test.integration;

import org.codehaus.jettison.json.JSONArray;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    public void testCommitWithTwoParticipants() throws Exception {
        txSupport.startTx();
        postRestATResource(FIRST_RESOURCE_URL);
        postRestATResource(SECOND_RESOURCE_URL);
        txSupport.commitTx();

        JSONArray firstLoggingXAResourceInvocations = getResourceInvocations(FIRST_RESOURCE_URL);
        JSONArray secondLoggingXAResourceInvocations = getResourceInvocations(SECOND_RESOURCE_URL);

        if (firstLoggingXAResourceInvocations.length() == 5) {
            Assert.assertEquals("LoggingXAResource.start", firstLoggingXAResourceInvocations.get(0));
            Assert.assertEquals("LoggingXAResource.isSameRM", firstLoggingXAResourceInvocations.get(1));
            Assert.assertEquals("LoggingXAResource.end", firstLoggingXAResourceInvocations.get(2));
            Assert.assertEquals("LoggingXAResource.prepare", firstLoggingXAResourceInvocations.get(3));
            Assert.assertEquals("LoggingXAResource.commit", firstLoggingXAResourceInvocations.get(4));

            Assert.assertEquals("LoggingXAResource.start", secondLoggingXAResourceInvocations.get(0));
            Assert.assertEquals("LoggingXAResource.end", secondLoggingXAResourceInvocations.get(1));
            Assert.assertEquals("LoggingXAResource.prepare", secondLoggingXAResourceInvocations.get(2));
            Assert.assertEquals("LoggingXAResource.commit", secondLoggingXAResourceInvocations.get(3));
        } else {
            Assert.assertEquals("LoggingXAResource.start", firstLoggingXAResourceInvocations.get(0));
            Assert.assertEquals("LoggingXAResource.end", firstLoggingXAResourceInvocations.get(1));
            Assert.assertEquals("LoggingXAResource.prepare", firstLoggingXAResourceInvocations.get(2));
            Assert.assertEquals("LoggingXAResource.commit", firstLoggingXAResourceInvocations.get(3));

            Assert.assertEquals("LoggingXAResource.start", secondLoggingXAResourceInvocations.get(0));
            Assert.assertEquals("LoggingXAResource.isSameRM", secondLoggingXAResourceInvocations.get(1));
            Assert.assertEquals("LoggingXAResource.end", secondLoggingXAResourceInvocations.get(2));
            Assert.assertEquals("LoggingXAResource.prepare", secondLoggingXAResourceInvocations.get(3));
            Assert.assertEquals("LoggingXAResource.commit", secondLoggingXAResourceInvocations.get(4));
        }
    }


    @Test
    public void testRollbackWithTwoParticipants() throws Exception {
        txSupport.startTx();
        postRestATResource(FIRST_RESOURCE_URL);
        postRestATResource(SECOND_RESOURCE_URL);
        txSupport.rollbackTx();

        JSONArray firstLoggingXAResourceInvocations = getResourceInvocations(FIRST_RESOURCE_URL);
        JSONArray secondLoggingXAResourceInvocations = getResourceInvocations(SECOND_RESOURCE_URL);

        if (firstLoggingXAResourceInvocations.length() == 4) {
            Assert.assertEquals("LoggingXAResource.start", firstLoggingXAResourceInvocations.get(0));
            Assert.assertEquals("LoggingXAResource.isSameRM", firstLoggingXAResourceInvocations.get(1));
            Assert.assertEquals("LoggingXAResource.end", firstLoggingXAResourceInvocations.get(2));
            Assert.assertEquals("LoggingXAResource.rollback", firstLoggingXAResourceInvocations.get(3));

            Assert.assertEquals("LoggingXAResource.start", secondLoggingXAResourceInvocations.get(0));
            Assert.assertEquals("LoggingXAResource.end", secondLoggingXAResourceInvocations.get(1));
            Assert.assertEquals("LoggingXAResource.rollback", secondLoggingXAResourceInvocations.get(2));
        } else {
            Assert.assertEquals("LoggingXAResource.start", firstLoggingXAResourceInvocations.get(0));
            Assert.assertEquals("LoggingXAResource.end", firstLoggingXAResourceInvocations.get(1));
            Assert.assertEquals("LoggingXAResource.rollback", firstLoggingXAResourceInvocations.get(2));

            Assert.assertEquals("LoggingXAResource.start", secondLoggingXAResourceInvocations.get(0));
            Assert.assertEquals("LoggingXAResource.isSameRM", secondLoggingXAResourceInvocations.get(1));
            Assert.assertEquals("LoggingXAResource.end", secondLoggingXAResourceInvocations.get(2));
            Assert.assertEquals("LoggingXAResource.rollback", secondLoggingXAResourceInvocations.get(3));
        }
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
