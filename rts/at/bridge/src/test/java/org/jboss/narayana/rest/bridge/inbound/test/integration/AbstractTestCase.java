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
import org.codehaus.jettison.json.JSONException;
import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.rest.bridge.inbound.test.common.AdvancedInboundBridgeResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.LoggingRestATResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public abstract class AbstractTestCase {

    protected static final String ManifestMF = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.jts, org.jboss.narayana.rts, org.jboss.logging, org.codehaus.jettison\n";

    protected static final String CONTAINER_NAME = "jboss";

    protected static final String BASE_URL = getBaseUrl();

    protected static final String TRANSACTION_MANAGER_URL = BASE_URL + "/" + "rest-at-coordinator/tx/transaction-manager";

    protected static final String DEPLOYMENT_NAME = "rest-tx-bridge-test";

    protected static final String DEPLOYMENT_URL = BASE_URL + "/" + DEPLOYMENT_NAME;

    protected static final String SIMPLE_INBOUND_BRIDGE_RESOURCE_URL = DEPLOYMENT_URL;

    protected static final String ADVANCED_INBOUND_BRIDGE_RESOURCE_URL = DEPLOYMENT_URL + "/"
            + AdvancedInboundBridgeResource.URL_SEGMENT;

    protected static final String LOGGING_REST_AT_RESOURCE_URL = DEPLOYMENT_URL + "/" + LoggingRestATResource.BASE_URL_SEGMENT;

    protected static final String LOGGING_REST_AT_RESOURCE_INVOCATIONS_URL = LOGGING_REST_AT_RESOURCE_URL + "/"
            + LoggingRestATResource.INVOCATIONS_URL_SEGMENT;

    protected TxSupport txSupport;

    @ArquillianResource
    protected ContainerController containerController;

    @ArquillianResource
    protected Deployer deployer;

    public static WebArchive getEmptyWebArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
                .addAsManifestResource(new StringAsset(ManifestMF), "MANIFEST.MF");

        return archive;
    }

    @Before
    public void before() {
        txSupport = new TxSupport(TRANSACTION_MANAGER_URL);
    }

    @After
    public void after() {
        try {
            txSupport.rollbackTx();
        } catch (Throwable t){
        }

        try {
            txSupport.txStatus(); //getTransactionInfo();
            Assert.fail("Failed to rollback the transaction.");
        } catch (HttpResponseException e) {
            // Expected if transaction was rolled back.
        } catch (IllegalStateException e) {
            // Expected if transaction didn't exist in a first place.
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected void startContainer() {
        startContainer(null);
    }

    protected void startContainer(final String vmArguments) {
        clearObjectStore();
        if (!containerController.isStarted(CONTAINER_NAME)) {
            if (vmArguments == null) {
                containerController.start(CONTAINER_NAME);
            } else {
                final Config config = new Config();
                config.add("javaVmArguments", vmArguments);
                containerController.start(CONTAINER_NAME, config.map());
            }

            deployer.deploy(DEPLOYMENT_NAME);
        }
    }

    protected void restartContainer(final String vmArguments) {
        if (vmArguments == null) {
            containerController.start(CONTAINER_NAME);
        } else {
            final Config config = new Config();
            config.add("javaVmArguments", vmArguments);
            containerController.start(CONTAINER_NAME, config.map());
        }
    }

    protected void stopContainer() {
        try {
            deployer.undeploy(DEPLOYMENT_NAME);
            containerController.stop(CONTAINER_NAME);
            containerController.kill(CONTAINER_NAME);
        } catch (final Throwable t) {
            // ignore
        } finally {
            clearObjectStore();
        }
    }

    protected void clearObjectStore() {
        final String jbossHome = System.getenv("JBOSS_HOME");
        if (jbossHome == null) {
            Assert.fail("$JBOSS_HOME not set");
        } else {
            final File objectStore = new File(jbossHome + File.separator + "standalone" + File.separator + "data" + File.separator
                    + "tx-object-store");

            if (objectStore.exists()) {
                if (!deleteDirectory(objectStore)) {
                    Assert.fail("Failed to remove tx-object-store: " + objectStore.getPath());
                }
            }
        }
    }

    protected void enlistRestATParticipant(final String resourceUrl) {
        final String linkHeader = txSupport.makeTwoPhaseAwareParticipantLinkHeader(resourceUrl, false, null, null);
        final String recoveryUrl = txSupport.enlistParticipant(txSupport.getTxnUri(), linkHeader);

        Assert.assertNotNull(recoveryUrl);
    }

    protected JSONArray getResourceInvocations(final String resourceUrl) {
        final String response = ClientBuilder.newClient().target(resourceUrl).request().get(String.class);
        final JSONArray jsonArray;

        try {
            jsonArray = new JSONArray(response);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonArray;
    }

    protected Response postRestATResource(final String resourceUrl) {
        final Link participantLink = Link.fromUri(txSupport.getTxnUri()).rel(TxLinkNames.PARTICIPANT)
                .title(TxLinkNames.PARTICIPANT).build();

        return ClientBuilder.newClient().target(resourceUrl).request().header("link", participantLink).post(null);
    }

    protected void resetResourceInvocations(final String resourceUrl) {
        final Response response = ClientBuilder.newClient().target(resourceUrl).request().put(null);
        org.junit.Assert.assertEquals(200, response.getStatus());
    }

    private static boolean deleteDirectory(final File path) {
        if (path.exists()) {
            final File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }

        return (path.delete());
    }

    private static String getBaseUrl() {
        String baseAddress = System.getProperty("jboss.bind.address");
        String basePort = System.getProperty("jboss.bind.port");

        if (baseAddress == null) {
            baseAddress = "http://localhost";
        } else if (!baseAddress.toLowerCase().startsWith("http://") && !baseAddress.toLowerCase().startsWith("https://")) {
            baseAddress = "http://" + baseAddress;
        }

        if (basePort == null) {
            basePort = "8080";
        }

        return baseAddress + ":" + basePort;
    }

}
