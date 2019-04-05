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

import io.narayana.lra.Current;
import io.narayana.lra.GenericLRAException;
import io.narayana.lra.IllegalLRAStateException;
import io.narayana.lra.InvalidLRAIdException;
import io.narayana.lra.coordinator.domain.model.LRAData;
import io.narayana.lra.coordinator.domain.model.LRAStatusHolder;
import io.narayana.lra.coordinator.domain.model.Transaction;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.logging.LRALogger;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

import javax.enterprise.context.ApplicationScoped;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.lra.annotation.LRAStatus;

import static io.narayana.lra.LRAConstants.CLIENT_ID_PARAM_NAME;
import static io.narayana.lra.LRAConstants.COMPENSATE;
import static io.narayana.lra.LRAConstants.COMPLETE;
import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;
import static io.narayana.lra.LRAConstants.PARENT_LRA_PARAM_NAME;
import static io.narayana.lra.LRAConstants.RECOVERY_COORDINATOR_PATH_NAME;
import static io.narayana.lra.LRAConstants.STATUS;
import static io.narayana.lra.LRAConstants.STATUS_PARAM_NAME;
import static io.narayana.lra.LRAConstants.TIMELIMIT_PARAM_NAME;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

@ApplicationScoped
@Path(COORDINATOR_PATH_NAME)
@Api(value = COORDINATOR_PATH_NAME, tags = "LRA Coordinator")
public class Coordinator {

    @Context
    private UriInfo context;

    @Inject // Will not work in an async scenario: CDI-452
    private LRAService lraService;

    // Performing a GET on /lra-io.narayana.lra.coordinator returns a list of all LRAs.
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returns all LRAs",
            notes = "Gets both active and recovering LRAs",
            response = LRAData.class, responseContainer = "List")
    public List<LRAData> getAllLRAs(
            @ApiParam(value = "Filter the returned LRAs to only those in the give state (see CompensatorStatus)", required = false)
            @QueryParam(STATUS_PARAM_NAME) @DefaultValue("") String state) {
        List<LRAStatusHolder> lras = lraService.getAll(state);

        if (lras == null) {
            LRALogger.i18NLogger.error_invalidQueryForGettingLraStatuses(state);
            throw new GenericLRAException(null, Response.Status.BAD_REQUEST.getStatusCode(),
                    String.format("Invalid query '%s' to get LRAs", state), null);
        }

        return lras.stream().map(Coordinator::convert).collect(toList());
    }

    private static LRAData convert(LRAStatusHolder lra) {
        return new LRAData(lra.getLraId(), lra.getClientId(),
                lra.getStatus().name(),
                lra.isClosed(), lra.isCancelled(), lra.isRecovering(), lra.isActive(), lra.isTopLevel(),
                lra.getStartTime(), lra.getFinishTime());
    }

    @GET
    @Path("{LraId}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Obtain the status of an LRA as a string",
            response = String.class)
    @ApiResponses({
            @ApiResponse(code = 404, message =
                    "The coordinator has no knowledge of this LRA"),
            @ApiResponse(code = 204, message =
                    "The LRA exists and has not yet been asked to close or cancel "
                           + " - compare this response with a 200 response.s"),
            @ApiResponse(code = 200, message =
                    "The LRA exists. The status is reported in the content body.")
    })
    public Response getLRAStatus(
            @ApiParam(value = "The unique identifier of the LRA", required = true)
            @PathParam("LraId")String lraId) throws NotFoundException {
        LRAStatus status = lraService.getTransaction(toURI(lraId)).getLRAStatus();

        if (status == null) {
            return Response.noContent().build(); // 204 means the LRA is still active
        }

        return Response.ok(status.name()).build();
    }

    @GET
    @Path("{LraId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Obtain the status of an LRA as a JSON structure",
            response = String.class)
    @ApiResponses({
            @ApiResponse(code = 404, message =
                    "The coordinator has no knowledge of this LRA"),
            @ApiResponse(code = 204, message =
                    "The LRA exists and has not yet been asked to close or cancel "
                            + " - compare this response with a 200 response.s"),
            @ApiResponse(code = 200, message =
                    "The LRA exists. The status is reported in the content body.")
    })
    public LRAData getLRAInfo(
            @ApiParam(value = "The unique identifier of the LRA", required = true)
            @PathParam("LraId") String lraId) throws NotFoundException {

        return lraService.getLRA(toURI(lraId));
    }

/*    @GET
    @Path("{LraId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Obtain the status of an LRA as a string",
            response = String.class)
    @ApiResponses({
            @ApiResponse(code = 404, message =
                    "The coordinator has no knowledge of this LRA"),
            @ApiResponse(code = 200, message =
                    "The LRA exists. A JSON representation of the state is reported in the content body.")
    })
    public LRAStatus getDetailedLRAStatus(
            @ApiParam(value = "The unique identifier of the LRA", required = true)
            @PathParam("LraId")String lraId) throws NotFoundException {
        return new LRAStatus(lraService.getTransaction(toURI(lraId)));
    }*/

    // Performing a GET on /lra-io.narayana.lra.coordinator/<LraId> returns 200 if the lra is still active.
    @GET
    @Path("/status/{LraId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Indicates whether an LRA is active",
            response = Boolean.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "The coordinator has no knowledge of this LRA"),
            @ApiResponse(code = 200, message = "If the LRA exists")
    })
    public Boolean isActiveLRA(
            @ApiParam(value = "The unique identifier of the LRA", required = true)
            @PathParam("LraId")String lraId) throws NotFoundException {
        return lraService.getTransaction(toURI(lraId)).isActive();
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
            @ApiResponse(code = 201, message = "The request was successful and the response body contains the id of the new LRA"),
            @ApiResponse(code = 500, message = "A new LRA could not be started")
    })

    public Response startLRA(
            @ApiParam(value = "Each client is expected to have a unique identity (which can be a URL).", required = false)
            @QueryParam(CLIENT_ID_PARAM_NAME) @DefaultValue("") String clientId,
            @ApiParam(value = "Specifies the maximum time in milli seconds that the LRA will exist for.\n"
                    + "If the LRA is terminated because of a timeout, the LRA URL is deleted.\n"
                    + "All further invocations on the URL will return 404.\n"
                    + "The invoker can assume this was equivalent to a compensate operation.")
            @QueryParam(TIMELIMIT_PARAM_NAME) @DefaultValue("0") Long timelimit,
            @ApiParam(value = "The enclosing LRA if this new LRA is nested", required = false)
            @QueryParam(PARENT_LRA_PARAM_NAME) @DefaultValue("") String parentLRA,
            @HeaderParam(LRA_HTTP_CONTEXT_HEADER) String parentId) throws WebApplicationException, InvalidLRAIdException {

        URI parentLRAUrl = null;

        if (parentLRA != null && !parentLRA.isEmpty()) {
            parentLRAUrl = toURI(parentLRA, "Invalid parent LRA id");
        }

        String coordinatorUrl = String.format("%s%s", context.getBaseUri(), COORDINATOR_PATH_NAME);
        URI lraId = lraService.startLRA(coordinatorUrl, parentLRAUrl, clientId, timelimit);

        if (parentLRAUrl != null) {
            // register with the parentLRA as a participant
            Client client = ClientBuilder.newClient();
            String compensatorUrl = null;
            URL url = null;

            try {
                url = lraId.toURL();
                compensatorUrl = String.format("%s/%s", coordinatorUrl,
                        URLEncoder.encode(url.toString(), "UTF-8"));
            } catch (UnsupportedEncodingException | MalformedURLException e) {
                LRALogger.i18NLogger.error_invalidFormatToEncodeUrl(url, e);
                throw new GenericLRAException(lraId, BAD_REQUEST.getStatusCode(), "Invalid parent LRA id", e);
            }
            Response response;

            if (lraService.hasTransaction(parentLRAUrl)) {
                response = joinLRAViaBody(parentLRAUrl.toASCIIString(), timelimit, null, compensatorUrl);
            } else {
                response = client.target(parentLRA).request().put(Entity.text(compensatorUrl));
            }

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                return response;
            }
        }

        Current.push(lraId);

        return Response.status(Response.Status.CREATED)
                .entity(lraId)
                .header(LRA_HTTP_CONTEXT_HEADER, Current.getContexts())
                .build();
    }

    @PUT
    @Path("{LraId}/renew")
    @ApiOperation(value = "Update the TimeLimit for an existing LRA",
            notes = "LRAs can be automatically cancelled if they aren't closed or cancelled before the TimeLimit\n"
                    + "specified at creation time is reached.\n"
                    + "The time limit can be updated.\n")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If the LRA timelimit has been updated"),
            @ApiResponse(code = 404, message = "The coordinator has no knowledge of this LRA"),
            @ApiResponse(code = 412, message = "The LRA is not longer active (ie in the complete or compensate messages have been sent")
    })
    public Response renewTimeLimit(
            @ApiParam(value = "The new time limit for the LRA", required = true)
            @QueryParam(TIMELIMIT_PARAM_NAME) @DefaultValue("0") Long timelimit,
            @PathParam("LraId")String lraId) throws NotFoundException {

        return Response.status(lraService.renewTimeLimit(toURI(lraId), timelimit)).build();
    }

    @GET
    @Path("{NestedLraId}/status")
    public Response getNestedLRAStatus(@PathParam("NestedLraId")String nestedLraId) {
        if (!lraService.hasTransaction(nestedLraId)) {
            // it must have compensated TODO maybe it's better to keep nested LRAs in separate collection
            return Response.ok(LRAStatus.Cancelled.name()).build();
        }

        Transaction lra = lraService.getTransaction(toURI(nestedLraId));
        LRAStatus status = lra.getLRAStatus();

        if (status == null || lra.getLRAStatus() == null) {
            LRALogger.i18NLogger.error_cannotGetStatusOfNestedLraURI(nestedLraId, lra.getId());
            throw new IllegalLRAStateException(nestedLraId, "The LRA is still active", "getNestedLRAStatus");
        }

        return Response.ok(lra.getLRAStatus().name()).build();
    }

    @PUT
    @Path("{NestedLraId}/complete")
    public Response completeNestedLRA(@PathParam("NestedLraId") String nestedLraId) {
        return endLRA(toURI(nestedLraId), false, true);
    }

    @PUT
    @Path("{NestedLraId}/compensate")
    public Response compensateNestedLRA(@PathParam("NestedLraId") String nestedLraId) {
        return endLRA(toURI(nestedLraId), true, true);
    }

    @PUT
    @Path("{NestedLraId}/forget")
    public Response forgetNestedLRA(@PathParam("NestedLraId") String nestedLraId) {
        lraService.remove(null, toURI(nestedLraId));

        return Response.ok().build();
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
                    + " compensators will be dropped by the coordinator."
                    + " The complete message will be sent to the compensators."
                    + " Upon termination, the URL is implicitly deleted."
                    + " The invoker cannot know for sure whether the lra completed or compensated without enlisting a participant.",
            response = Boolean.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "The coordinator has no knowledge of this LRA"),
            @ApiResponse(code = 200, message = "The complete message was sent to all coordinators")
    })
    public Response closeLRA(
            @ApiParam(value = "The unique identifier of the LRA", required = true)
            @PathParam("LraId")String txId) throws NotFoundException {
        return endLRA(toURI(txId), false, false);
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
    @ApiResponses({
            @ApiResponse(code = 404, message = "The coordinator has no knowledge of this LRA"),
            @ApiResponse(code = 200, message = "The compensate message was sent to all coordinators")
    })
    public Response cancelLRA(
            @ApiParam(value = "The unique identifier of the LRA", required = true)
            @PathParam("LraId")String lraId) throws NotFoundException {
        return endLRA(toURI(lraId), true, false);
    }


    private Response endLRA(URI lraId, boolean compensate, boolean fromHierarchy) throws NotFoundException {
        LRAStatusHolder status = lraService.endLRA(lraId, compensate, fromHierarchy);

        return Response.ok(status.getStatus().name()).build();
//        return compensatorData == null
//                ? Response.ok().status(status.getHttpStatus()).build()
//                : Response.ok(compensatorData).status(status.getHttpStatus()).build();
    }

    @PUT
    @Path("{LraId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "A Compensator can join with the LRA at any time prior to the completion of an activity",
            response = String.class)
    @ResponseHeader(name = LRA_HTTP_RECOVERY_HEADER, response = String.class,
            description = "If the participant is successfully registered with the LRA then this header\n"
                    + " will contain a unique resource reference for that participant:\n"
                    + " - HTTP GET on the reference returns the original participant URL;\n"
                    + " - HTTP PUT on the reference will overwrite the old participant URL with the new one supplied.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "The coordinator has no knowledge of this LRA"),
            @ApiResponse(code = 412, message = "The LRA is not longer active (ie in the complete or compensate messages have been sent"),
            @ApiResponse(code = 200, message = "The participant was successfully registered with the LRA and"
                    + " the response body contains a unique resource reference for that participant:\n"
                    + " - HTTP GET on the reference returns the original participant URL;\n"
                    + " - HTTP PUT on the reference will overwrite the old participant URL with the new one supplied."
           )
    })
    public Response joinLRAViaBody(
            @ApiParam(value = "The unique identifier of the LRA", required = true)
            @PathParam("LraId")String lraId,
            @ApiParam(value = "The time limit (in seconds) that the Compensator can guarantee that it can compensate the work performed by the service."
                    + " After this time period has elapsed, it may no longer be possible to undo the work within the scope of this (or any enclosing) LRA."
                    + " It may therefore be necessary for the application or service to start other activities to explicitly try to compensate this work."
                    + " The application or coordinator may use this information to control the lifecycle of a LRA.",
                    required = true)
            @QueryParam(TIMELIMIT_PARAM_NAME) @DefaultValue("0") long timeLimit,
            @ApiParam(value = "The resource paths that the coordinator will use to complete or compensate and to request"
                    + " the status of the participant. The link rel names are"
                    + " complete, compensate and status.",
                    required = false)
            @HeaderParam("Link") String compensatorLink,
            @ApiParam(value = "opaque data that will be stored with the coordinator and passed back to\n"
                    + "the participant when the LRA is closed or cancelled.\n")
                    String compensatorData) throws NotFoundException {
        // test to see if the join request contains any participant specific data
        boolean isLink = isLink(compensatorData);

        if (compensatorLink != null) {
            return joinLRA(toURI(lraId), timeLimit, null, compensatorLink, compensatorData);
        }

        if (!isLink) { // interpret the content as a standard participant url
            compensatorData += "/";

            Map<String, String> terminateURIs = new HashMap<>();

            try {
                terminateURIs.put(COMPENSATE, new URL(compensatorData + "compensate").toExternalForm());
                terminateURIs.put(COMPLETE, new URL(compensatorData + "complete").toExternalForm());
                terminateURIs.put(STATUS, new URL(compensatorData + "status").toExternalForm());
            } catch (MalformedURLException e) {
                if (LRALogger.logger.isTraceEnabled()) {
                    LRALogger.logger.tracef(e, "Cannot join to LRA id '%s' with body as compensator url '%s' is invalid",
                            lraId, compensatorData);
                }

                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            // register with the coordinator
            // put the lra id in an http header
            StringBuilder linkHeaderValue = new StringBuilder();

            terminateURIs.forEach((k, v) -> makeLink(linkHeaderValue, "", k, v)); // or use Collectors.joining(",")

            compensatorData = linkHeaderValue.toString();
        }

        return joinLRA(toURI(lraId), timeLimit, null, compensatorData, null);
    }


    private static StringBuilder makeLink(StringBuilder b, String uriPrefix, String key, String value) {

        if (value == null) {
            return b;
        }

        String terminationUri = uriPrefix == null ? value : String.format("%s%s", uriPrefix, value);
        Link link =  Link.fromUri(terminationUri).rel(key).type(MediaType.TEXT_PLAIN).build();

        if (b.length() != 0) {
            b.append(',');
        }

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

    private Response joinLRA(URI lraId, long timeLimit, String compensatorUrl, String linkHeader, String userData)
            throws NotFoundException {
        final String recoveryUrlBase = String.format("http://%s/%s/",
                context.getRequestUri().getAuthority(), RECOVERY_COORDINATOR_PATH_NAME);

        StringBuilder recoveryUrl = new StringBuilder();

        int status = lraService.joinLRA(recoveryUrl, lraId, timeLimit, compensatorUrl, linkHeader, recoveryUrlBase, userData);

        try {
            return Response.status(status)
                    .entity(recoveryUrl)
                    .location(new URI(recoveryUrl.toString()))
                    .header(LRA_HTTP_RECOVERY_HEADER, recoveryUrl)
                    .build();
        } catch (URISyntaxException e) {
            LRALogger.i18NLogger.error_invalidRecoveryUrlToJoinLRAURI(recoveryUrl.toString(), lraId);
            throw new GenericLRAException(lraId, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Invalid recovery URL", e);
        }
    }

    // A participant can resign from a lra at any time prior to the completion of an activity by performing a
    // PUT on lra-coordinator/<LraId>/remove with the URL of the participant.
    @PUT
    @Path("{LraId}/remove")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "A Compensator can resign from the LRA at any time prior to the completion of an activity",
            response = Boolean.class)
    @ApiResponses({
            @ApiResponse(code = 404, message = "The coordinator has no knowledge of this LRA"),
            @ApiResponse(code = 412, message = "The LRA is not longer active (ie in the complete or compensate messages have been sent"),
            @ApiResponse(code = 200, message = "If the participant was successfully removed from the LRA")
    })
    public Response leaveLRA(
            @ApiParam(value = "The unique identifier of the LRA", required = true)
            @PathParam("LraId") String lraId,
            String compensatorUrl) throws NotFoundException, URISyntaxException {
        String reqUri = context.getRequestUri().toString();

        reqUri =  reqUri.substring(0, reqUri.lastIndexOf('/'));

        int status = 0;

        status = lraService.leave(new URI(reqUri), compensatorUrl);

        return Response.status(status).build();
    }

    private URI toURI(String lraId) {
        return toURI(lraId, "Invalid LRA id format");
    }

    private URI toURI(String lraId, String message) {
        URL url;

        try {
            // see if it already in the correct format
            url = new URL(lraId);
            url.toURI();
        } catch (Exception e) {
            try {
                url = new URL(String.format("%s%s/%s", context.getBaseUri(), COORDINATOR_PATH_NAME, lraId));
            } catch (MalformedURLException e1) {
                LRALogger.i18NLogger.error_invalidStringFormatOfUrl(lraId, e1);
                throw new InvalidLRAIdException(lraId, message, e1);
            }
        }

        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            LRALogger.i18NLogger.error_invalidStringFormatOfUrl(lraId, e);
            throw new InvalidLRAIdException(lraId, message, e);
        }
    }
}
