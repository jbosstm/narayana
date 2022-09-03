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
import io.narayana.lra.arquillian.resource.SimpleLRAParticipant;
import io.narayana.lra.arquillian.spi.NarayanaLRARecovery;
import io.narayana.lra.coordinator.domain.model.FailedLongRunningAction;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static io.narayana.lra.arquillian.resource.SimpleLRAParticipant.RESET_ACCEPTED_PATH;
import static io.narayana.lra.arquillian.resource.SimpleLRAParticipant.SIMPLE_PARTICIPANT_RESOURCE_PATH;
import static  io.narayana.lra.LRAConstants.RECOVERY_COORDINATOR_PATH_NAME;
import static io.narayana.lra.arquillian.resource.SimpleLRAParticipant.START_LRA_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * There is a spec requirement to report failed LRAs but the spec only requires that a failure message is reported
 * (not how it is reported). Failure records are vital pieces of data needed to aid failure tracking and analysis.
 *
 * The Narayana implementation allows failed LRAs to be directly queried. The following tests validate that the
 * correct failure records are kept until explicitly removed.
 */
public class FailedLRAIT extends TestBase {
    private static final Logger log = Logger.getLogger(FailedLRAIT.class);

    @ArquillianResource
    public URL baseURL;

    @Rule
    public TestName testName = new TestName();

    @Override
    public void before() {
        super.before();
        log.info("Running test " + testName.getMethodName());
    }

    @Deployment
    public static WebArchive deploy() {
        return Deployer.deploy(FailedLRAIT.class.getSimpleName(),
                LRAParticipantWithStatusURI.class,
                LRAParticipantWithoutStatusURI.class,
                SimpleLRAParticipant.class);
    }

    static String getRecoveryUrl(URI lraId) {
        return LRAConstants.getLRACoordinatorUrl(lraId) + "/" + RECOVERY_COORDINATOR_PATH_NAME;
    }

    /**
     * test that a failure record is created when a participant (with status reporting) fails to compensate
     */
    @Test
    public void testWithStatusCompensateFailed() throws Exception {
        URI lraId = invokeInTransaction(LRAParticipantWithStatusURI.LRA_PARTICIPANT_PATH,
                LRAParticipantWithStatusURI.TRANSACTIONAL_CANCEL_PATH, 500);
        lrasToAfterFinish.add(lraId);

        if (!validateStateAndRemove(lraId, LRAStatus.FailedToCancel)) {
            fail("lra not in failed list");
        }
    }

    /**
     * test that a failure record is created when a participant (with status reporting) fails to complete
     */
    @Test
    public void testWithStatusCompleteFailed() throws Exception {
        // invoke a method that should run with an LRA
        URI lraId = invokeInTransaction(LRAParticipantWithStatusURI.LRA_PARTICIPANT_PATH,
                LRAParticipantWithStatusURI.TRANSACTIONAL_CLOSE_PATH, 200);
        lrasToAfterFinish.add(lraId);

        // when the invoked method returns validate that the narayana implementation created a failure record
        if (!validateStateAndRemove(lraId, LRAStatus.FailedToClose)) {
            fail("lra not in failed list");
        }
    }

    /**
     * test that a failure record is created when a participant (without status reporting) fails to compensate
     */
    @Test
    public void testCompensateFailed() throws Exception {
        URI lraId = invokeInTransaction(LRAParticipantWithoutStatusURI.LRA_PARTICIPANT_PATH,
                LRAParticipantWithoutStatusURI.TRANSACTIONAL_CANCEL_PATH, 500);
        lrasToAfterFinish.add(lraId);

        if (!validateStateAndRemove(lraId, LRAStatus.FailedToCancel)) {
            fail("lra not in failed list");
        }
    }

    /**
     * test that a failure record is created when a participant (without status reporting) fails to complete
     */
    @Test
    public void testCompleteFailed() throws Exception {
        URI lraId = invokeInTransaction(LRAParticipantWithoutStatusURI.LRA_PARTICIPANT_PATH,
                LRAParticipantWithoutStatusURI.TRANSACTIONAL_CLOSE_PATH, 200);
        lrasToAfterFinish.add(lraId);

        if (!validateStateAndRemove(lraId, LRAStatus.FailedToClose)) {
            fail("lra not in failed list");
        }
    }

    /**
     * test that only failed LRAs can be deleted via the recovery coordinator
     */
    @Test
    public void testCannotDeleteNonFailedLRAs() throws Exception {
        // start an LRA which will return 202 when asked to compensate
        URI lraId = invokeInTransaction(SIMPLE_PARTICIPANT_RESOURCE_PATH,
                START_LRA_PATH, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        lrasToAfterFinish.add(lraId);

        // verify that deleting the LRA log fails as it's with "Cancelling" status
        int status1 = removeFailedLRA(lraId);
        assertEquals("deleting a cancelling LRA should fail with error code 412", 412, status1);

        int status2 = removeFailedLRA(getRecoveryUrl(lraId), "Invalid URI Syntax");
        assertEquals("used an invalid (wrong URI syntax) LRA id and precondition failed is expected", 412, status2);

        int status3 = removeFailedLRA(getRecoveryUrl(lraId), "http://example.com");
        // there is a difference on handling this url format in different REST Easy versions
        assertThat("deleting an LRA in wrong format that JAX-RS understands as non-existent URL binding or as method not-allowed",
                status3, anyOf(equalTo(404), equalTo(405)));

        int status4 = removeFailedLRA(getRecoveryUrl(lraId), URLEncoder.encode("http://example.com", StandardCharsets.UTF_8.name()));
        assertEquals("using correct URI format but such that has empty path component and thus non-existent, " +
                "expecting internal server error", 500, status4);

        int status5 = removeFailedLRA(getRecoveryUrl(lraId), "a:b.c");
        assertEquals("using correct URI format but such that has no path component and thus non-existent, " +
                "expecting internal server error", 500, status5);

        // tell the participant compensate method to return an end state on the next compensate request
        invokeInTransaction(SIMPLE_PARTICIPANT_RESOURCE_PATH, RESET_ACCEPTED_PATH, 200);

        // wait for recovery to replay the compensation
        new NarayanaLRARecovery().waitForRecovery(lraId);

        // the participant should now be in a failed state so the record can be deleted
        int statusDeleted = removeFailedLRA(lraId);
        assertEquals("deleting a failed LRA", 204, statusDeleted);
    }

    /**
     * test that a created failure record is moved to failedLRAType location
     */
    @Test
    public void testToMoveFailedLRARecords() throws Exception {
        URI lraId = invokeInTransaction(LRAParticipantWithStatusURI.LRA_PARTICIPANT_PATH,
                LRAParticipantWithStatusURI.TRANSACTIONAL_CANCEL_PATH, 500);
        lrasToAfterFinish.add(lraId);
        // Checks if the record exists in the LRARecords location
        if (!validateFailedRecordMoved(lraId)) {
            fail("lra not in failed list location : " + FailedLongRunningAction.FAILED_LRA_TYPE);
        }
        // Removing Failed LRA record.
        removeFailedLRA(lraId);
    }

    /**
     * Validate if Failed LRA is moved to failed lraRecord's Location.
     *
     * @param lra the LRA whose state is to be validated
     * @return true if the Failed LRA record is moved to another location
     * @throws Exception if the state cannot be determined
     */
    private boolean validateFailedRecordMoved(URI lra) throws Exception {
        boolean isMoved = false;
        String failedLRAId = null;
        JsonArray failedRecords = getFailedRecords(lra);
        for (JsonValue failedLRA : failedRecords) {
            String lraId = failedLRA.asJsonObject().getString("lraId");
            lraId = lraId.replaceAll("\\\\", "");
            if (lraId.contains(lra.toASCIIString())) {
                failedLRAId = lraId;
            }
        }
        JsonArray allRecords = getAllRecords(lra);
        if (allRecords.isEmpty() && failedLRAId != null) {
            isMoved = true;
        } else {
            isMoved = true;
            for (JsonValue lraRec : allRecords) {
                String lraId = lraRec.asJsonObject().getString("lraId");
                lraId = lraId.replaceAll("\\\\", "");
                if (failedLRAId == null || lraId.contains(failedLRAId)) {
                    return false;
                }
            }
        }

        return isMoved;
    }

    private JsonArray getAllRecords(URI lra) {
        String coordinatorUrl = LRAConstants.getLRACoordinatorUrl(lra) + "/";

        try (Response response = client.target(coordinatorUrl).path("").request().get()) {
            Assert.assertTrue("Missing response body when querying for all LRAs", response.hasEntity());
            String allLRAs = response.readEntity(String.class);

            JsonReader jsonReader = Json.createReader(new StringReader(allLRAs));
            return jsonReader.readArray();
        }
    }

    private URI invokeInTransaction(String resourcePrefix, String resourcePath, int expectedStatus) {
        try(Response response = client.target(baseURL.toURI())
                .path(resourcePrefix)
                .path(resourcePath)
                .request()
                .get()) {

            Assert.assertTrue("Expecting a non empty body in response from " + resourcePrefix + "/" + resourcePath,
                    response.hasEntity());

            String entity = response.readEntity(String.class);

            Assert.assertEquals(
                    "response from " + resourcePrefix + "/" + resourcePath + " was " + entity,
                    expectedStatus, response.getStatus());

            return new URI(entity);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("response cannot be converted to URI: " + e.getMessage());
        }
    }

    /**
     * Validate whether a given LRA is in a particular state.
     * Also validate that the corresponding record is removed.
     *
     * @param lra the LRA whose state is to be validated
     * @param state the state that the target LRA should be in
     * @return true if the LRA is in the target state and that its log was successfully removed
     * @throws Exception if the state cannot be determined
     */
    private boolean validateStateAndRemove(URI lra, LRAStatus state) throws Exception {
        JsonArray failedRecords = getFailedRecords(lra);
        for (JsonValue failedLRA : failedRecords) {
            String lraId = failedLRA.asJsonObject().getString("lraId");
            lraId = lraId.replaceAll("\\\\", "");
            if (lraId.contains(lra.toASCIIString())) {
                String status = failedLRA.asJsonObject().getString("status");
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
    private JsonArray getFailedRecords(URI lra) {
        String recoveryUrl = getRecoveryUrl(lra);

        try (Response response = client.target(recoveryUrl).path("failed").request().get()){
            Assert.assertTrue("Missing response body when querying for failed LRAs", response.hasEntity());
            String failedLRAs = response.readEntity(String.class);

            JsonReader jsonReader = Json.createReader(new StringReader(failedLRAs));
            return jsonReader.readArray();
        }
    }

    // ask the recovery coordinator to delete its log for an LRA
    private int removeFailedLRA(URI lra) throws UnsupportedEncodingException {
        String recoveryUrl = getRecoveryUrl(lra);
        String txId = URLEncoder.encode(lra.toASCIIString(), "UTF-8");

        return removeFailedLRA(recoveryUrl, txId);
    }

    // ask the recovery coordinator to delete its log for an LRA
    private int removeFailedLRA(String recoveryUrl, String lra) {
        try (Response response = client.target(recoveryUrl).path(lra).request().delete()) {
            return response.getStatus();
        }
    }
}
