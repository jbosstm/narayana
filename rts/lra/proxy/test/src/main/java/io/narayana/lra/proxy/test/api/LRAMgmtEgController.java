/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package io.narayana.lra.proxy.test.api;

import io.narayana.lra.client.internal.proxy.ProxyService;
import io.narayana.lra.proxy.test.model.Activity;
import io.narayana.lra.proxy.test.model.Participant;
import io.narayana.lra.proxy.test.service.ActivityService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;

import static io.narayana.lra.proxy.test.api.LRAMgmtEgController.LRAM_PATH;

/**
 * for testing {@link io.narayana.lra.client.internal.proxy.ProxyService}
 */
@Path(LRAM_PATH)
public class LRAMgmtEgController {
    public static final String LRAM_PATH = "lram";
    public static final String LRAM_WORK = "work";
    public static final String GET_ACTIVITY_PATH = "getActivity";

    @Inject
    private ProxyService lraManagement;

    @Inject
    private ActivityService activityService;

    @PUT
    @Path(LRAM_WORK)
    public Response lramTest(@QueryParam("lraId") String lraId) throws URISyntaxException {
        assert lraId != null;

        Activity activity = new Activity(lraId);

        activityService.add(activity);

        activity.rcvUrl = lraManagement.joinLRA(
                new Participant(activity), toURI(lraId), 0L, ChronoUnit.SECONDS);

        return Response.ok(activity.id).build();
    }

    @GET
    @Path(GET_ACTIVITY_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public String getActivity(@QueryParam("activityId") String activityId) {
        return activityService.getActivity(activityId, false).toString();
    }

    private URI toURI(String uri) throws URISyntaxException {
        return uri == null ? null : new URI(uri);
    }
}