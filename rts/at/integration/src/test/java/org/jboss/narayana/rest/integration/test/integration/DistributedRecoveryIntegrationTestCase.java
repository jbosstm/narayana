package org.jboss.narayana.rest.integration.test.integration;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.util.TxStatusMediaType;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionStatusElement;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.Vote;
import org.jboss.narayana.rest.integration.test.common.LoggingParticipant;
import org.jboss.narayana.rest.integration.test.common.RestATManagementResource;
import org.jboss.narayana.rest.integration.test.common.TestParticipantDeserializer;
import org.jboss.narayana.rest.integration.test.common.TransactionalService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.File;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class DistributedRecoveryIntegrationTestCase extends AbstractIntegrationTestCase {

    private static final String FIRST_BASE_URL = "http://" + System.getProperty("node.address") + ":8080";

    private static final String SECOND_BASE_URL = "http://" + System.getProperty("node.address") + ":8180";

    private static final String FIRST_DEPLOYMENT_NAME = "test";

    private static final String SECOND_DEPLOYMENT_NAME = "test2";

    protected static final String FIRST_DEPLOYMENT_URL = FIRST_BASE_URL + "/" + FIRST_DEPLOYMENT_NAME;

    protected static final String SECOND_DEPLOYMENT_URL = SECOND_BASE_URL + "/" + SECOND_DEPLOYMENT_NAME;

    protected static final String TRANSACTION_MANAGER_URL = FIRST_BASE_URL + "/rest-at-coordinator/tx/transaction-manager";

    private static final String DEPENDENCIES = "Dependencies: org.jboss.narayana.rts,org.codehaus.jettison\n";

    private static final String FIRST_CONTAINER_NAME = "jboss";

    private static final String SECOND_CONTAINER_NAME = "jboss2";

    protected static final TxSupport txSupport = new TxSupport(TRANSACTION_MANAGER_URL);

    private static final int RECOVERY_PERIOD = 4;

    private static final int RECOVERY_WAIT_CYCLES = 8;

    private static final String FIRST_CONTAINER_VM_ARGUMENTS = System.getProperty("server.jvm.args").trim()
            + " -Dcom.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod=" + RECOVERY_PERIOD;

    private static final String SECOND_CONTAINER_VM_ARGUMENTS = System.getProperty("server2.jvm.args").trim()
            + " -Dcom.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod=" + RECOVERY_PERIOD;

    private static final String BYTEMAN_ARGUMENTS = "-Dorg.jboss.byteman.verbose -Djboss.modules.system.pkgs=org.jboss.byteman -Dorg.jboss.byteman.transform.all -javaagent:lib/byteman.jar=script:scripts/@BMScript@.btm,boot:lib/byteman.jar,listener:true";

    @Deployment(name = FIRST_DEPLOYMENT_NAME, managed = false, testable = true)
    @TargetsContainer(FIRST_CONTAINER_NAME)
    public static WebArchive getFirstDeployment() {
        return ShrinkWrap.create(WebArchive.class, FIRST_DEPLOYMENT_NAME + ".war")
                .addClass(LoggingParticipant.class)
                .addClass(TestParticipantDeserializer.class)
                .addClass(TransactionalService.class)
                .addAsWebInfResource(new File("web.xml"), "web.xml")
                .addAsManifestResource(new StringAsset(DEPENDENCIES), "MANIFEST.MF");
    }

    @Deployment(name = SECOND_DEPLOYMENT_NAME, managed = false, testable = true)
    @TargetsContainer(SECOND_CONTAINER_NAME)
    public static WebArchive getSecondDeployment() {
        return ShrinkWrap.create(WebArchive.class, SECOND_DEPLOYMENT_NAME + ".war")
                .addClass(LoggingParticipant.class)
                .addClass(TestParticipantDeserializer.class)
                .addClass(TransactionalService.class)
                .addClass(RestATManagementResource.class)
                .addAsWebInfResource(new File("web.xml"), "web.xml")
                .addAsManifestResource(new StringAsset(DEPENDENCIES), "MANIFEST.MF");
    }

    @After
    public void after() {
        try {
            txSupport.rollbackTx();
        } catch (Throwable t) {
            // pass
        }

        try {
            stopContainer(FIRST_CONTAINER_NAME, FIRST_DEPLOYMENT_NAME);
        } catch (Throwable t) {
            // pass
        }

        try {
            stopContainer(SECOND_CONTAINER_NAME, SECOND_DEPLOYMENT_NAME);
        } catch (Throwable t) {
            // pass
        }
    }

    @Test
    public void testSecondServerFailureBeforeCommit() throws JAXBException {
        startContainer(FIRST_CONTAINER_NAME, FIRST_DEPLOYMENT_NAME, FIRST_CONTAINER_VM_ARGUMENTS);
        startContainer(SECOND_CONTAINER_NAME, SECOND_DEPLOYMENT_NAME,
                SECOND_CONTAINER_VM_ARGUMENTS + " " + BYTEMAN_ARGUMENTS.replace("@BMScript@", "CrashBeforeCommit"));

        txSupport.startTx();
        enlistParticipant(txSupport.getDurableParticipantEnlistmentURI(), FIRST_DEPLOYMENT_URL, new Prepared());
        enlistParticipant(txSupport.getDurableParticipantEnlistmentURI(), SECOND_DEPLOYMENT_URL, new Prepared());

        try {
            Assert.assertEquals(2, txSupport.getTransactionInfo().getTwoPhaseAware().size());
        } catch (final JAXBException e) {
            Assert.fail(e.getMessage());
        }

        final String result = txSupport.commitTx();
        Assert.assertEquals(TxStatusMediaType.TX_COMMITTED, result);

        restartContainer(SECOND_CONTAINER_NAME, SECOND_CONTAINER_VM_ARGUMENTS);
        Assert.assertEquals(0, getParticipantsInformation(SECOND_DEPLOYMENT_URL).length());
        registerDeserializer(SECOND_DEPLOYMENT_URL);
        Assert.assertEquals(1, getParticipantsInformation(SECOND_DEPLOYMENT_URL).length());

        int cycles = 0;
        TransactionStatusElement status = txSupport.getTransactionInfo().getStatus();
        Assert.assertEquals(TransactionStatusElement.TransactionCommitted, status);

        do {
            try {
                Thread.sleep(RECOVERY_PERIOD + 2000);
                // Updates coordinator's active transactions list
                txSupport.getTransactions();
                // After successful recovery transaction is removed and 404 is returned.
                status = txSupport.getTransactionInfo().getStatus();
            } catch (final Throwable t) {
                // ignore
            }
        } while (status != null && cycles++ < RECOVERY_WAIT_CYCLES);

        if (status == null) {
            Assert.fail("Recovery failed");
        }
    }

    private void enlistParticipant(final String enlistmentUrl, final String deploymentUrl, final Vote vote) {
        final Response response = ClientBuilder.newClient()
                .target(deploymentUrl + "/" + TransactionalService.PATH_SEGMENT)
                .queryParam("participantEnlistmentUrl", enlistmentUrl)
                .queryParam("vote", vote.getClass().getName())
                .request().post(null);

        Assert.assertEquals(200, response.getStatus());
    }

    private void registerDeserializer(final String deploymentUrl) {
        final Response response = ClientBuilder.newClient()
                .target(deploymentUrl + "/" + TransactionalService.PATH_SEGMENT)
                .request().put(null);

        Assert.assertEquals(204, response.getStatus());
    }

    private JSONArray getParticipantsInformation(final String deploymentUrl) {
        final String response = ClientBuilder.newClient()
                .target(deploymentUrl + "/" + RestATManagementResource.BASE_URL_SEGMENT + "/"
                        + RestATManagementResource.PARTICIPANTS_URL_SEGMENT)
                .request().get(String.class);

        try {
            return new JSONArray(response);
        } catch (final JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

}
