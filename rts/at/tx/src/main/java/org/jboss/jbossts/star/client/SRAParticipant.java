/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client;

import org.jboss.jbossts.star.annotation.Commit;
import org.jboss.jbossts.star.annotation.Participant;
import org.jboss.jbossts.star.annotation.Prepare;
import org.jboss.jbossts.star.annotation.Rollback;
import org.jboss.jbossts.star.annotation.Status;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.jbossts.star.client.SRAClient.RTS_HTTP_CONTEXT_HEADER;

@RequestScoped
public abstract class SRAParticipant {
    @Context
    private UriInfo context;

    @Context
    private HttpServletRequest httpRequest;

    private Map<String, SRAStatus> sraParticipantStatusMap;

    protected abstract SRAStatus updateParticipantState(SRAStatus status, String activityId);

    protected String getCompensatorData(String activityId) {
        return null;
    }
    /**
     * Get the SRA context of the currently running method.
     * Note that @HeaderParam(SRA_HTTP_HEADER) does not match the header (done't know why) so we the httpRequest
     *
     * @return the SRA context of the currently running method
     */
    protected String getCurrentActivityId() {
        return httpRequest.getHeader(RTS_HTTP_CONTEXT_HEADER);
    }

    @HEAD
    @Path("/participant")
    @Produces(MediaType.APPLICATION_JSON)
    @Participant
    public Response getTerminator() {
        String uriPrefix = httpRequest.getRequestURI();
        StringBuilder linkHeaderValue = new StringBuilder();

        makeLink(linkHeaderValue, uriPrefix, SRAClient.COMMIT, "/commit");
        makeLink(linkHeaderValue, uriPrefix, SRAClient.PREPARE, "/prepare");
        makeLink(linkHeaderValue, uriPrefix, SRAClient.ROLLBACK, "/rollback");

        return Response.ok().header("Link", linkHeaderValue.toString()).entity(linkHeaderValue.toString()).build();
    }

    private StringBuilder makeLink(StringBuilder b, String uriPrefix, String key, String value) {

        String terminationUri = uriPrefix == null ? value : String.format("%s%s", uriPrefix, value);
        Link link =  Link.fromUri(terminationUri).title(key + " URI").rel(key).type(MediaType.TEXT_PLAIN).build();

        if (b.length() != 0)
            b.append(',');

        return b.append(link);
    }

    @PUT
    @Path("/commit/{txid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Commit
    public Response commitWork(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) String atId,
                               @PathParam("txid")String sraId) throws NotFoundException {
        return updateState(SRAStatus.TransactionCommitted, sraId);//getCurrentActivityId());
    }

    @PUT
    @Path("/prepare/{txid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Prepare
    public Response prepareWork(@PathParam("txid")String sraId) throws NotFoundException {
        return updateState(SRAStatus.TransactionPrepared, sraId);//getCurrentActivityId());
    }

    @PUT
    @Path("/rollback/{txid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Rollback
    public Response rollbackWork(@PathParam("txid")String sraId) throws NotFoundException {
        return updateState(SRAStatus.TransactionRolledBack, sraId);//getCurrentActivityId());
    }

    @GET
    @Path("/status/{txid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Status
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public Response status(@PathParam("txid")String sraId) throws NotFoundException {
//        String sraId = getCurrentActivityId();

        if (sraId == null)
            throw new InvalidSRAId("null", "not present on SRAParticipant#status request", null);

        if (!sraParticipantStatusMap.containsKey(sraId))
            throw new InvalidSRAId(sraId, "SRAParticipant#status request: unknown sra id", null);

        // return status ok together with optional completion data or one of the other codes with a url that
        // returns

        /*
         * the compensator will either return a 200 OK code (together with optional completion data) or a URL which
         * indicates the outcome. That URL can be probed (via GET) and will simply return the same (implicit) information:
         *
         * <URL>/cannot-compensate
         * <URL>/cannot-complete
         *
         * TODO I am returning the status url instead. And if the status is compensated or completed then performing
         * GET on it will return 200 OK together with a compensator specific string that the business operation can
         * reason about, otherwise some other suitable status code is returned together with one of he valid
         * compensator states.
         */
        return updateState(sraParticipantStatusMap.get(sraId), sraId);
    }

    @PostConstruct
    public void postConstruct() {
        sraParticipantStatusMap = new HashMap<>();
    }

    /**
     * If the compensator was successful return a 200 status code and optionally an application specific string
     * that can be used by whoever closed the SRA (that triggered this compensator).
     * <p>
     * Otherwise return a status url that can be probed to obtain the final outcome when it is ready
     *
     * @param status
     * @param activityId
     * @return
     */
    private Response updateState(SRAStatus status, String activityId) {

        SRAStatus newStatus = updateParticipantState(status, activityId);

        sraParticipantStatusMap.put(activityId, newStatus); // NB in the demo we never remove completed activities

        switch (newStatus) {
            case TransactionCommitted:
            case TransactionRolledBack:
                String data = getCompensatorData(activityId);

                return data == null ? Response.ok().build() : Response.ok(data).build();

            case TransactionPrepared:
                return Response.status(Response.Status.OK).build();

            default:
                String statusUrl = getStatusUrl(activityId);

                return Response.status(Response.Status.ACCEPTED).entity(Entity.text(statusUrl)).build();
        }
    }

    private String getStatusUrl(String sraId) {
        return String.format("%s/%s/activity/status", context.getBaseUri(), SRAClient.getSRAId(sraId));
    }
}