/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package io.narayana.sra.client;

import io.narayana.sra.annotation.Commit;
import io.narayana.sra.annotation.Participant;
import io.narayana.sra.annotation.Prepare;
import io.narayana.sra.annotation.Rollback;
import io.narayana.sra.annotation.Status;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static io.narayana.sra.client.SRAClient.SRA_HTTP_HEADER;

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
     * Get the LRA context of the currently running method.
     * Note that @HeaderParam(LRA_HTTP_HEADER) does not match the header (done't know why) so we the httpRequest
     *
     * @return the LRA context of the currently running method
     */
    protected String getCurrentActivityId() {
        return httpRequest.getHeader(SRA_HTTP_HEADER);
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
    @Path("/commit")
    @Produces(MediaType.APPLICATION_JSON)
    @Commit
    public Response commitWork() throws NotFoundException {
        return updateState(SRAStatus.TransactionCommitted, getCurrentActivityId());
    }

    @PUT
    @Path("/prepare")
    @Produces(MediaType.APPLICATION_JSON)
    @Prepare
    public Response prepareWork() throws NotFoundException {
        return updateState(SRAStatus.TransactionPrepared, getCurrentActivityId());
    }

    @PUT
    @Path("/rollback")
    @Produces(MediaType.APPLICATION_JSON)
    @Rollback
    public Response rollbackWork() throws NotFoundException {
        return updateState(SRAStatus.TransactionRolledBack, getCurrentActivityId());
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Status
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public Response status() throws NotFoundException {
        String lraId = getCurrentActivityId();

        if (lraId == null)
            throw new InvalidSRAId("null", "not present on SRAParticipant#status request", null);

        if (!sraParticipantStatusMap.containsKey(lraId))
            throw new InvalidSRAId(lraId, "SRAParticipant#status request: unknown lra id", null);

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
        return updateState(sraParticipantStatusMap.get(lraId), lraId);
    }

    @PostConstruct
    public void postConstruct() {
        sraParticipantStatusMap = new HashMap<>();
    }

    /**
     * If the compensator was successful return a 200 status code and optionally an application specific string
     * that can be used by whoever closed the LRA (that triggered this compensator).
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
            default:
                String statusUrl = getStatusUrl(activityId);

                return Response.status(Response.Status.ACCEPTED).entity(Entity.text(statusUrl)).build();
        }
    }

    private String getStatusUrl(String lraId) {
        return String.format("%s/%s/activity/status", context.getBaseUri(), SRAClient.getSRAId(lraId));
    }
}
