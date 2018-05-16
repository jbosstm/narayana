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
package io.narayana.lra.participant.api;

import io.narayana.lra.logging.LRALogger;
import io.narayana.lra.participant.service.ActivityService;

import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.lra.participant.model.Activity;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRA;
import org.eclipse.microprofile.lra.annotation.Leave;
import org.eclipse.microprofile.lra.annotation.NestedLRA;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.TimeLimit;
import org.eclipse.microprofile.lra.client.GenericLRAException;
import org.eclipse.microprofile.lra.client.IllegalLRAStateException;
import org.eclipse.microprofile.lra.client.InvalidLRAIdException;
import org.eclipse.microprofile.lra.client.LRAClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static io.narayana.lra.client.NarayanaLRAClient.LRA_HTTP_HEADER;
import static io.narayana.lra.client.NarayanaLRAClient.LRA_HTTP_RECOVERY_HEADER;
import static io.narayana.lra.participant.api.ActivityController.ACTIVITIES_PATH;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@ApplicationScoped
@Path(ACTIVITIES_PATH)
@LRA(LRA.Type.SUPPORTS)
public class ActivityController {
    public static final String ACTIVITIES_PATH = "activities";
    public static final String ACCEPT_WORK = "acceptWork";

    private static final AtomicInteger completedCount = new AtomicInteger(0);
    private static final AtomicInteger compensatedCount = new AtomicInteger(0);

    @Inject
    private LRAClient lraClient;

    @Context
    private UriInfo context;

    @Inject
    private ActivityService activityService;

    /**
     Performing a GET on the participant URL will return the current status of the participant {@link CompensatorStatus}, or 404 if the participant is no longer present.
     */
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Status
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Response status(@HeaderParam(LRA_HTTP_HEADER) String lraId) throws NotFoundException {
        Activity activity = activityService.getActivity(lraId);

        if (activity.status == null)
            throw new IllegalLRAStateException(lraId, "LRA is not active", "getStatus");

        if (activity.getAndDecrementAcceptCount() <= 0) {
            if (activity.status == CompensatorStatus.Completing)
                activity.status = CompensatorStatus.Completed;
            else if (activity.status == CompensatorStatus.Compensating)
                activity.status = CompensatorStatus.Compensated;
        }

        return Response.ok(activity.status.name()).build();
    }

    /**
     * Test that participants can leave an LRA using the {@link LRAClient} programatic API
     * @param lraUrl the LRA that the participant should leave
     * @return the id of the LRA that was left
     * @throws NotFoundException if the requested LRA does not exist
     */
    @PUT
    @Path("/leave/{LraUrl}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response leaveWorkViaAPI(@PathParam("LraUrl")String lraUrl) throws NotFoundException, MalformedURLException {

        if (lraUrl != null) {
            Map<String, String> terminateURIs = NarayanaLRAClient.getTerminationUris(this.getClass(), context.getBaseUri());
            lraClient.leaveLRA(new URL(lraUrl), terminateURIs.get("Link"));

            activityService.getActivity(lraUrl);

            activityService.remove(lraUrl);

            return Response.ok(lraUrl).build();
        }

        return Response.ok("non transactional").build();
    }

    @PUT
    @Path("/leave")
    @Produces(MediaType.APPLICATION_JSON)
    @Leave
    public Response leaveWork(@HeaderParam(LRA_HTTP_HEADER) String lraId) throws NotFoundException {
        if (lraId != null) {
            activityService.getActivity(lraId);

            activityService.remove(lraId);

            return Response.ok(lraId).build();
        }

        return Response.ok("non transactional").build();
    }

    @PUT
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeWork(@HeaderParam(LRA_HTTP_HEADER) String lraId, String userData) throws NotFoundException {
        completedCount.incrementAndGet();

        assert lraId != null;

        Activity activity = activityService.getActivity(lraId);

        activity.setEndData(userData);

        if (activity.getAndDecrementAcceptCount() > 0) {
            activity.status = CompensatorStatus.Completing;
            activity.statusUrl = String.format("%s/%s/%s/status", context.getBaseUri(), ACTIVITIES_PATH, lraId);

            return Response.accepted().location(URI.create(activity.statusUrl)).build();
        }

        activity.status = CompensatorStatus.Completed;
        activity.statusUrl = String.format("%s/%s/activity/completed", context.getBaseUri(), lraId);

        System.out.printf("ActivityController completing %s%n", lraId);
        return Response.ok(activity.statusUrl).build();
    }

    @PUT
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response compensateWork(@HeaderParam(LRA_HTTP_HEADER) String lraId, String userData) throws NotFoundException {
        compensatedCount.incrementAndGet();

        assert lraId != null;

        Activity activity = activityService.getActivity(lraId);

        activity.setEndData(userData);

        if (activity.getAndDecrementAcceptCount() > 0) {
            activity.status = CompensatorStatus.Compensating;
            activity.statusUrl = String.format("%s/%s/%s/status", context.getBaseUri(), ACTIVITIES_PATH, lraId);

            return Response.accepted().location(URI.create(activity.statusUrl)).build();
        }

        activity.status = CompensatorStatus.Compensated;
        activity.statusUrl = String.format("%s/%s/activity/compensated", context.getBaseUri(), lraId);

        System.out.printf("ActivityController compensating %s%n", lraId);
        return Response.ok(activity.statusUrl).build();
    }

    @DELETE
    @Path("/forget")
    @Produces(MediaType.APPLICATION_JSON)
    @Forget
    public Response forgetWork(@HeaderParam(LRA_HTTP_HEADER) String lraId) {//throws NotFoundException {
        completedCount.incrementAndGet();

        assert lraId != null;

        Activity activity = activityService.getActivity(lraId);

        activityService.remove(activity.id);
        activity.status = CompensatorStatus.Completed;
        activity.statusUrl = String.format("%s/%s/activity/completed", context.getBaseUri(), lraId);

        System.out.printf("ActivityController forgetting %s%n", lraId);
        return Response.ok(activity.statusUrl).build();
    }

    @PUT
    @Path(ACCEPT_WORK)
    @LRA(LRA.Type.REQUIRED)
    public Response acceptWork(
            @HeaderParam(LRA_HTTP_RECOVERY_HEADER) String rcvId,
            @HeaderParam(LRA_HTTP_HEADER) String lraId) {
        assert lraId != null;
        Activity activity = addWork(lraId, rcvId);

        if (activity == null)
            return Response.status(Response.Status.EXPECTATION_FAILED).entity("Missing lra data").build();

        activity.setAcceptedCount(1); // tests that it is possible to asynchronously complete
        return Response.ok(lraId).build();
    }

    @PUT
    @Path("/supports")
    @LRA(LRA.Type.SUPPORTS)
    public Response supportsLRACall(@HeaderParam(LRA_HTTP_HEADER) String lraId) {
        assert lraId != null;
        addWork(lraId, null);

        return Response.ok(lraId).build();
    }

    @PUT
    @Path("/startViaApi")
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Response subActivity(@HeaderParam(LRA_HTTP_HEADER) String lraId) {
        if (lraId != null)
            throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);

        // manually start an LRA via the injection LRAClient api
        URL lra = lraClient.startLRA("subActivity", 0L, TimeUnit.SECONDS);

        lraId = lra.toString();

        addWork(lraId, null);

        // invoke a method that SUPPORTS LRAs. The filters should detect the LRA we just started via the injected client
        // and add it as a header before calling the method at path /supports (ie supportsLRACall()).
        // The supportsLRACall method will return LRA id in the body if it is present.
        String id = restPutInvocation(lra,"supports", "");

        // check that the invoked method saw the LRA
        if (id == null || !lraId.equals(id))
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Entity.text("Unequal LRA ids")).build();

        return Response.ok(id).build();
    }

    @PUT
    @Path("/work")
    @LRA(LRA.Type.REQUIRED)
    public Response activityWithLRA(@HeaderParam(LRA_HTTP_RECOVERY_HEADER) String rcvId,
                                    @HeaderParam(LRA_HTTP_HEADER) String lraId) {
        assert lraId != null;
        Activity activity = addWork(lraId, rcvId);

        if (activity == null)
            return Response.status(Response.Status.EXPECTATION_FAILED).entity("Missing lra data").build();

        return Response.ok(lraId).build();
    }

    private String restPutInvocation(URL lraURL, String path, String bodyText) {
        String id = null;
        Response response = ClientBuilder.newClient()
            .target(context.getBaseUri())
            .path("activities")
            .path(path)
            .request()
            .header(LRAClient.LRA_HTTP_HEADER, lraURL)
            .put(Entity.text(bodyText));

        if (response.hasEntity())
            id = response.readEntity(String.class);

        checkStatusAndClose(response, Response.Status.OK.getStatusCode());

        return id;
    }

    @PUT
    @Path("/nestedActivity")
    @LRA(LRA.Type.MANDATORY)
    @NestedLRA
    public Response nestedActivity(@HeaderParam(LRA_HTTP_RECOVERY_HEADER) String rcvId,
                                   @HeaderParam(LRA_HTTP_HEADER) String nestedLRAId) {
        assert nestedLRAId != null;
        Activity activity = addWork(nestedLRAId, rcvId);

        if (activity == null)
            return Response.status(Response.Status.EXPECTATION_FAILED).entity("Missing lra data").build();

        return Response.ok(nestedLRAId).build();
    }

    @PUT
    @Path("/multiLevelNestedActivity")
    @LRA(LRA.Type.MANDATORY)
    public Response multiLevelNestedActivity(
            @HeaderParam(LRA_HTTP_RECOVERY_HEADER) String rcvId,
            @HeaderParam(LRA_HTTP_HEADER) String nestedLRAId,
            @QueryParam("nestedCnt") @DefaultValue("1") Integer nestedCnt) {
        assert nestedLRAId != null;
        Activity activity = addWork(nestedLRAId, rcvId);

        if (activity == null)
            return Response.status(Response.Status.EXPECTATION_FAILED).entity("Missing lra data").build();

        URL lraURL;

        try {
            lraURL = new URL(URLDecoder.decode(nestedLRAId, "UTF-8"));
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new InvalidLRAIdException(nestedLRAId, e.getMessage(), e);
        }

        // invoke resources that enlist nested LRAs
        String[] lras = new String[nestedCnt + 1];
        lras[0] = nestedLRAId;
        IntStream.range(1, lras.length).forEach(i -> lras[i] = restPutInvocation(lraURL,"nestedActivity", ""));

        return Response.ok(String.join(",", lras)).build();
    }

    private Activity addWork(String lraId, String rcvId) {
        assert lraId != null;
//        String txId = NarayanaLRAClient.getLRAId(lraId);

        System.out.printf("ActivityController: work id %s and rcvId %s %n", lraId, rcvId);

        try {
            return activityService.getActivity(lraId);
        } catch (NotFoundException e) {
            Activity activity = new Activity(lraId);

            activity.rcvUrl = rcvId;
            activity.status = null;

            activityService.add(activity);

            return activity;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Response findAll() {
        List<Activity> results = activityService.findAll();

        return Response.ok(results.size()).build();
    }

    @GET
    @Path("/completedactivitycount")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Response getCompleteCount() {
        return Response.ok(completedCount.get()).build();
    }
    @GET
    @Path("/compensatedactivitycount")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Response getCompensatedCount() {
        return Response.ok(compensatedCount.get()).build();
    }

    @GET
    @Path("/cancelOn")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(value = LRA.Type.REQUIRED, cancelOn = {Response.Status.NOT_FOUND, Response.Status.BAD_REQUEST})
    public Response cancelOn(@HeaderParam(LRA_HTTP_HEADER) String lraId) {
        activityService.add(new Activity(lraId));//NarayanaLRAClient.getLRAId(lraId)));

        return Response.status(Response.Status.BAD_REQUEST).entity(Entity.text("Simulate buisiness logic failure")).build();
    }

    @GET
    @Path("/cancelOnFamily")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(value = LRA.Type.REQUIRED, cancelOnFamily = {Response.Status.Family.CLIENT_ERROR})
    public Response cancelOnFamily(@HeaderParam(LRA_HTTP_HEADER) String lraId) {
        activityService.add(new Activity(lraId));//NarayanaLRAClient.getLRAId(lraId)));

        return Response.status(Response.Status.BAD_REQUEST).entity(Entity.text("Simulate buisiness logic failure")).build();
    }

    @GET
    @Path("/timeLimit")
    @Produces(MediaType.APPLICATION_JSON)
    @TimeLimit(limit = 100, unit = TimeUnit.MILLISECONDS)
    @LRA(value = LRA.Type.REQUIRED)
    public Response timeLimit(@HeaderParam(LRA_HTTP_HEADER) String lraId) {
        activityService.add(new Activity(lraId));//NarayanaLRAClient.getLRAId(lraId)));

        try {
            Thread.sleep(300); // sleep for 200 miliseconds (should be longer than specified in the @TimeLimit annotation)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.OK).entity(Entity.text("Simulate buisiness logic timeoout")).build();
    }

    @GET
    @Path("/renewTimeLimit")
    @Produces(MediaType.APPLICATION_JSON)
    @TimeLimit(limit = 100, unit = TimeUnit.MILLISECONDS)
    @LRA(value = LRA.Type.REQUIRED)
    public Response extendTimeLimit(@HeaderParam(LRA_HTTP_HEADER) String lraId) {
        activityService.add(new Activity(lraId));//NarayanaLRAClient.getLRAId(lraId)));

        try {
            /*
             * the incomming LRA was created with a timeLimit of 100 ms via the @TimeLimit annotation
             * update the timeLimit to 300
             * sleep for 200
             * return from the method so the LRA will have been running for 200 ms so it should not be cancelled
             */
            lraClient.renewTimeLimit(lraToURL(lraId, "Invalid LRA id"), 300, TimeUnit.MILLISECONDS);
            Thread.sleep(200); // sleep for 200000 micro seconds (should be longer than specified in the @TimeLimit annotation)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.OK).entity(Entity.text("Simulate buisiness logic timeoout")).build();
    }

    /**
     * Performing a PUT on <participant URL>/compensate will cause the participant to compensate
     * the work that was done within the scope of the transaction.
     *
     * The participant will either return a 200 OK code and a <status URL> which indicates the outcome and which can be probed (via GET)
     * and will simply return the same (implicit) information:
     *
     * <URL>/cannot-compensate
     * <URL>/cannot-complete
     */
    @PUT
    @Path("/{TxId}/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response compensate(@PathParam("TxId")String txId) throws NotFoundException {
        Activity activity = activityService.getActivity(txId);

        activity.status = CompensatorStatus.Compensated;
        activity.statusUrl = String.format("%s/%s/activity/compensated", context.getBaseUri(), txId);

        return Response.ok(activity.statusUrl).build();
    }

    /**
     * Performing a PUT on <participant URL>/complete will cause the participant to tidy up and it can forget this transaction.
     *
     * The participant will either return a 200 OK code and a <status URL> which indicates the outcome and which can be probed (via GET)
     * and will simply return the same (implicit) information:
     * <URL>/cannot-compensate
     * <URL>/cannot-complete
     */
    @PUT
    @Path("/{TxId}/complete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response complete(@PathParam("TxId")String txId) throws NotFoundException {
        Activity activity = activityService.getActivity(txId);

        activity.status = CompensatorStatus.Completed;
        activity.statusUrl = String.format("%s/%s/activity/completed", context.getBaseUri(), txId);

        return Response.ok(activity.statusUrl).build();
    }

    @PUT
    @Path("/{TxId}/forget")
    public void forget(@PathParam("TxId")String txId) throws NotFoundException {
        Activity activity = activityService.getActivity(txId);

        activityService.remove(activity.id);
    }

    @GET
    @Path("/{TxId}/completed")
    @Produces(MediaType.APPLICATION_JSON)
    public String completedStatus(@PathParam("TxId")String txId) {
        return CompensatorStatus.Completed.name();
    }

    @GET
    @Path("/{TxId}/compensated")
    @Produces(MediaType.APPLICATION_JSON)
    public String compensatedStatus(@PathParam("TxId")String txId) {
        return CompensatorStatus.Compensated.name();
    }

    private void checkStatusAndClose(Response response, int expected) {
        try {
            if (response.getStatus() != expected)
                throw new WebApplicationException(response);
        } finally {
            response.close();
        }
    }

    private static URL lraToURL(String lraId, String errorMessage) {
        try {
            return new URL(lraId);
        } catch (MalformedURLException e) {
            LRALogger.i18NLogger.error_urlConstructionFromStringLraId(lraId, e);
            throw new GenericLRAException(null, BAD_REQUEST.getStatusCode(), errorMessage + ": lra id: " + lraId, e);
        }
    }
}
