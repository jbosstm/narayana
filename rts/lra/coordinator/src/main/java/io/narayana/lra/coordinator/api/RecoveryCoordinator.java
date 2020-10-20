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
package io.narayana.lra.coordinator.api;

import io.narayana.lra.LRAData;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.logging.LRALogger;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static io.narayana.lra.LRAConstants.RECOVERY_COORDINATOR_PATH_NAME;
import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

@ApplicationScoped
@Path(COORDINATOR_PATH_NAME + "/" + RECOVERY_COORDINATOR_PATH_NAME)
@Tag(name = "LRA Recovery")
public class RecoveryCoordinator {

    private final Logger logger = Logger.getLogger(RecoveryCoordinator.class.getName());

    @Context
    private UriInfo context;

    @Inject
    LRAService lraService;

    // Performing a GET on the recovery URL (return from a join request) will return the original <participant URL>
    @GET
    @Path("{LRAId}/{RecCoordId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Lookup the participant URL", description = "Performing a GET on the recovery URL " +
        "(returned from a join request) will return the original participant URL(s)")
    @APIResponses({
        @APIResponse(responseCode = "404", description = "The coordinator has no knowledge of this participant"),
        @APIResponse(responseCode = "200", description = "The participant associated with this recovery id is returned",
            content = @Content(schema = @Schema(title = "The original participant URI")))
    })
    public String getCompensator(
            @Parameter(name = "LRAId", description = "Identifies the LRAId that the participant joined", required = true)
            @PathParam("LRAId") String lraId,
            @Parameter(name = "RecCoordId",
                description = "An identifier that was returned by the coordinator when a participant joined the LRA",
                required = true)
            @PathParam("RecCoordId") String rcvCoordId) throws NotFoundException {

        String compensatorUrl = lraService.getParticipant(rcvCoordId);

        if (compensatorUrl == null) {
            LRALogger.i18NLogger.error_cannotFoundCompensatorUrl(compensatorUrl, lraId);
            throw new NotFoundException(rcvCoordId);
        }

        return compensatorUrl;
    }

    // Performing a PUT on the recovery URL will overwrite the old <compensor URL> with the new one supplied
    // and return the old url
    @PUT
    @Path("{LRAId}/{RecCoordId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the endpoint that a participant is prepared to accept requests on.",
        description = "Performing a PUT on the recovery URL will overwrite the old <compensor URL> with the new one supplied" +
            " and return the old url. The old value is returned." +
            "The full URL was returned when the participant first joined the LRA.")
    @APIResponses({
        @APIResponse(responseCode = "404", description = "The coordinator has no knowledge of this participant"),
        @APIResponse(responseCode = "200", description = "The coordinator has replaced the old participant with the new one")
    })
    public String replaceCompensator(
            @Parameter(name = "LRAId",
                description = "Identifies the LRAId that the participant joined",
                required = true)
            @PathParam("LRAId") String lraId,
            @Parameter(name = "RecCoordId",
                description = "An identifier that was returned by the coordinator when a participant joined the LRA",
                required = true)
            @PathParam("RecCoordId") String rcvCoordId,
            String newCompensatorUrl) throws NotFoundException {
        String compensatorUrl = lraService.getParticipant(rcvCoordId);

        if (compensatorUrl != null) {
            URI lra = null;

            try {
                lra = new URI(lraId);
            } catch (URISyntaxException e) {
                LRALogger.i18NLogger.error_invalidFormatOfLraIdReplacingCompensatorURI(lraId, compensatorUrl, e);
                String errMsg = String.format("%s: %s", lraId, e.getMessage());
                throw new WebApplicationException(errMsg, e,
                        Response.status(INTERNAL_SERVER_ERROR.getStatusCode()).entity(errMsg).build());
            }

            lraService.updateRecoveryURI(lra, newCompensatorUrl, rcvCoordId, true);

            return context.getRequestUri().toASCIIString();
        }

        LRALogger.i18NLogger.error_cannotFoundCompensatorUrl(compensatorUrl, lraId);
        String errorMsg = lraId + ": Cannot find compensator URL " + compensatorUrl;
        throw new NotFoundException(errorMsg, Response.status(NOT_FOUND).entity(rcvCoordId).build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List recovering Long Running Actions",
        description = "Returns LRAs that are recovering (ie some compensators still need to be ran)")
    @APIResponse(responseCode = "200",
        content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = LRAData.class)))
    public List<LRAData> getRecoveringLRAs() {
        return lraService.getAllRecovering(true);
    }

    @GET
    @Path("failed")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List failed Long Running Actions",
            description = "Returns LRAs that have failed. " +
                    " Failure records are vital pieces of data needed to aid failure tracking and analysis " +
                    " and are retained for inspection.")
    @APIResponse(responseCode = "200",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = LRAData.class)))
    public List<LRAData> getFailedLRAs() {
        return lraService.getFailedLRAs();
    }

    @DELETE
    @Path("{LraId}")
    @Operation(summary = "Remove the log for a failed LRA")
    @APIResponses({
            @APIResponse(responseCode = "204",
                    description = "If the LRA log was successfully removed"),
            @APIResponse(responseCode = "412",
                    description = "If the LRA is not in an end state (in which case the response " +
                            "entity will indicate the current state at the time of the request)"),
            @APIResponse(responseCode = "412",
                    description = "If the input LRA does not correspond to a valid URI (in which case the " +
                            "response entity will contain the error message)"),
            @APIResponse(responseCode = "500",
                    description = "If the attempt to remove the LRA log failed. This return code does not " +
                            "discriminate between a failure at the log storage level or if the log did not exist)")
    })
    public Response deleteFailedLRA(
            @Parameter(name = "LraId", description = "The unique identifier of the LRA", required = true)
            @PathParam("LraId")String lraId) throws NotFoundException {
        URI lra;

        try {
            lra = new URI(lraId);

            // verify that the LRA is not still being processed
            // will throw NotFoundException if it's unknown or is failed (to be caught and processed in the catch block)
            LRAData lraData = lraService.getLRA(lra);

            // 412 the LRA is not in an end state (return 412 and the actual status of the LRA)
            return Response.status(PRECONDITION_FAILED).entity(lraData.getStatus().name()).build();
        } catch (NotFoundException e) {
            // the LRA has finished and, if it corresponds to a failure record, it is safe to delete it
            if (lraService.removeLog(lraId)) {
                // 204 the log for the LRA was successfully removed
                return Response.noContent().build(); // return 204
            }

            // 500 remove log failed (or it was not present)
            return Response.status(INTERNAL_SERVER_ERROR).build();
        } catch (URISyntaxException e) {
            // 412 the user provided URI was invalid
            if (LRALogger.logger.isDebugEnabled()) {
                LRALogger.logger.debugf("%s#deleteLRA: %s: %s",
                        getClass().getCanonicalName(), lraId, e.getMessage());
            }

            return Response.status(PRECONDITION_FAILED).entity(String.format("%s: %s", lraId, e.getMessage())).build();
        }
    }
}
