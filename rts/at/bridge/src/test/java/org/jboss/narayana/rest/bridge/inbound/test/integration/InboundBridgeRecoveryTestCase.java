/*
 * SPDX short identifier: Apache-2.0
 */

package org.jboss.narayana.rest.bridge.inbound.test.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.narayana.rest.bridge.inbound.test.common.LoggingRestATResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.LoggingXAResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.RestATManagementResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.SimpleInboundBridgeResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.ClientBuilder;
import java.io.StringReader;

/**
 *
 * @author Gytis Trikleris
 *
 */
@RunWith(Arquillian.class)
public class InboundBridgeRecoveryTestCase extends AbstractTestCase {

    private static final int RECOVERY_PERIOD = 2;

    private static final int RECOVERY_WAIT_CYCLES = 5;

    private static final String VM_ARGUMENTS = System.getProperty("server.jvm.args")
            + " -Dcom.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod=" + RECOVERY_PERIOD;

    private static final String BYTEMAN_ARGUMENTS = "-Dorg.jboss.byteman.verbose"
            + " -Djboss.modules.system.pkgs=org.jboss.byteman"
            + " -Dorg.jboss.byteman.transform.all"
            + " -javaagent:../lib/byteman.jar=script:scripts/@BMScript@.btm,listener:true";

    @Deployment(name = DEPLOYMENT_NAME, testable = false, managed = false)
    public static WebArchive createTestArchive() {
        return getEmptyWebArchive()
                .addClasses(RestATManagementResource.class, LoggingXAResource.class, SimpleInboundBridgeResource.class, LoggingRestATResource.class)
                .addAsWebInfResource("web.xml", "web.xml");
    }

    @After
    public void after() {
        super.after();
        stopContainer();
    }

    @Test
    public void testCrashAfterPrepareInParticipantResource() throws Exception {
        startContainer(VM_ARGUMENTS + " " + BYTEMAN_ARGUMENTS.replace("@BMScript@", "CrashAfterPrepareInParticipantResource"));
        txSupport.startTx();

        enlistRestATParticipant(LOGGING_REST_AT_RESOURCE_URL);
        postRestATResource(SIMPLE_INBOUND_BRIDGE_RESOURCE_URL);

        try {
            txSupport.commitTx();
            Assert.fail("Container was not killed.");
        } catch (HttpResponseException e) {
            // After crash participant won't return any response and exception will be thrown.
        }

        restartContainer(VM_ARGUMENTS);

        int cycles = 0;
        JsonArray partricipantsInformation;

        do {
            Thread.sleep(RECOVERY_PERIOD * 2000);
            partricipantsInformation = getParticipantsInformation();
        } while (cycles++ < RECOVERY_WAIT_CYCLES && partricipantsInformation.size() > 0);

        if (partricipantsInformation.size() > 0) {
            Assert.fail("Recovery failed");
        }
    }

    @Test
    public void testCrashBeforeCommit() throws Exception {
        startContainer(VM_ARGUMENTS + " " + BYTEMAN_ARGUMENTS.replace("@BMScript@", "CrashBeforeCommit"));
        txSupport.startTx();

        enlistRestATParticipant(LOGGING_REST_AT_RESOURCE_URL);
        postRestATResource(SIMPLE_INBOUND_BRIDGE_RESOURCE_URL);

        try {
            txSupport.commitTx();
            Assert.fail("Container was not killed.");
        } catch (HttpResponseException e) {
            // After crash participant won't return any response and exception will be thrown.
        }

        restartContainer(VM_ARGUMENTS);

        Object status;
        int cycles = 0;

        do {
            Thread.sleep(RECOVERY_PERIOD * 3000);
            status = null;
            try {
                // Updates coordinator's active transactions list
                txSupport.getTransactions();
                // After successful recovery transaction is removed and 404 is returned.
                status = txSupport.txStatus(); //getTransactionInfo().getStatus();
            } catch (HttpResponseException e) {
            }
        } while (status != null && cycles++ < RECOVERY_WAIT_CYCLES);

        if (status != null) {
            Assert.fail("Recovery failed");
        }
    }

    private JsonArray getParticipantsInformation() {
        try {
            final String response = ClientBuilder.newClient().target(DEPLOYMENT_URL + "/"
                    + RestATManagementResource.BASE_URL_SEGMENT + "/"
                    + RestATManagementResource.PARTICIPANTS_URL_SEGMENT).request().get(String.class);
            JsonReader reader = Json.createReader(new StringReader(response));
            return reader.readArray();
        } catch (Exception e) {
            e.printStackTrace();
            return Json.createArrayBuilder().build();
        }
    }

}