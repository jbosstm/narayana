/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package io.narayana.lra.proxy.test.api;

import io.narayana.lra.client.internal.proxy.ProxyService;
import io.narayana.lra.proxy.test.model.Activity;
import io.narayana.lra.proxy.test.model.Participant;
import io.narayana.lra.proxy.test.service.ActivityService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
