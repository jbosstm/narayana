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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
@RunWith(Arquillian.class)
public class FailedLRAIT {
    private static final Logger log = Logger.getLogger(FailedLRAIT.class);

    @ArquillianResource
    private URL baseURL;

    private Client client;

    @Rule
    public TestName testName = new TestName();

    @Deployment
    public static WebArchive deploy() {
        return Deployer.deploy(FailedLRAIT.class.getSimpleName());
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

        // verify that deleting the LRA log fails as it's with "Cancelling" status
        int status1 = removeFailedLRA(lraId);
        assertEquals("deleting a cancelling LRA should fail with error code 412", 412, status1);

        int status2 = removeFailedLRA(getRecoveryUrl(lraId), "Invalid URI Syntax");
        assertEquals("used an invalid (wrong URI syntax) LRA id and precondition failed is expected", 412, status2);

        int status3 = removeFailedLRA(getRecoveryUrl(lraId), "http://example.com");
        // there is a difference on handling this url format in different REST Easy versions (ie. Thorntail returns 404, WFLY 21 returns 405)
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

    private URI invokeInTransaction(String resourcePrefix, String resourcePath, int expectedStatus) {
        Response response = null;

        try {
            response = client.target(baseURL.toURI())
                    .path(resourcePrefix)
                    .path(resourcePath)
                    .request()
                    .get();

            Assert.assertTrue("Expecting a non empty body in response from " + resourcePrefix + "/" + resourcePath,
                    response.hasEntity());

            return new URI(response.readEntity(String.class));
        } catch (URISyntaxException use) {
            throw new IllegalStateException("baseUrl '" + baseURL + "' cannot be converted to URI");
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
        String recoveryUrl = getRecoveryUrl(lra);

        try {
            response = client.target(recoveryUrl).path("failed").request().get();

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
        String recoveryUrl = getRecoveryUrl(lra);
        String txId = URLEncoder.encode(lra.toASCIIString(), "UTF-8");

        return removeFailedLRA(recoveryUrl, txId);
    }

    // ask the recovery coordinator to delete its log for an LRA
    private int removeFailedLRA(String recoveryUrl, String lra) {
        Response response = null;

        try {
            response = client.target(recoveryUrl).path(lra).request().delete();

            return response.getStatus();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
