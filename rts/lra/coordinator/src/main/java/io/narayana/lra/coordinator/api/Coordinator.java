/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.coordinator.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.narayana.lra.Current;
import io.narayana.lra.LRAConstants;
import io.narayana.lra.LRAData;
import io.narayana.lra.coordinator.domain.model.LongRunningAction;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import io.narayana.lra.logging.LRALogger;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static io.narayana.lra.LRAConstants.API_VERSION_1_0;
import static io.narayana.lra.LRAConstants.CLIENT_ID_PARAM_NAME;
import static io.narayana.lra.LRAConstants.COMPENSATE;
import static io.narayana.lra.LRAConstants.COMPLETE;
import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;
import static io.narayana.lra.LRAConstants.NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME;
import static io.narayana.lra.LRAConstants.PARENT_LRA_PARAM_NAME;
import static io.narayana.lra.LRAConstants.PARTICIPANT_TIMEOUT;
import static io.narayana.lra.LRAConstants.RECOVERY_COORDINATOR_PATH_NAME;
import static io.narayana.lra.LRAConstants.STATUS;
import static io.narayana.lra.LRAConstants.STATUS_PARAM_NAME;
import static io.narayana.lra.LRAConstants.TIMELIMIT_PARAM_NAME;
import static io.narayana.lra.LRAConstants.CURRENT_API_VERSION_STRING;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static jakarta.ws.rs.core.Response.Status.OK;
import static io.narayana.lra.LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

@ApplicationScoped
@ApplicationPath("/")
@Path(COORDINATOR_PATH_NAME)
@OpenAPIDefinition(
        info = @Info(title = "LRA Coordinator", version = LRAConstants.CURRENT_API_VERSION_STRING,
                contact = @Contact(name = "Narayana", url = "https://narayana.io")),
        tags = @Tag(name = "LRA Coordinator"),
        components = @Components(
                schemas = {
                        @Schema(name = "LRAApiVersionSchema",
                                description = "Format is `major.minor`, both components are required, they are to be numbers",
                                type = SchemaType.STRING, pattern = "^\\d+\\.\\d+$", example = "1.0")
                },
                parameters = {
                        @Parameter(name = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME, in = ParameterIn.HEADER,
                                description = "Narayana LRA API version", schema = @Schema(ref = "LRAApiVersionSchema"))
                },
                headers = {
                        @Header(name = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME, description = "Narayana LRA API version",
                                schema = @Schema(ref = "LRAApiVersionSchema"))
                }
        )
)
@Tag(name = "LRA Coordinator", description = "Operations to work with active LRAs (to start, to get a status, to finish, etc.)")
public class Coordinator extends Application {
    @Context
    private UriInfo context;

    private static final boolean allowParticipantData = initAllowParticipantData();

    private final LRAService lraService;
    private final RecoveryCoordinator recoveryCoordinator;

    public Coordinator() {
        lraService = LRARecoveryModule.getService();
        recoveryCoordinator = new RecoveryCoordinator();
    }

    @Path(RECOVERY_COORDINATOR_PATH_NAME)
    public RecoveryCoordinator getRecoveryCoordinator() {
        return recoveryCoordinator;
    }

    private static boolean initAllowParticipantData() {
        try {
            // We cannot inject it using @ConfigProperty(name = LRAConstants.ALLOW_PARTICIPANT_DATA,defaultValue = "true")
            // because CDI injection isn't guaranteed in JAX-RS Application classes
            return ConfigProvider.getConfig().getValue(LRAConstants.ALLOW_PARTICIPANT_DATA, Boolean.class);
        } catch (Exception e) {
            return true; // the property is unset or there is no config provider so use the default value
        }
    }

    private boolean isAllowParticipantData(String version) {
        // only protocol version API_VERSION_1_0 doesn't support participant data
        // and using a null version header is interpreted as meaning the caller doesn't care
        return (version == null) || (allowParticipantData && !version.equals(API_VERSION_1_0));
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Operation(summary = "Returns all LRAs", description = "Gets both active and recovering LRAs")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The LRAData json array which is known to coordinator",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = LRAData.class)),
            headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)}),
        @APIResponse(responseCode = "400", description = "Provided Status is not recognized as a valid LRA status value",
            content = @Content(schema = @Schema(implementation = String.class)),
            headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)}),
        @APIResponse(responseCode = "417", description = "The requested version provided in HTTP Header is not supported by this end point",
                content = @Content(schema = @Schema(implementation = String.class))),
    })
    public Response getAllLRAs(
            @Parameter(name = STATUS_PARAM_NAME, description = "Filter the returned LRAs to only those in the give state (see CompensatorStatus)")
            @QueryParam(STATUS_PARAM_NAME) @DefaultValue("") String state,
            @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.TEXT_PLAIN) String mediaType,
            @Parameter(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)
            @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version) {
        LRAStatus requestedLRAStatus = null;
        if (!state.isEmpty()) {
            try {
                requestedLRAStatus = LRAStatus.valueOf(state);
            } catch (IllegalArgumentException e) {
                String errorMsg = "Status " + state + " is not a valid LRAStatus value";
                LRALogger.logger.debugf(errorMsg);
                throw new WebApplicationException(errorMsg, e,
                        Response.status(BAD_REQUEST).header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version).entity(errorMsg).build());
            }
        }

        List<LRAData> lras = lraService.getAll(requestedLRAStatus);

        if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            try {
                String jsonArray = new ObjectMapper().writeValueAsString(lras);

                return Response.ok()
                        .entity(jsonArray)
                        .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
                        .build();
            } catch (JsonProcessingException e) {
                return Response.status(INTERNAL_SERVER_ERROR)
                        .entity(e.getMessage())
                        .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
                        .build();
            }
        } else { // produce MediaType.TEXT_PLAIN
            return Response.ok()
                    .entity(lras)
                    .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
                    .build();
        }
    }

    @GET
    @Path("{LraId}/status")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Operation(summary = "Obtain the status of an LRA as a string")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The LRA exists. The status is reported in the content body.",
            content = @Content(schema = @Schema(implementation = String.class)),
            headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
        @APIResponse(responseCode = "404", description = "The coordinator has no knowledge of this LRA",
            content = @Content(schema = @Schema(implementation = String.class))),
        @APIResponse(responseCode = "417", description = "The requested version provided in HTTP Header is not supported by this end point",
                content = @Content(schema = @Schema(implementation = String.class))),
    })
    public Response getLRAStatus(
        @Parameter(name = "LraId", description = "The unique identifier of the LRA." +
                "Expecting to be a valid URL where the participant can be contacted at. If not in URL format it will be considered " +
                "to be an id which will be declared to exist at URL where coordinator is deployed at.", required = true)
        @PathParam("LraId")String lraId,
        @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.TEXT_PLAIN) String mediaType,
        @Parameter(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)
        @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version)
            throws NotFoundException {
        LongRunningAction transaction = lraService.getTransaction(toURI(lraId)); // throws NotFoundException -> response 404
        LRAStatus status = transaction.getLRAStatus();

        if (status == null) {
            status = LRAStatus.Active;
        }

        if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            JsonObject model = Json.createObjectBuilder().add("status", status.name()).build();

            return Response.ok()
                    .entity(model)
                    .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version).build();
        }

        return Response.ok()
            .entity(status.name())
            .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version).build();
    }

    @GET
    @Path("{LraId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Operation(summary = "Obtain the information about an LRA as a JSON structure")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The LRA exists and the information is packed as JSON in the content body.",
            content = @Content(schema = @Schema(implementation = LRAData.class)),
            headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
        @APIResponse(responseCode = "404", description = "The coordinator has no knowledge of this LRA",
            content = @Content(schema = @Schema(implementation = String.class))),
        @APIResponse(responseCode = "417", description = "The requested version provided in HTTP Header is not supported by this end point",
                content = @Content(schema = @Schema(implementation = String.class))),
    })
    public Response getLRAInfo(
            @Parameter(name = "LraId", description = "The unique identifier of the LRA", required = true)
            @PathParam("LraId") String lraId,
            @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.TEXT_PLAIN) String mediaType,
            @Parameter(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)
            @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version) {
        URI lraIdURI = toURI(lraId);
        LRAData lraData = lraService.getLRA(lraIdURI);
        return Response.status(OK).entity(lraData)
                .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version).build();
    }

    /**
     * Performing a POST on {@value LRAConstants#COORDINATOR_PATH_NAME}/start?ClientID={ClientID}
     * will start a new lra with a default timeout and return an LRA URL
     * of the form {coordinator url}/{@value LRAConstants#COORDINATOR_PATH_NAME}/{LraId}.
     * Adding a query parameter, {@value LRAConstants#TIMELIMIT_PARAM_NAME}={timeout}, will start a new lra with the specified timeout.
     * If the lra is terminated because of a timeout, the lra URL is deleted and all further invocations on the URL will return 404.
     * The invoker can assume this was equivalent to a compensation operation.
     */
    @POST
    @Path("start")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Bulkhead
    @Operation(summary = "Start a new LRA",
        description = "The LRA model uses a presumed nothing protocol: the coordinator must communicate "
            + "with participants in order to inform them of the LRA activity. Every time a "
            + "Compensator is enrolled with an LRA, the coordinator must make information about "
            + "it durable so that the Compensator can be contacted when the LRA terminates, "
            + "even in the event of subsequent failures. Participants, clients and coordinators "
            + "cannot make any presumption about the state of the global transaction without "
            + "consulting the coordinator and all participants, respectively.")
    @APIResponses({
        @APIResponse(responseCode = "201",
            description = "The request was successful and the response body contains the id of the new LRA",
            content = @Content(schema = @Schema(description = "An URI of the new LRA", implementation = String.class)),
            headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
        @APIResponse(responseCode = "404", description = "Parent LRA id cannot be joint to the started LRA",
            content = @Content(schema = @Schema(description = "Message containing problematic LRA id", implementation = String.class))),
        @APIResponse(responseCode = "417", description = "The requested version provided in HTTP Header is not supported by this end point",
                content = @Content(schema = @Schema(implementation = String.class))),
        @APIResponse(responseCode = "500", description = "A new LRA could not be started. Coordinator internal error.",
                content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Response startLRA(
            @Parameter(name = CLIENT_ID_PARAM_NAME,
                description = "Each client is expected to have a unique identity (which can be a URL).",
                required = true)
            @QueryParam(CLIENT_ID_PARAM_NAME) @DefaultValue("") String clientId,
            @Parameter(name = TIMELIMIT_PARAM_NAME,
                description = "Specifies the maximum time in milli seconds that the LRA will exist for.\n"
                    + "If the LRA is terminated because of a timeout, the LRA URL is deleted.\n"
                    + "All further invocations on the URL will return 404.\n"
                    + "The invoker can assume this was equivalent to a compensate operation.")
            @QueryParam(TIMELIMIT_PARAM_NAME) @DefaultValue("0") Long timelimit,
            @Parameter(name = PARENT_LRA_PARAM_NAME,
                description = "The enclosing LRA if this new LRA is nested")
            @QueryParam(PARENT_LRA_PARAM_NAME) @DefaultValue("") String parentLRA,
            @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.TEXT_PLAIN) String mediaType,
            @Parameter(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)
            @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version) throws WebApplicationException {

        URI parentId = (parentLRA == null || parentLRA.trim().isEmpty()) ? null : toURI(parentLRA);
        String coordinatorUrl = String.format("%s%s", context.getBaseUri(), COORDINATOR_PATH_NAME);
        LongRunningAction lra = lraService.startLRA(coordinatorUrl, parentId, clientId, timelimit);
        URI lraId = lra.getId();

        if (parentId != null) {
            // the startLRA call will have imported the parent LRA
            String compensatorUrl = String.format("%s/nested/%s", coordinatorUrl, LRAConstants.getLRAUid(lraId));

            if (!lraService.hasTransaction(parentId)) {

                try (Client client = ClientBuilder.newClient()) {
                    try (Response response = client.target(parentId)
                            .request()
                            .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, CURRENT_API_VERSION_STRING)
                            .async()
                            .put(Entity.text(compensatorUrl))
                            .get(PARTICIPANT_TIMEOUT, TimeUnit.SECONDS)) {

                        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                            String errMessage = String.format("The coordinator at %s returned an unexpected response: %d"
                                    + "when the LRA '%s' tried to join the parent LRA '%s'", parentId, response.getStatus(), lraId, parentLRA);
                            return Response.status(response.getStatus()).entity(errMessage).build();
                        }
                    }
                } catch (Exception e) {
                    String errorMsg = String.format("Cannot contact the LRA Coordinator at '%s' for LRA '%s' joining parent LRA '%s'",
                            parentId, lraId, parentLRA);
                    LRALogger.logger.debugf(errorMsg);
                    throw new WebApplicationException(errorMsg, e,
                            Response.status(INTERNAL_SERVER_ERROR).header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
                                    .entity(errorMsg).build());
                }
            }
        }

        Current.push(lraId);

        if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            JsonObject model = Json.createObjectBuilder().add("lraId", lraId.toASCIIString()).build();

            return Response.ok()
                    .entity(model)
                    .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version).build();
        }

        return Response.created(lraId)
                .entity(lraId.toASCIIString())
                .header(LRA_HTTP_CONTEXT_HEADER, Current.getContexts())
                .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
                .build();
    }

    @PUT
    @Path("{LraId}/renew")
    @Operation(summary = "Update the TimeLimit for an existing LRA",
        description = "LRAs can be automatically cancelled if they aren't closed or cancelled before the TimeLimit "
            + "specified at creation time is reached. The time limit can be updated.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "If the LRA time limit has been updated",
            content = @Content(schema = @Schema(implementation = String.class)),
            headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
        @APIResponse(responseCode = "404", description = "The coordinator has no knowledge of this LRA or " +
            "the LRA is not longer active (ie the complete or compensate messages have been sent",
            content = @Content(schema = @Schema(implementation = String.class)),
            headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
        @APIResponse(responseCode = "417", description = "The requested version provided in HTTP Header is not supported by this end point",
                content = @Content(schema = @Schema(implementation = String.class))),
    })
    public Response renewTimeLimit(
            @Parameter(name = "LraId", description = "The unique identifier of the LRA", required = true)
            @PathParam("LraId") String lraId,
            @Parameter(name = TIMELIMIT_PARAM_NAME, description = "The new time limit for the LRA", required = true)
            @QueryParam(TIMELIMIT_PARAM_NAME) @DefaultValue("0") Long timeLimit,
            @Parameter(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)
            @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version) {
        return Response.status(lraService.renewTimeLimit(toURI(lraId), timeLimit))
            .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
            .entity(lraId)
            .build();
    }

    @GET
    @Path("nested/{NestedLraId}/status")
    public Response getNestedLRAStatus(@PathParam("NestedLraId")String nestedLraId) {
        if (!lraService.hasTransaction(nestedLraId)) {
            // it must have compensated
            return Response.ok(ParticipantStatus.Compensated.name()).build();
        }

        LongRunningAction lra = lraService.getTransaction(toURI(nestedLraId));
        LRAStatus status = lra.getLRAStatus();

        if (status == null || lra.getLRAStatus() == null) {
            String logMsg = LRALogger.i18nLogger.error_cannotGetStatusOfNestedLraURI(nestedLraId, lra.getId());
            LRALogger.logger.debug(logMsg);
            throw new WebApplicationException(logMsg,
                    Response.status(Response.Status.PRECONDITION_FAILED).entity(logMsg).build());
        }

        return Response.ok(mapToParticipantStatus(lra.getLRAStatus()).name()).build();
    }

    private ParticipantStatus mapToParticipantStatus(LRAStatus lraStatus) {
        switch (lraStatus) {
            case Active: return ParticipantStatus.Active;
            case Closed: return ParticipantStatus.Completed;
            case Cancelled: return ParticipantStatus.Compensated;
            case Closing: return ParticipantStatus.Completing;
            case Cancelling: return ParticipantStatus.Compensating;
            case FailedToClose: return ParticipantStatus.FailedToComplete;
            case FailedToCancel: return ParticipantStatus.FailedToCompensate;
            default: throw new RuntimeException("Invalid LRAStatus enum value: " + lraStatus);
        }
    }

    @PUT
    @Path("nested/{NestedLraId}/complete")
    public Response completeNestedLRA(
            @PathParam("NestedLraId") String nestedLraId,
            @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.TEXT_PLAIN) String mediaType,
            @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version) {

        LRAData lraData = lraService.endLRA(toURI(nestedLraId), false, true, null, null);

        return buildResponse(mapToParticipantStatus(lraData.getStatus()).name(), version, mediaType);
    }

    @PUT
    @Path("nested/{NestedLraId}/compensate")
    public Response compensateNestedLRA(
            @PathParam("NestedLraId") String nestedLraId,
            @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.TEXT_PLAIN) String mediaType,
            @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version) {

        LRAData lraData = lraService.endLRA(toURI(nestedLraId), true, true, null, null);

        return buildResponse(mapToParticipantStatus(lraData.getStatus()).name(), version, mediaType);
    }

    @PUT
    @Path("nested/{NestedLraId}/forget")
    public Response forgetNestedLRA(@PathParam("NestedLraId") String nestedLraId) {
        lraService.remove(toURI(nestedLraId));

        return Response.ok().build();
    }

    /**
     * Performing a PUT on {@value LRAConstants#COORDINATOR_PATH_NAME}/{LraId}/close will trigger the successful completion
     * of the LRA and all participants will be dropped by the LRA Coordinator.
     * The complete message will be sent to the participants.
     * Upon termination, the URL is implicitly deleted. If it no longer exists, then 404 will be returned.
     * The invoker cannot know for sure whether the lra completed or compensated without enlisting a participant.
     */
    @PUT
    @Path("{LraId}/close")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Operation(summary = "Attempt to close an LRA",
        description = "Trigger the successful completion of the LRA. All"
            + " participants will be dropped by the coordinator."
            + " The complete message will be sent to the participants."
            + " Upon termination, the URL is implicitly deleted."
            + " The invoker cannot know for sure whether the lra completed"
            + " or compensated without enlisting a participant.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "The complete message was sent to all coordinators",
                content = @Content(schema = @Schema(implementation = String.class)),
                headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
            @APIResponse(responseCode = "404", description = "The coordinator has no knowledge of this LRA",
                    content = @Content(schema = @Schema(implementation = String.class)),
                    headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
            @APIResponse(responseCode = "417", description = "The requested version provided in HTTP Header is not supported by this end point",
                    content = @Content(schema = @Schema(implementation = String.class))),
    })
    public Response closeLRA(
            @Parameter(name = "LraId", description = "The unique identifier of the LRA", required = true)
            @PathParam("LraId") String lraId,
            @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.TEXT_PLAIN) String mediaType,
            @Parameter(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)
            @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version,
            @HeaderParam(LRAConstants.NARAYANA_LRA_PARTICIPANT_LINK_HEADER_NAME) @DefaultValue("") String compensator,
            @HeaderParam(LRAConstants.NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME) @DefaultValue("") String userData) {

        LRAData lraData = lraService.endLRA(toURI(lraId), false, false, compensator, userData);

        return buildResponse(lraData.getStatus().name(), version, mediaType);
    }

    @PUT
    @Path("{LraId}/cancel")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Operation(summary = "Attempt to cancel an LRA",
        description = " Trigger the compensation of the LRA. All"
            + " participants will be triggered by the coordinator (ie the compensate message will be sent to each participants)."
            + " Upon termination, the URL is implicitly deleted."
            + " The invoker cannot know for sure whether the lra completed or compensated without enlisting a participant.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The compensate message was sent to all coordinators",
            content = @Content(schema = @Schema(implementation = String.class)),
            headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
        @APIResponse(responseCode = "404", description = "The coordinator has no knowledge of this LRA",
            content = @Content(schema = @Schema(implementation = String.class)),
            headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
        @APIResponse(responseCode = "417", description = "The requested version provided in HTTP Header is not supported by this end point",
                content = @Content(schema = @Schema(implementation = String.class))),
    })
    public Response cancelLRA(
        @Parameter(name = "LraId", description = "The unique identifier of the LRA", required = true)
        @PathParam("LraId")String lraId,
        @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.TEXT_PLAIN) String mediaType,
        @Parameter(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)
        @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version,
        @HeaderParam(LRAConstants.NARAYANA_LRA_PARTICIPANT_LINK_HEADER_NAME) @DefaultValue("") String compensator,
        @HeaderParam(LRAConstants.NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME) @DefaultValue("") String userData)
            throws NotFoundException {

        LRAData lraData = lraService.endLRA(toURI(lraId), true, false, compensator, userData);

        return buildResponse(lraData.getStatus().name(), version, mediaType);
    }

    @PUT
    @Path("{LraId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Operation(summary = "A Compensator can join with the LRA at any time prior to the completion of an activity")
    @APIResponses({
        @APIResponse(responseCode = "200",
            description = "The participant was successfully registered with the LRA",
            content = @Content(schema = @Schema(description = "A URI representing the recovery id of this join request",implementation = String.class)),
            headers = {
                @Header(name = LRA_HTTP_RECOVERY_HEADER, description = "It contains a unique resource reference for that participant:\n"
                        + " - HTTP GET on the reference returns the original participant URL;\n" // Note that isn't a test for this
                        + " - HTTP PUT on the reference will overwrite the old participant URL with the new one supplied.",
                    schema = @Schema(implementation = String.class)),
                @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
        @APIResponse(responseCode = "400", description = "Link does not contain all required fields for joining the LRA. " +
                "Probably no compensator or after 'rel' is available.",
            content = @Content(schema = @Schema(implementation = String.class))),
        @APIResponse(responseCode = "404", description = "The coordinator has no knowledge of this LRA",
            content = @Content(schema = @Schema(implementation = String.class))),
        @APIResponse(responseCode = "412",
            description = "The LRA is not longer active (ie the complete or compensate message has been sent), or wrong format of compensator data",
            content = @Content(schema = @Schema(implementation = String.class)),
            headers = {@Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)}),
        @APIResponse(responseCode = "417", description = "The requested version provided in HTTP Header is not supported by this end point",
            content = @Content(schema = @Schema(implementation = String.class))),
        @APIResponse(responseCode = "500", description = "Format of the compensator data (e.g. Link format) could not be processed",
            content = @Content(schema = @Schema(implementation = String.class))),
    })
    public Response joinLRAViaBody(
            @Parameter(name = "LraId", description = "The unique identifier of the LRA", required = true)
            @PathParam("LraId")String lraId,
            @Parameter(name = TIMELIMIT_PARAM_NAME,
                description = "The time limit in milliseconds that the Compensator can guarantee that it can compensate "
                    + "the work performed by the service. After this time period has elapsed, it may no longer be "
                    + "possible to undo the work within the scope of this (or any enclosing) LRA. It may therefore "
                    + "be necessary for the application or service to start other activities to explicitly try to "
                    + "compensate this work. The application or coordinator may use this information to control the "
                    + "lifecycle of an LRA.")
            @QueryParam(TIMELIMIT_PARAM_NAME) @DefaultValue("0") long timeLimit,
            @Parameter(name = "Link",
                description = "The resource paths that the coordinator will use to complete or compensate and to request"
                    + " the status of the participant. The link rel names are"
                    + " complete, compensate and status.")
            @HeaderParam("Link") @DefaultValue("") String compensatorLink,
            @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.TEXT_PLAIN) String mediaType,
            @Parameter(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)
            @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version,
            @HeaderParam(LRAConstants.NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME) @DefaultValue("") String userData,
            @RequestBody(name = "Compensator data",
                description = "A compensator can also register with an LRA by putting the compensator end "
                    + "points in the body of request as a link header. This feature is deprecated and undocumented "
                    + "and will be removed in a later version of the protocol") String compensatorURL) throws NotFoundException {

        // test to see if the join request contains any participant specific data
        if (userData != null && !userData.isEmpty() && !isAllowParticipantData(version)) {
            String logMsg = LRALogger.i18nLogger.error_participant_data_disallowed(lraId);
            LRALogger.logger.error(logMsg);

            throw new WebApplicationException(logMsg,
                    Response.status(PRECONDITION_FAILED).entity(logMsg)
                            .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
                            .build());
        }

        // test to see if the compensator endpoints are in the body of the join request
        boolean isLink = isLink(compensatorURL);

        if (compensatorLink != null && !compensatorLink.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            if (userData != null) {
                sb.append(userData);
            }

            return joinLRA(toURI(lraId), mediaType, timeLimit, compensatorLink, sb, version);
        }

        if (!isLink && !compensatorURL.isEmpty()) {
            // interpret the content as a standard participant <url> with the convention that
            // <url>/compensate, <url>/complete and <url>/status are the endpoints for compensating,
            // completing and status reporting (this was the protocol in the early prototype and
            // is deprecated (see issue JBTM-1488 Implement the REST-JDI specification)
            compensatorURL += "/";

            Map<String, String> terminateURIs = new HashMap<>();

            try {
                terminateURIs.put(COMPENSATE, new URL(compensatorURL + "compensate").toExternalForm());
                terminateURIs.put(COMPLETE, new URL(compensatorURL + "complete").toExternalForm());
                terminateURIs.put(STATUS, new URL(compensatorURL + "status").toExternalForm());
            } catch (MalformedURLException e) {
                String errorMsg = String.format("Cannot join to LRA id '%s' with body as compensator url '%s' is invalid",
                        lraId, compensatorURL);
                if (LRALogger.logger.isTraceEnabled()) {
                    LRALogger.logger.trace(errorMsg, e);
                }

                return Response.status(PRECONDITION_FAILED)
                        .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
                        .entity(errorMsg)
                        .build();
            }

            // register with the coordinator, put the lra id in an HTTP header
            StringBuilder linkHeaderValue = new StringBuilder();

            terminateURIs.forEach((k, v) -> makeLink(linkHeaderValue, k, v)); // or use Collectors.joining(",")

            compensatorURL = linkHeaderValue.toString();
        }

        return joinLRA(toURI(lraId), mediaType, timeLimit, compensatorURL, null, version);
    }


    private static void makeLink(StringBuilder b, String key, String value) {

        if (value != null) {

            Link link = Link.fromUri(value).rel(key).type(MediaType.TEXT_PLAIN).build();

            if (b.length() != 0) {
                b.append(',');
            }

            b.append(link);
        }
    }

    private boolean isLink(String linkString) {
        try {
            Link.valueOf(linkString);

            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Response joinLRA(URI lraId, String acceptMediaType, long timeLimit, String linkHeader,
                             StringBuilder userData, String version)
            throws NotFoundException {
        final String recoveryUrlBase = String.format("%s%s/%s",
                context.getBaseUri().toASCIIString(), COORDINATOR_PATH_NAME, RECOVERY_COORDINATOR_PATH_NAME);

        if (userData == null) {
            userData = new StringBuilder();
        }

        StringBuilder recoveryUrl = new StringBuilder();
        int status = lraService.joinLRA(recoveryUrl, lraId, timeLimit, null, linkHeader, recoveryUrlBase, userData, version);
        String recoveryUrlValue;

        if (acceptMediaType.equals(MediaType.APPLICATION_JSON)) {
            JsonObject model = Json.createObjectBuilder().add("recoveryUrl", recoveryUrl.toString()).build();
            recoveryUrlValue = model.toString();
        } else {
            recoveryUrlValue = recoveryUrl.toString();
        }

        try {
            return Response.status(status)
                    .entity(recoveryUrlValue)
                    .location(new URI(recoveryUrl.toString()))
                    .header(LRA_HTTP_RECOVERY_HEADER, recoveryUrl)
                    .header(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME, userData)
                    .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
                    .build();
        } catch (URISyntaxException e) {
            String logMsg = LRALogger.i18nLogger.error_invalidRecoveryUrlToJoinLRAURI(recoveryUrl.toString(), lraId);
            LRALogger.logger.error(logMsg);
            throw new WebApplicationException(logMsg, e,
                    Response.status(INTERNAL_SERVER_ERROR).entity(logMsg)
                            .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
                            .build());
        }
    }
    /**
     * A participant can resign from an LRA at any time prior to the completion of an activity by performing a
     * PUT on {@value LRAConstants#COORDINATOR_PATH_NAME}/{LraId}/remove with the URL of the participant.
     */
    @PUT
    @Path("{LraId}/remove")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Operation(summary = "A Compensator can resign from the LRA at any time prior to the completion of an activity")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "If the participant was successfully removed from the LRA",
            headers = { @Header(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) }),
        @APIResponse(responseCode = "400", description = "The coordinator has no knowledge of this participant compensator URL",
            content = @Content(schema = @Schema(implementation = String.class))),
        @APIResponse(responseCode = "404", description = "The coordinator has no knowledge of this LRA",
                content = @Content(schema = @Schema(implementation = String.class))),
        @APIResponse(responseCode = "412",
            description = "The LRA is not longer active (ie in the complete or compensate messages have been sent"),
        @APIResponse(responseCode = "417", description = "The requested version provided in HTTP Header is not supported by this end point",
                content = @Content(schema = @Schema(implementation = String.class))),
    })
    public Response leaveLRA(
            @Parameter(name = "LraId", description = "The unique identifier of the LRA", required = true)
            @PathParam("LraId") String lraId,
            @HeaderParam(HttpHeaders.ACCEPT) @DefaultValue(MediaType.TEXT_PLAIN) String mediaType,
            @Parameter(ref = LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME)
            @HeaderParam(LRAConstants.NARAYANA_LRA_API_VERSION_HEADER_NAME) @DefaultValue(CURRENT_API_VERSION_STRING) String version,
            String participantCompensatorUrl) throws NotFoundException {
        int status = lraService.leave(toURI(lraId), participantCompensatorUrl);

        return Response.status(status)
                .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, version)
                .build();
    }

    private Response buildResponse(String status, String apiVersion, String mediaType) throws NotFoundException {
        if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            JsonObject model = Json.createObjectBuilder()
                    .add("status", status)
                    .build();

            return Response.ok(model.toString())
                    .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, apiVersion)
                    .build();
        } else { // produce MediaType.TEXT_PLAIN
            return Response.ok(status)
                    .header(NARAYANA_LRA_API_VERSION_HEADER_NAME, apiVersion)
                    .build();
        }
    }

    private URI toURI(String lraId) {
        URL url;

        try {
            // see if it already in the correct format
            url = new URL(lraId);
            url.toURI();
        } catch (Exception e) {
            try {
                url = new URL(String.format("%s%s/%s", context.getBaseUri(), COORDINATOR_PATH_NAME, lraId));
            } catch (MalformedURLException e1) {
                String logMsg = LRALogger.i18nLogger.error_invalidStringFormatOfUrl(lraId, e1);
                LRALogger.logger.error(logMsg);
                throw new WebApplicationException(logMsg, e1,
                        Response.status(BAD_REQUEST).entity(logMsg).build());
            }
        }

        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            String logMsg = LRALogger.i18nLogger.error_invalidStringFormatOfUrl(lraId, e);
            LRALogger.logger.error(logMsg);
            throw new WebApplicationException(logMsg, e,
                    Response.status(BAD_REQUEST).entity(logMsg).build());
        }
    }
}
