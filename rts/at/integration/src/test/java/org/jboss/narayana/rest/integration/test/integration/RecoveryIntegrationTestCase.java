/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.rest.integration.test.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionStatusElement;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.Vote;
import org.jboss.narayana.rest.integration.test.common.LoggingParticipant;
import org.jboss.narayana.rest.integration.test.common.RestATManagementResource;
import org.jboss.narayana.rest.integration.test.common.TransactionalService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonString;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import java.io.File;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public final class RecoveryIntegrationTestCase extends AbstractIntegrationTestCase {

    private static final String DEPENDENCIES = "Dependencies: org.jboss.narayana.rts,org.jboss.jts\n";

    private static final int RECOVERY_PERIOD = 4;

    private static final int RECOVERY_WAIT_CYCLES = 8;

    private static final String VM_ARGUMENTS = System.getProperty("server.jvm.args").trim()
            + " -Dcom.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod=" + RECOVERY_PERIOD;

    private static final String BYTEMAN_ARGUMENTS = "-Dorg.jboss.byteman.verbose -Djboss.modules.system.pkgs=org.jboss.byteman -Dorg.jboss.byteman.transform.all -javaagent:lib/byteman.jar=script:scripts/@BMScript@.btm,listener:true";

    @Deployment(name = DEPLOYMENT_NAME, managed = false, testable = false)
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
                .addPackages(false, LoggingParticipant.class.getPackage()).addAsWebInfResource(new File("web.xml"), "web.xml")
                .addAsManifestResource(new StringAsset(DEPENDENCIES), "MANIFEST.MF");
    }

    @After
    public void after() {
        super.after();
        stopContainer();
    }

    @Test
    public void testCrashAfterPrepare() throws Exception {
        startContainer(VM_ARGUMENTS + " " + BYTEMAN_ARGUMENTS.replace("@BMScript@", "CrashAfterPrepare"));

        txSupport.startTx();

        enlistParticipant(txSupport.getDurableParticipantEnlistmentURI(), new Prepared());
        enlistParticipant(txSupport.getDurableParticipantEnlistmentURI(), new Prepared());

        Assert.assertEquals(2, txSupport.getTransactionInfo().getTwoPhaseAware().size());

        try {
            // JVM is killed here.
            txSupport.commitTx();
        } catch (HttpResponseException e) {
        }

        restartContainer(VM_ARGUMENTS);
        registerDeserializer();

        TransactionStatusElement status;
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

        enlistParticipant(txSupport.getDurableParticipantEnlistmentURI(), new Prepared());
        enlistParticipant(txSupport.getDurableParticipantEnlistmentURI(), new Prepared());

        Assert.assertEquals(2, txSupport.getTransactionInfo().getTwoPhaseAware().size());

        try {
            // JVM is killed here.
            txSupport.commitTx();
        } catch (HttpResponseException e) {
        }

        restartContainer(VM_ARGUMENTS);
        registerDeserializer();

        TransactionStatusElement status;
        int cycles = 0;

        do {
            Thread.sleep(RECOVERY_PERIOD * 2000);
            status = null;
            try {
                // Updates coordinator's active transactions list
                txSupport.getTransactions();
                // After successful recovery transaction is removed and 404 is returned.
                status = txSupport.getTransactionInfo().getStatus();
            } catch (HttpResponseException e) {
            }
        } while (status != null && cycles++ < RECOVERY_WAIT_CYCLES);

        if (status != null) {
            Assert.fail("Recovery failed unexpected status " + status);
        }
    }

    private void enlistParticipant(final String enlistmentUrl, final Vote vote) throws Exception {
        Response clientResponse = ClientBuilder.newClient().target(DEPLOYMENT_URL + "/" + TransactionalService.PATH_SEGMENT)
                .queryParam("participantEnlistmentUrl", enlistmentUrl).queryParam("vote", vote.getClass().getName()).request()
                .post(null);

        Assert.assertEquals(200, clientResponse.getStatus());
    }

    private void registerDeserializer() throws Exception {
        Response clientResponse = ClientBuilder.newClient().target(DEPLOYMENT_URL + "/" + TransactionalService.PATH_SEGMENT)
                .request().put(null);

        Assert.assertEquals(204, clientResponse.getStatus());
    }

    private JsonArray getParticipantsInformation() {
        try {
            final Response response = ClientBuilder.newClient().target(DEPLOYMENT_URL + "/"
                    + RestATManagementResource.BASE_URL_SEGMENT + "/"
                    + RestATManagementResource.PARTICIPANTS_URL_SEGMENT).request().get();
            JsonString jsonString = response.readEntity(JsonString.class);
            return Json.createArrayBuilder().add(jsonString).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Json.createArrayBuilder().build();
        }
    }

}
