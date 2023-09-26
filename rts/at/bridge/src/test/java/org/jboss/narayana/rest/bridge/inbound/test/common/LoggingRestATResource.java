/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.rest.bridge.inbound.test.common;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.logging.Logger;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@Path(LoggingRestATResource.BASE_URL_SEGMENT)
public final class LoggingRestATResource {

    public static final String BASE_URL_SEGMENT = "logging-participant-resource";

    public static final String INVOCATIONS_URL_SEGMENT = "invocations";

    private static final Logger LOG = Logger.getLogger(LoggingRestATResource.class);

    private static final List<String> invocations = new ArrayList<>();

    /**
     * Returns links to the participant terminator.
     *
     * @return Link to the participant terminator.
     */
    @HEAD
    public Response headParticipant(@Context UriInfo uriInfo) {
        LOG.info("LoggingRestATResource.headParticipant()");

        invocations.add("LoggingRestATResource.headParticipant()");

        String serviceURL = uriInfo.getBaseUri() + uriInfo.getPath();
        String linkHeader = new TxSupport().makeTwoPhaseAwareParticipantLinkHeader(serviceURL, false, null, null);

        return Response.ok().header("Link", linkHeader).build();
    }

    /**
     * Returns current status of the participant.
     *
     * @return the status
     */
    @GET
    public Response getStatus() {
        LOG.info("LoggingRestATResource.getStatus()");

        invocations.add("LoggingRestATResource.getStatus()");

        return null;
    }

    /**
     * Terminates participant.
     *
     * @param content
     * @return the status
     */
    @PUT
    @Path(TxLinkNames.TERMINATOR)
    public Response terminateParticipant(String content) {
        LOG.info("LoggingRestATResource.terminateParticipant(" + content + ")");

        invocations.add("LoggingRestATResource.terminateParticipant(" + content + ")");

        TxStatus txStatus = TxSupport.toTxStatus(content);
        String responseStatus = null;

        if (txStatus.isPrepare()) {
            responseStatus = TxStatus.TransactionPrepared.name();

        } else if (txStatus.isCommit()) {
            responseStatus = TxStatus.TransactionCommitted.name();

        } else if (txStatus.isCommitOnePhase()) {
            responseStatus = TxStatus.TransactionCommittedOnePhase.name();

        } else if (txStatus.isAbort()) {
            responseStatus = TxStatus.TransactionRolledBack.name();
        }

        if (responseStatus == null) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        } else {
            return Response.ok(TxSupport.toStatusContent(responseStatus)).build();
        }
    }

    @GET
    @Path(INVOCATIONS_URL_SEGMENT)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getInvocations() {
        LOG.info("LoggingRestATResource.getInvocations()");

        return Json.createArrayBuilder(invocations).build();
    }

    @PUT
    @Path(INVOCATIONS_URL_SEGMENT)
    public Response resetInvocations() {
        LOG.info("LoggingRestATResource.resetInvocations()");

        invocations.clear();
        return Response.ok().build();
    }

}