/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package participant.demo;

import org.jboss.narayana.rts.lra.annotation.Compensate;
import org.jboss.narayana.rts.lra.annotation.CompensatorStatus;
import org.jboss.narayana.rts.lra.annotation.Complete;
import org.jboss.narayana.rts.lra.annotation.LRA;
import org.jboss.narayana.rts.lra.annotation.Leave;
import org.jboss.narayana.rts.lra.annotation.Status;
import org.jboss.narayana.rts.lra.client.InvalidLRAId;
import org.jboss.narayana.rts.lra.client.LRAClient;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.util.HashMap;
import java.util.Map;

import static org.jboss.narayana.rts.lra.client.LRAClient.LRA_HTTP_HEADER;

@RequestScoped
public abstract class Compensator {
    @Context
    private UriInfo context;

    @Context
    private HttpServletRequest httpRequest;

    private Map<String, CompensatorStatus> compensatorStatusMap;

    /**
     * Tell the compensator to move to the requested state.
     *
     * @param status the next state to move to
     * @param activityId the current LRA context
     * @return the state that compensator achieved
     */
    protected abstract CompensatorStatus updateCompensator(CompensatorStatus status, String activityId);

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
        return httpRequest.getHeader(LRA_HTTP_HEADER);
    }

    @POST
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeWork() throws NotFoundException {
        return updateState(CompensatorStatus.Completed, getCurrentActivityId());
    }

    @POST
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensateWork() throws NotFoundException {
        return updateState(CompensatorStatus.Compensated, getCurrentActivityId());
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Status
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Response status() throws NotFoundException {
        String lraId = getCurrentActivityId();

        if (lraId == null)
            throw new InvalidLRAId("null", "not present on Compensator#status request", null);

        if (!compensatorStatusMap.containsKey(lraId))
            throw new InvalidLRAId(lraId, "Compensator#status request: unknown lra id", null);

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
        return updateState(compensatorStatusMap.get(lraId), lraId);
    }

    @PUT
    @Path("/leave")
    @Produces(MediaType.APPLICATION_JSON)
    @Leave
    public Response leaveWork(@HeaderParam(LRA_HTTP_HEADER) String lraId) throws NotFoundException {
        return Response.ok().build();
    }

    @PostConstruct
    public void postConstruct() {
        compensatorStatusMap = new HashMap<>();
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
    private Response updateState(CompensatorStatus status, String activityId) {

        CompensatorStatus newStatus = updateCompensator(status, activityId);

        compensatorStatusMap.put(activityId, newStatus); // NB in the demo we never remove completed activities

        switch (newStatus) {
            case Completed:
            case Compensated:
                String data = getCompensatorData(activityId);

                return data == null ? Response.ok().build() : Response.ok(data).build();
            default:
                String statusUrl = getStatusUrl(activityId);

                return Response.status(Response.Status.ACCEPTED).entity(Entity.text(statusUrl)).build();
        }
    }

    private String getStatusUrl(String lraId) {
        return String.format("%s/%s/activity/status", context.getBaseUri(), LRAClient.getLRAId(lraId));
    }
}
