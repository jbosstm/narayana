package org.jboss.narayana.rest.integration.test.integration;

import org.codehaus.jettison.json.JSONArray;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionStatusElement;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.Vote;
import org.jboss.narayana.rest.integration.test.common.LoggingParticipant;
import org.jboss.narayana.rest.integration.test.common.RestATManagementResource;
import org.jboss.narayana.rest.integration.test.common.TransactionalService;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public final class RecoveryIntegrationTestCase extends AbstractIntegrationTestCase {

    private static final String DEPENDENCIES = "Dependencies: org.jboss.narayana.rts,org.jboss.jts,org.codehaus.jettison\n";

    private static final int RECOVERY_PERIOD = 2;

    private static final int RECOVERY_WAIT_CYCLES = 5;

    private static final String VM_ARGUMENTS = System.getProperty("server.jvm.args").trim()
            + " -Dcom.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod=" + RECOVERY_PERIOD;

    private static final String BYTEMAN_ARGUMENTS = "-Dorg.jboss.byteman.verbose -Djboss.modules.system.pkgs=org.jboss.byteman -Dorg.jboss.byteman.transform.all -javaagent:lib/byteman.jar=script:scripts/@BMScript@.btm,boot:lib/byteman.jar,listener:true";

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
        JSONArray partricipantsInformation;

        do {
            Thread.sleep(RECOVERY_PERIOD * 2000);
            partricipantsInformation = getParticipantsInformation();
        } while (cycles++ < RECOVERY_WAIT_CYCLES && partricipantsInformation.length() > 0);

        if (partricipantsInformation.length() > 0) {
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
            Assert.fail("Recovery failed");
        }
    }

    private void enlistParticipant(final String enlistmentUrl, final Vote vote) throws Exception {
        ClientResponse<String> clientResponse = new ClientRequest(DEPLOYMENT_URL + "/" + TransactionalService.PATH_SEGMENT)
                .queryParameter("participantEnlistmentUrl", enlistmentUrl).queryParameter("vote", vote.getClass().getName())
                .post(String.class);

        Assert.assertEquals(200, clientResponse.getStatus());
    }

    private void registerDeserializer() throws Exception {
        ClientResponse<String> clientResponse = new ClientRequest(DEPLOYMENT_URL + "/" + TransactionalService.PATH_SEGMENT)
                .put(String.class);

        Assert.assertEquals(204, clientResponse.getStatus());
    }

    private JSONArray getParticipantsInformation() {
        try {
            final ClientResponse<String> response = new ClientRequest(DEPLOYMENT_URL + "/"
                    + RestATManagementResource.BASE_URL_SEGMENT + "/"
                    + RestATManagementResource.PARTICIPANTS_URL_SEGMENT).get(String.class);
            return new JSONArray(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

}
