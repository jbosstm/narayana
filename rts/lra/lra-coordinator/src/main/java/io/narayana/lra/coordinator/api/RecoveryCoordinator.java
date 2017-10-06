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

import io.narayana.lra.client.GenericLRAException;
import io.narayana.lra.coordinator.domain.model.LRAStatus;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jboss.logging.Logger;
import io.narayana.lra.client.LRAClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@ApplicationScoped
@Path(LRAClient.RECOVERY_COORDINATOR_PATH_NAME)
@Api(value = LRAClient.RECOVERY_COORDINATOR_PATH_NAME, tags = "LRA Recovery")
public class RecoveryCoordinator {

    private final Logger logger = Logger.getLogger(RecoveryCoordinator.class.getName());

    @Context
    private UriInfo context;

    @Inject
    private LRAService lraService;

    // Performing a GET on the recovery URL (return from a join request) will return the original <compensator URL>
    @GET
    @Path("{LRAId}/{RecCoordId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lookup the compensator URL",
            notes = "Performing a GET on the recovery URL (returned from a join request) will return the original compensator URL(s)",
            response = String.class)
    @ApiResponses( {
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this compensator" ),
            @ApiResponse( code = 200, message = "The compensator associated with this recovery id is returned" )
    } )
    public String getCompensator(
            @ApiParam( value = "Identifies the LRAId that the compensator joined", required = true )
            @PathParam("LRAId") String lraId,
            @ApiParam( value = "An identifier that was returned by the coordinator when a compensator joined the LRA", required = true )
            @PathParam("RecCoordId") String rcvCoordId) throws NotFoundException {

        String compensatorUrl = lraService.getParticipant(rcvCoordId);

        if (compensatorUrl == null)
            throw new NotFoundException(rcvCoordId);

        return compensatorUrl;
    }

    // Performing a PUT on the recovery URL will overwrite the old <compensor URL> with the new one supplied
    // and return the old url
    @PUT
    @Path("{LRAId}/{RecCoordId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update the endpoint that a compensator is prepared to accept requests on.",
            notes = "Performing a PUT on the recovery URL will overwrite the old <compensor URL> with the new one supplied" +
                    " and return the old url. The old value is returned." +
                    "The full URL was returned when the compensator first joined the LRA.",
            response = String.class)
    @ApiResponses( {
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this compensator" ),
            @ApiResponse( code = 200, message = "The coordinator has replaced the old compensator with the new one " )
    } )
    public String replaceCompensator(
            @ApiParam( value = "Identifies the LRAId that the compensator joined", required = true )
            @PathParam("LRAId") String lraId,
            @ApiParam( value = "An identifier that was returned by the coordinator when a compensator joined the LRA", required = true )
            @PathParam("RecCoordId") String rcvCoordId,
            String newCompensatorUrl) throws NotFoundException {
        String compensatorUrl = lraService.getParticipant(rcvCoordId);

        if (compensatorUrl != null) {
            URL lra = null;

            try {
                lra = new URL(lraId);
            } catch (MalformedURLException e) {
                throw new GenericLRAException(null,
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage(), e.getCause());
            }

            lraService.updateRecoveryURL(lra, newCompensatorUrl, rcvCoordId, true);

            try {
                return context.getRequestUri().toURL().toExternalForm();
            } catch (MalformedURLException e) { // cannot happen
                throw new GenericLRAException(lra,
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage(), e.getCause());
            }
        }

        throw new NotFoundException(rcvCoordId);
    }

    @GET
    @Path("recovery")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List recovering Long Running Actions",
            notes = "Returns LRAs that are recovering (ie some compensators still need to be ran)",
            response = LRAStatus.class, responseContainer = "List")
    @ApiResponses( {
            @ApiResponse( code = 200, message = "The request was successful" )
    } )
    public List<LRAStatus> getRecoveringLRAs() {
        return lraService.getAllRecovering(true);
    }
}
