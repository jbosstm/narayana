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

import io.narayana.lra.participant.model.Activity;
import io.narayana.lra.participant.service.ActivityService;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRA;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.TimeLimit;

import org.eclipse.microprofile.lra.client.IllegalLRAStateException;
import org.eclipse.microprofile.lra.client.LRAClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.narayana.lra.client.NarayanaLRAClient.LRA_HTTP_HEADER;
import static io.narayana.lra.client.NarayanaLRAClient.LRA_HTTP_RECOVERY_HEADER;
import static io.narayana.lra.participant.api.TimedParticipant.ACTIVITIES_PATH2;

@ApplicationScoped
@Path(ACTIVITIES_PATH2)
@LRA(LRA.Type.SUPPORTS)
public class TimedParticipant {
    public static final String ACTIVITIES_PATH2 = "timedactivities";
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

    @PUT
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    @TimeLimit(limit = 100, unit = TimeUnit.MILLISECONDS)
    public Response completeWork(@HeaderParam(LRA_HTTP_HEADER) String lraId, String userData) throws NotFoundException {
        completedCount.incrementAndGet();

        assert lraId != null;

        Activity activity = activityService.getActivity(lraId);

        activity.setEndData(userData);

        if (activity.getAndDecrementAcceptCount() > 0) {
            activity.status = CompensatorStatus.Completing;
            activity.statusUrl = String.format("%s/%s/%s/status", context.getBaseUri(), ACTIVITIES_PATH2, lraId);

            return Response.accepted().location(URI.create(activity.statusUrl)).build();
        }

        activity.status = CompensatorStatus.Completed;
        activity.statusUrl = String.format("%s/%s/activity/completed", context.getBaseUri(), lraId);

        endCheck(activity);

        System.out.printf("ActivityController completing %s%n", lraId);
        return Response.ok(activity.statusUrl).build();
    }

    @PUT
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    @TimeLimit(limit = 100, unit = TimeUnit.MILLISECONDS)
    public Response compensateWork(@HeaderParam(LRA_HTTP_HEADER) String lraId, String userData) throws NotFoundException {
        compensatedCount.incrementAndGet();

        assert lraId != null;

        Activity activity = activityService.getActivity(lraId);

        activity.setEndData(userData);

        if (activity.getAndDecrementAcceptCount() > 0) {
            activity.status = CompensatorStatus.Compensating;
            activity.statusUrl = String.format("%s/%s/%s/status", context.getBaseUri(), ACTIVITIES_PATH2, lraId);

            return Response.accepted().location(URI.create(activity.statusUrl)).build();
        }

        activity.status = CompensatorStatus.Compensated;
        activity.statusUrl = String.format("%s/%s/activity/compensated", context.getBaseUri(), lraId);

        endCheck(activity);

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
    @Path("/timeLimitRequiredLRA")
    @Produces(MediaType.APPLICATION_JSON)
    @TimeLimit(limit = 100, unit = TimeUnit.MILLISECONDS)
    @LRA(value = LRA.Type.REQUIRED)
    public Response timeLimitRequiredLRA(@HeaderParam(LRA_HTTP_HEADER) String lraId) {
        activityService.add(new Activity(lraId));//NarayanaLRAClient.getLRAId(lraId)));

        try {
            Thread.sleep(300); // sleep for 200 miliseconds (should be longer than specified in the @TimeLimit annotation)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.OK).entity(Entity.text("Simulate buisiness logic timeoout")).build();
    }

    @GET
    @Path("/timeLimitSupportsLRA")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(value = LRA.Type.SUPPORTS)
    public Response timeLimitSupportsLRA(@HeaderParam(LRA_HTTP_HEADER) String lraId) {
        activityService.add(new Activity(lraId));

        return Response.status(Response.Status.OK).entity(Entity.text("Simulate buisiness logic timeoout")).build();
    }

    private Activity addWork(String lraId, String rcvId) {
        assert lraId != null;

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

    private void endCheck(Activity activity) {
        String how = activity.getHow();
        String arg = activity.getArg();

        activity.setHow(null);
        activity.setArg(null);

        if ("wait".equals(how) && arg != null && "recovery".equals(arg)) {
            lraClient.getRecoveringLRAs();
        }
    }
}
