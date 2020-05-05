/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
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

package io.narayana.lra.arquillian;

import io.narayana.lra.LRAConstants;
import io.narayana.lra.arquillian.resource.LRAParticipantWithStatusURI;
import io.narayana.lra.arquillian.resource.LRAParticipantWithoutStatusURI;
import io.narayana.lra.arquillian.spi.NarayanaLRARecovery;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.tck.service.spi.LRARecoveryService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static io.narayana.lra.arquillian.resource.SimpleLRAParticipant.RESET_ACCEPTED_PATH;
import static io.narayana.lra.arquillian.resource.SimpleLRAParticipant.START_LRA_PATH;
import static io.narayana.lra.arquillian.resource.SimpleLRAParticipant.SIMPLE_PARTICIPANT_RESOURCE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * There is a spec requirement to report failed LRAs but the spec only requires that a failure message is reported
 * (not how it is reported). Failure records are vital pieces of data needed to aid failure tracking and analysis.
 *
 * The Narayana implementation allows failed LRAs to be directly queried. The following tests validate that the
 * correct failure records are kept until explicitly removed.
 */
@RunWith(Arquillian.class)
public class FailedLRAIT {

    @ArquillianResource
    private URL baseURL;

    private Client client;

    @Deployment
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, FailedLRAIT.class.getSimpleName() + ".war")
                .addPackages(true,
                        org.codehaus.jettison.JSONSequenceTooLargeException.class.getPackage(),
                        LRARecoveryService.class.getPackage());
    }

    @Before
    public void before() {
        client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * test that a failure record is created when a participant (with status reporting) fails to compensate
     */
    @Test
    public void testWithStatusCompensateFailed() throws Exception {
        URI lra = invokeInTransaction(LRAParticipantWithStatusURI.LRA_PARTICIPANT_PATH,
                LRAParticipantWithStatusURI.TRANSACTIONAL_CANCEL_PATH, 500);

        if (!validateStateAndRemove(lra, LRAStatus.FailedToCancel)) {
            fail("lra not in failed list");
        }
    }

    /**
     * test that a failure record is created when a participant (with status reporting) fails to complete
     */
    @Test
    public void testWithStatusCompleteFailed() throws Exception {
        // invoke a method that should run with an LRA
        URI lra = invokeInTransaction(LRAParticipantWithStatusURI.LRA_PARTICIPANT_PATH,
                LRAParticipantWithStatusURI.TRANSACTIONAL_CLOSE_PATH, 200);

        // when the invoked method returns validate that the narayana implementation created a failure record
        if (!validateStateAndRemove(lra, LRAStatus.FailedToClose)) {
            fail("lra not in failed list");
        }
    }

    /**
     * test that a failure record is created when a participant (without status reporting) fails to compensate
     */
    @Test
    public void testCompensateFailed() throws Exception {
        URI lra = invokeInTransaction(LRAParticipantWithoutStatusURI.LRA_PARTICIPANT_PATH,
                LRAParticipantWithoutStatusURI.TRANSACTIONAL_CANCEL_PATH, 500);

        if (!validateStateAndRemove(lra, LRAStatus.FailedToCancel)) {
            fail("lra not in failed list");
        }
    }

    /**
     * test that a failure record is created when a participant (without status reporting) fails to complete
     */
    @Test
    public void testCompleteFailed() throws Exception {
        URI lra = invokeInTransaction(LRAParticipantWithoutStatusURI.LRA_PARTICIPANT_PATH,
                LRAParticipantWithoutStatusURI.TRANSACTIONAL_CLOSE_PATH, 200);

        if (!validateStateAndRemove(lra, LRAStatus.FailedToClose)) {
            fail("lra not in failed list");
        }
    }

    /**
     * test that only failed LRAs can be deleted via the recovery coordinator
     */
    @Test
    public void testCannotDeleteNonFailedLRAs() throws Exception {
        // start an LRA which will return 202 when asked to compensate
        URI lra = invokeInTransaction(SIMPLE_PARTICIPANT_RESOURCE_PATH,
                START_LRA_PATH, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        List<JSONObject> failedLRAs = getFailedRecords(lra);

        // there should be no failed LRAs yet
        assertEquals(0, failedLRAs.size());

        // the LRA should return 202 Accepted when asked to compensate

        // verify that deleting the LRA log fails
        int status1 = removeFailedLRA(lra); // lra is compensating so should fail
        int status2 = removeFailedLRA(lra.getHost(), lra.getPort(), "Invalid URI Syntax");
        int status3 = removeFailedLRA(lra.getHost(), lra.getPort(), "http://example.com");

        assertEquals("deleting a compensating LRA", 412, status1);
        assertEquals("deleting an invalid (wrong URI syntax) LRA", 412, status2);
        assertEquals("deleting a non existent LRA", 404, status3);

        // tell the participant compensate method to return an end state on the next compensate request
        invokeInTransaction(SIMPLE_PARTICIPANT_RESOURCE_PATH, RESET_ACCEPTED_PATH, 200);

        // wait for recovery to replay the compensation
        new NarayanaLRARecovery().waitForRecovery(lra);

        // the participant should now be in a failed state so the record can be deleted
        int status4 = removeFailedLRA(lra);
        assertEquals("deleting a failed LRA", 204, status4);
    }

    private URI invokeInTransaction(String resourcePrefix, String resourcePath, int expectedStatus) {
        Response response = null;

        try {
            response = client.target(UriBuilder.fromUri(baseURL.toExternalForm())
                    .path(resourcePrefix)
                    .path(resourcePath).build())
                    .request()
                    .get();

            assertEquals(expectedStatus, response.getStatus());
            Assert.assertTrue(response.hasEntity());

            return URI.create(response.readEntity(String.class));
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Validate whether or not a given LRA is in a particular state.
     * Also validate that the corresponding record is removed.
     *
     * @param lra the LRA whose state is to be validated
     * @param state the state that the target LRA should be in
     * @return true if the LRA is in the target state and that its log was successfully removed
     * @throws Exception if the state cannot be determined
     */
    private boolean validateStateAndRemove(URI lra, LRAStatus state) throws Exception {
        List<JSONObject> failedRecords = getFailedRecords(lra);
        for (JSONObject failedLRA : failedRecords) {
            String lraId = (String) failedLRA.get("lraId");
            lraId = lraId.replaceAll("\\\\", "");
            if (lraId.contains(lra.toASCIIString())) {
                String status = (String) failedLRA.get("status");
                if (status.equals(state.name())) {
                    // remove the failed LRA
                    Assert.assertEquals("Could not remove log",
                            Response.Status.NO_CONTENT.getStatusCode(),
                            removeFailedLRA(lra));
                    return true;
                }
            }
        }

        return false;
    }

    // look up LRAs that are in a failed state (ie FailedToCancel or FailedToClose)
    private List<JSONObject> getFailedRecords(URI lra) throws Exception {
        Response response = null;
        String recoveryUrl = String.format("http://%s:%d/%s",
                lra.getHost(), lra.getPort(), LRAConstants.RECOVERY_COORDINATOR_PATH_NAME);

        try {
            response = client.target(new URI(recoveryUrl)).path("failed").request().get();

            Assert.assertTrue("Missing response body when querying for failed LRAs", response.hasEntity());
            String failedLRAs = response.readEntity(String.class);

            JSONArray jsonArray = new JSONArray(failedLRAs);
            List<JSONObject> failedList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                failedList.add(new JSONObject(jsonArray.getString(i)));
            }

            return failedList;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    // ask the recovery coordinator to delete its log for an LRA
    private int removeFailedLRA(URI lra) throws URISyntaxException, UnsupportedEncodingException {
        String recoveryUrl = String.format("http://%s:%d/%s",
                lra.getHost(), lra.getPort(), LRAConstants.RECOVERY_COORDINATOR_PATH_NAME);
        String txId = URLEncoder.encode(lra.toASCIIString(), "UTF-8");

        return removeFailedLRA(recoveryUrl, txId);
    }

    // ask the recovery coordinator to delete its log for an LRA
    private int removeFailedLRA(String host, int port, String lra) throws URISyntaxException, UnsupportedEncodingException {
        String recoveryUrl = String.format("http://%s:%d/%s",
                host, port, LRAConstants.RECOVERY_COORDINATOR_PATH_NAME);

        return removeFailedLRA(recoveryUrl, lra);
    }

    // ask the recovery coordinator to delete its log for an LRA
    private int removeFailedLRA(String recoveryUrl, String lra) throws URISyntaxException {
        Response response = null;

        try {
            response = client.target(new URI(recoveryUrl)).path(lra).request().delete();

            return response.getStatus();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
