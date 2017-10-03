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
import io.narayana.lra.coordinator.domain.model.Transaction;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.jboss.logging.Logger;

import io.narayana.lra.annotation.CompensatorStatus;
import io.narayana.lra.client.Current;
import io.narayana.lra.client.IllegalLRAStateException;
import io.narayana.lra.client.InvalidLRAId;
import io.narayana.lra.client.LRAClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiOperation;

import static io.narayana.lra.client.LRAClient.CLIENT_ID_PARAM_NAME;
import static io.narayana.lra.client.LRAClient.COORDINATOR_PATH_NAME;

import static io.narayana.lra.client.LRAClient.LRA_HTTP_HEADER;
import static io.narayana.lra.client.LRAClient.LRA_HTTP_RECOVERY_HEADER;
import static io.narayana.lra.client.LRAClient.PARENT_LRA_PARAM_NAME;
import static io.narayana.lra.client.LRAClient.TIMELIMIT_PARAM_NAME;

@ApplicationScoped
@Path(COORDINATOR_PATH_NAME)
@Api(value = COORDINATOR_PATH_NAME, tags = "LRA Coordinator")
public class Coordinator {

    private final Logger logger = Logger.getLogger(Coordinator.class.getName());

    @Context
    private UriInfo context;

    @Inject // Will not work in an async scenario: CDI-452
    private LRAService lraService;

    // Performing a GET on /lra-io.narayana.lra.coordinator returns a list of all transactions.
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returns all LRAs",
            notes = "Gets both active and recovering LRAs",
            response = LRAStatus.class, responseContainer = "List")
    public List<LRAStatus> getAllLRAs() {
        return lraService.getAll();
    }

    // Performing a GET on /lra-io.narayana.lra.coordinator/<LraId> returns 200 if the lra is still active.
    @GET
    @Path("/status/{LraId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Indicates whether an LRA is active",
            response = Boolean.class)
    @ApiResponses( {
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this LRA" ),
            @ApiResponse( code = 200, message = "If the LRA exists" )
    } )
    public Boolean isActiveLRA(
            @ApiParam( value = "The unique identifier of the LRA", required = true )
            @PathParam("LraId")String lraId) throws NotFoundException {
        return lraService.getTransaction(toURL(lraId)).isActive();
    }

    // Performing a GET on /lra-io.narayana.lra.coordinator/recovery returns a list of recovering transactions.
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
        return lraService.getAllRecovering();
    }

    // Performing a GET on /lra-io.narayana.lra.coordinator/active returns a list of inflight transactions.
    @GET
    @Path("active")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lookup active LRAs",
            response = LRAStatus.class, responseContainer = "List")
    @ApiResponses( {
            @ApiResponse( code = 200, message = "The request was successful" )
    } )
    public List<LRAStatus> getActiveLRAs() {
        return lraService.getAllActive();
    }

    // Performing a POST on /lra-io.narayana.lra.coordinator/start?ClientID=<ClientID> will start a new lra with a default timeout and
    // return a lra URL of the form <machine>/lra-io.narayana.lra.coordinator/<LraId>.
    // Adding a query parameter, timeout=<timeout>, will start a new lra with the specified timeout.
    // If the lra is terminated because of a timeout, the lra URL is deleted and all further invocations on the URL will return 404.
    // The invoker can assume this was equivalent to a compensate operation.
    @POST
    @Path("start")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @ApiOperation(value = "Start a new LRA",
            notes = "The LRA model uses a presumed nothing protocol: the coordinator must communicate\n"
                    + "with Compensators in order to inform them of the LRA activity. Every time a\n"
                    + "Compensator is enrolled with a LRA, the coordinator must make information about\n"
                    + "it durable so that the Compensator can be contacted when the LRA terminates,\n"
                    + "even in the event of subsequent failures. Compensators, clients and coordinators\n"
                    + "cannot make any presumption about the state of the global transaction without\n"
                    + "consulting the coordinator and all compensators, respectively.",
            response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "The request was successful and the response body contains the id of the new LRA"),
            @ApiResponse(code = 500, message = "A new LRA could not be started")
    } )

    public Response startLRA(
            @ApiParam( value = "Each client is expected to have a unique identity (which can be a URL).", required = false)
            @QueryParam(CLIENT_ID_PARAM_NAME) @DefaultValue("") String clientId,
            @ApiParam( value = "Specifies the maximum time in milli seconds that the LRA will exist for.\n"
                    + "If the LRA is terminated because of a timeout, the LRA URL is deleted.\n"
                    + "All further invocations on the URL will return 404.\n"
                    + "The invoker can assume this was equivalent to a compensate operation.")
            @QueryParam(TIMELIMIT_PARAM_NAME) @DefaultValue("0") Long timelimit,
            @ApiParam( value = "The enclosing LRA if this new LRA is nested", required = false)
            @QueryParam(PARENT_LRA_PARAM_NAME) @DefaultValue("") String parentLRA,
            @HeaderParam(LRA_HTTP_HEADER) String parentId) throws WebApplicationException, InvalidLRAId {

        URL parentLRAUrl = null;

        if (parentLRA != null && !parentLRA.isEmpty())
            parentLRAUrl = LRAClient.lraToURL(parentLRA, "Invalid parent LRA id");

        String coordinatorUrl = String.format("%s%s", context.getBaseUri(), COORDINATOR_PATH_NAME);
        URL lraId = lraService.startLRA(coordinatorUrl, parentLRAUrl, clientId, timelimit);

        if (parentLRAUrl != null) {
            // register with the parentLRA as a participant
            Client client = ClientBuilder.newClient();
            String compensatorUrl = String.format("%s/%s", coordinatorUrl,
                    LRAClient.encodeURL(lraId, "Invalid parent LRA id"));
            Response response;

            if (lraService.hasTransaction(parentLRAUrl))
                response = joinLRAViaBody(parentLRAUrl.toExternalForm(), timelimit, null, compensatorUrl);
            else
                response = client.target(parentLRA).request().put(Entity.text(compensatorUrl));

            if (response.getStatus() != Response.Status.OK.getStatusCode())
                return response;
        }

        Current.push(lraId);

        return Response.status(Response.Status.CREATED)
                .entity(lraId)
                .header(LRA_HTTP_HEADER, lraId)
                .build();
    }

    @PUT
    @Path("{LraId}/renew")
    @ApiOperation(value = "Update the TimeLimit for an existing LRA",
            notes = "LRAs can be automatically cancelled if they aren't closed or cancelled before the TimeLimit\n"
                    + "specified at creation time is reached.\n"
                    + "The time limit can be updated.\n")
    @ApiResponses({
            @ApiResponse( code = 200, message = "If the LRA timelimit has been updated" ),
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this LRA" ),
            @ApiResponse( code = 412, message = "The LRA is not longer active (ie in the complete or compensate messages have been sent" )
    } )
    public Response renewTimeLimit(
            @ApiParam( value = "The new time limit for the LRA", required = true )
            @QueryParam(TIMELIMIT_PARAM_NAME) @DefaultValue("0") Long timelimit,
            @PathParam("LraId")String lraId) throws NotFoundException {

        return Response.status(lraService.renewTimeLimit(toURL(lraId), timelimit)).build();
    }

    @GET
    @Path("{NestedLraId}/status")
    public Response getNestedLRAStatus(@PathParam("NestedLraId")String nestedLraId) {
        if (!lraService.hasTransaction(nestedLraId)) {
            // it must have compensated TODO maybe it's better to keep nested LRAs in separate collection
            return Response.ok(CompensatorStatus.Compensated.name()).build();
        }

        Transaction lra = lraService.getTransaction(toURL(nestedLraId));
        CompensatorStatus status = lra.getLRAStatus();

        if (status == null)
            throw new IllegalLRAStateException(nestedLraId, "The LRA is still active", null);

        if (lra.getLRAStatus() == null)
            throw new IllegalLRAStateException(nestedLraId, "LRA is still active", null);

        return Response.ok(lra.getLRAStatus().name()).build();
    }

    @POST
    @Path("{NestedLraId}/complete")
    public Response completeNestedLRA(@PathParam("NestedLraId") String nestedLraId) {
        return endLRA(toURL(nestedLraId), false, true);
    }

    @POST
    @Path("{NestedLraId}/compensate")
    public Response compensateNestedLRA(@PathParam("NestedLraId") String nestedLraId) {
        return endLRA(toURL(nestedLraId), true, true);
    }

    @POST
    @Path("{NestedLraId}/forget")
    public Response forgetNestedLRA(@PathParam("NestedLraId") String nestedLraId) {
        lraService.remove(null, toURL(nestedLraId));

        return Response.ok().build();
    }

    // Performing a GET on /lra-io.narayana.lra.coordinator/completed/<LraId> returns 200 if the lra completed successfully
    // (a 404 response means it is not present).
    @GET
    @Path("completed/{LraId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Indicates whether an LRA is complete",
            response = Boolean.class)
    @ApiResponses( {
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this LRA" ),
            @ApiResponse( code = 200, message = "If the LRA exists and its' status is known" )
    } )
    public Response isCompletedLRA(
            @ApiParam( value = "The unique identifier of the LRA", required = true )
            @PathParam("LraId")String lraId) throws NotFoundException {
        return Response.ok(Boolean.toString(lraService.getTransaction(toURL(lraId)).isComplete())).build();
    }

    // Performing a GET on /lra-io.narayana.lra.coordinator/compensated/<LraId> returns 200 if the lra compensated (a 404 response means it is not present).
    @GET
    @Path("compensated/{LraId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Indicates whether an LRA was compensated",
            response = Boolean.class)
    @ApiResponses( {
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this LRA" ),
            @ApiResponse( code = 200, message = "If the LRA exists and its' status is known" )
    } )
    public Response isCompensatedLRA(
            @ApiParam( value = "The unique identifier of the LRA", required = true )
            @PathParam("LraId")String lraId) throws NotFoundException {
        return Response.ok(Boolean.toString(lraService.getTransaction(toURL(lraId)).isCompensated())).build();
    }

    // Performing a PUT on lra-coordinator/<LraId>/close will trigger the successful completion of the lra and all
    // compensators will be dropped by the io.narayana.lra.coordinator.
    // The complete message will be sent to the compensators. Question: is this message best effort or at least once?
    // Upon termination, the URL is implicitly deleted. If it no longer exists, then 404 will be returned.
    // The invoker cannot know for sure whether the lra completed or compensated without enlisting a participant.
    // TODO rework spec to allow an LRAStatus header everywhere
    @PUT
    @Path("{LraId}/close")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Attempt to close an LRA",
            notes = "Trigger the successful completion of the LRA. All"
                    +" compensators will be dropped by the coordinator."
                    +" The complete message will be sent to the compensators."
                    +" Upon termination, the URL is implicitly deleted."
                    +" The invoker cannot know for sure whether the lra completed or compensated without enlisting a participant.",
            response = Boolean.class)
    @ApiResponses( {
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this LRA" ),
            @ApiResponse( code = 200, message = "The complete message was sent to all coordinators" )
    } )
    public Response closeLRA(
            @ApiParam( value = "The unique identifier of the LRA", required = true )
            @PathParam("LraId")String txId) throws NotFoundException {
        return endLRA(toURL(txId), false, false);
    }

    @PUT
    @Path("{LraId}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Attempt to cancel an LRA",
            notes =   " Trigger the compensation of the LRA. All"
                    + " compensators will be triggered by the coordinator (ie the compensate message will be sent to each compensators)."
                    + " Upon termination, the URL is implicitly deleted."
                    + " The invoker cannot know for sure whether the lra completed or compensated without enlisting a participant.",
            response = Boolean.class)
    @ApiResponses( {
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this LRA" ),
            @ApiResponse( code = 200, message = "The compensate message was sent to all coordinators" )
    } )
    public Response cancelLRA(
            @ApiParam( value = "The unique identifier of the LRA", required = true )
            @PathParam("LraId")String lraId) throws NotFoundException {
        return endLRA(toURL(lraId), true, false);
    }


    private Response endLRA(URL lraId, boolean compensate, boolean fromHierarchy) throws NotFoundException {
        LRAStatus status = lraService.endLRA(lraId, compensate, fromHierarchy);
        String compensatorData = null;
        try {
            compensatorData = status.getEncodedResponseData();
        } catch (IOException e) {
            compensatorData = "Unable to encode as JSON";
        }

        return compensatorData == null
                ? Response.ok().status(status.getHttpStatus()).build()
                : Response.ok(compensatorData).status(status.getHttpStatus()).build();
    }

    //    @PUT
//    @Path("/")
//    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "A Compensator can join with the LRA at any time prior to the completion of an activity",
            response = String.class)
    @ResponseHeader(name = LRA_HTTP_RECOVERY_HEADER, response = String.class,
            description = "If the compensator is successfully registered with the LRA then this header\n"
                    + " will contain a unique resource reference for that compensator:\n"
                    + " - HTTP GET on the reference returns the original compensator URL;\n"
                    + " - HTTP PUT on the reference will overwrite the old compensator URL with the new one supplied.")
    @ApiResponses( {
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this LRA" ),
            @ApiResponse( code = 412, message = "The LRA is not longer active (ie in the complete or compensate messages have been sent" ),
            @ApiResponse( code = 200, message = "The compensator was successfully registered with the LRA and"
                    + " the response body contains a unique resource reference for that compensator:\n"
                    + " - HTTP GET on the reference returns the original compensator URL;\n"
                    + " - HTTP PUT on the reference will overwrite the old compensator URL with the new one supplied."
            )
    } )
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    // TODO don't use headers to pass the LRA context to the coordinator (follow the spec instead via joinLRAViaBody())
    public Response joinLRAViaHeader(
            @ApiParam( value = "The identifier of the LRA that the compensator wishes to join", required = true )
            @HeaderParam(LRA_HTTP_HEADER) String lraId,
            @ApiParam( value = "The resource paths that the coordinator will use to complete or compensate and to request"
                    + " the status of the compensator. The link rel names are"
                    + " complete, compensate and status",
                    required = true )
            @HeaderParam("Link") String linkHeader,
            @ApiParam( value = "The time limit (in seconds) that the Compensator can guarantee that it can compensate the work performed by the service."
                    + " After this time period has elapsed, it may no longer be possible to undo the work within the scope of this (or any enclosing) LRA."
                    + " It may therefore be necessary for the application or service to start other activities to explicitly try to compensate this work."
                    + " The application or coordinator may use this information to control the lifecycle of a LRA.",
                    required = true )
            @QueryParam(TIMELIMIT_PARAM_NAME) @DefaultValue("0") long timeLimit,
            @ApiParam( value = "compensator data that will be passed back to the compensator when LRA is terminated" )
            String userData) throws NotFoundException {
        return joinLRA(toURL(lraId), timeLimit, null, linkHeader, userData);
    }

    @PUT
    @Path("{LraId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "A Compensator can join with the LRA at any time prior to the completion of an activity",
            response = String.class)
    @ResponseHeader(name = LRA_HTTP_RECOVERY_HEADER, response = String.class,
            description = "If the compensator is successfully registered with the LRA then this header\n"
                    + " will contain a unique resource reference for that compensator:\n"
                    + " - HTTP GET on the reference returns the original compensator URL;\n"
                    + " - HTTP PUT on the reference will overwrite the old compensator URL with the new one supplied.")
    @ApiResponses( {
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this LRA" ),
            @ApiResponse( code = 412, message = "The LRA is not longer active (ie in the complete or compensate messages have been sent" ),
            @ApiResponse( code = 200, message = "The compensator was successfully registered with the LRA and"
                    + " the response body contains a unique resource reference for that compensator:\n"
                    + " - HTTP GET on the reference returns the original compensator URL;\n"
                    + " - HTTP PUT on the reference will overwrite the old compensator URL with the new one supplied."
            )
    } )
    public Response joinLRAViaBody(
            @ApiParam( value = "The unique identifier of the LRA", required = true )
            @PathParam("LraId")String lraId,
            @ApiParam( value = "The time limit (in seconds) that the Compensator can guarantee that it can compensate the work performed by the service."
                    + " After this time period has elapsed, it may no longer be possible to undo the work within the scope of this (or any enclosing) LRA."
                    + " It may therefore be necessary for the application or service to start other activities to explicitly try to compensate this work."
                    + " The application or coordinator may use this information to control the lifecycle of a LRA.",
                    required = true )
            @QueryParam(TIMELIMIT_PARAM_NAME) @DefaultValue("0") long timeLimit,
            @ApiParam( value = "The resource paths that the coordinator will use to complete or compensate and to request"
                    + " the status of the compensator. The link rel names are"
                    + " complete, compensate and status.",
                    required = false )
            @HeaderParam("Link") String compensatorLink,
            @ApiParam( value = "The resource path that the LRA coordinator will use to drive the compensator.\n"
                    + "Performing a GET on the compensator URL will return the current status of the compensator,\n"
                    + "or 404 if the compensator is no longer present.\n"
                    + "\n"
                    + "The following types must be returned by Compensators to indicate their current status:\n"
                    + "-  Compensating: the Compensator is currently compensating for the jfdi.\n"
                    + "-  Compensated: the Compensator has successfully compensated for the jfdi.\n"
                    + "-  FailedToCompensate: the Compensator was not able to compensate for the jfdi.\n"
                    + "   It must maintain information about the work it was to compensate until the\n"
                    + "   coordinator sends it a forget message.\n"
                    + "-  Completing: the Compensator is tidying up after being told to complete.\n"
                    + "-  Completed: the coordinator/participant has confirmed.\n"
                    + "-  FailedToComplete: the Compensator was unable to tidy-up.\n"
                    + "\n"
                    + "Performing a POST on <URL>/compensate will cause the compensator to compensate\n"
                    + "  the work that was done within the scope of the LRA.\n"
                    + "Performing a POST on <URL>/complete will cause the compensator to tidy up and\n"
                    + "   it can forget this LRA.\n")
                    String compensatorUrl) throws NotFoundException {
        // test to see if the join request contains any compensator specific data
        boolean isLink = isLink(compensatorUrl);

        if (compensatorLink != null && !isLink)
            return joinLRA(toURL(lraId), timeLimit, compensatorLink, null, compensatorUrl);

        if (!isLink) { // interpret the content as a standard compensator url
            compensatorUrl += "/";

            Map<String, String> terminateURIs = new HashMap<>();

            try {
                terminateURIs.put(LRAClient.COMPENSATE, new URL(compensatorUrl + "compensate").toExternalForm());
                terminateURIs.put(LRAClient.COMPLETE, new URL(compensatorUrl + "complete").toExternalForm());
                terminateURIs.put(LRAClient.STATUS, new URL(compensatorUrl + "status").toExternalForm());
            } catch (MalformedURLException e) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            // register with the coordinator
            // put the lra id in an http header
            StringBuilder linkHeaderValue = new StringBuilder();

            terminateURIs.forEach((k, v) -> makeLink(linkHeaderValue, "", k, v)); // or use Collectors.joining(",")

            compensatorUrl = linkHeaderValue.toString();
        }

        return joinLRA(toURL(lraId), timeLimit, null, compensatorUrl, null);
    }


    private static StringBuilder makeLink(StringBuilder b, String uriPrefix, String key, String value) {

        if (value == null)
            return b;

        String terminationUri = uriPrefix == null ? value : String.format("%s%s", uriPrefix, value);
        Link link =  Link.fromUri(terminationUri).rel(key).type(MediaType.TEXT_PLAIN).build();

        if (b.length() != 0)
            b.append(',');

        return b.append(link);
    }

    private boolean isLink(String linkString) {
        try {
            Link.valueOf(linkString);

            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Response joinLRA(URL lraId, long timeLimit, String compensatorUrl, String linkHeader, String userData)
            throws NotFoundException {
        final String recoveryUrlBase = String.format("http://%s/%s/",
                context.getRequestUri().getAuthority(), LRAClient.RECOVERY_COORDINATOR_PATH_NAME);

        StringBuilder recoveryUrl = new StringBuilder();

        int status = lraService.joinLRA(recoveryUrl, lraId, timeLimit, compensatorUrl, linkHeader, recoveryUrlBase, userData);

        return Response.status(status).entity(recoveryUrl).header(LRA_HTTP_RECOVERY_HEADER, recoveryUrl).build();
    }

    // A compensator can resign from a lra at any time prior to the completion of an activity by performing a
    // PUT on lra-coordinator/<LraId>/remove with the URL of the compensator.
    @PUT
    @Path("{LraId}/remove")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "A Compensator can resign from the LRA at any time prior to the completion of an activity",
            response = Boolean.class)
    @ApiResponses( {
            @ApiResponse( code = 404, message = "The coordinator has no knowledge of this LRA" ),
            @ApiResponse( code = 412, message = "The LRA is not longer active (ie in the complete or compensate messages have been sent" ),
            @ApiResponse( code = 200, message = "If the compensator was successfully removed from the LRA" )
    } )
    public Response leaveLRA(
            @ApiParam( value = "The unique identifier of the LRA", required = true )
            @PathParam("LraId") String lraId,
            String compensatorUrl) throws NotFoundException, MalformedURLException {
        String reqUri = context.getRequestUri().toString();

        reqUri =  reqUri.substring(0, reqUri.lastIndexOf('/'));

        int status = lraService.leave(new URL(reqUri), compensatorUrl);

        return Response.status(status).build();
    }


    private URL toURL(String lraId) {
        URL url;

        try {
            // see if it already in the correct format
            url = new URL(lraId);
            url.toURI();

        } catch (Exception e) {
            try {
                url = new URL(String.format("%s%s/%s", context.getBaseUri(), COORDINATOR_PATH_NAME, lraId));
            } catch (MalformedURLException e1) {
                throw new InvalidLRAId(lraId, "Invalid LRA id format", e1);
            }
        }

        return url;
    }
}
