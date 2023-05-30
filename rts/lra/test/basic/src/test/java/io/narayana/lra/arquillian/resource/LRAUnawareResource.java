/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */
package io.narayana.lra.arquillian.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@ApplicationScoped
@Path(LRAUnawareResource.ROOT_PATH)
public class LRAUnawareResource {

    public static final String ROOT_PATH = "/lra-unaware";
    public static final String RESOURCE_PATH = "/lra-work";

    @Context
    private UriInfo context;

    @GET
    @Path(RESOURCE_PATH)
    public Response doInLRA() {
        try (Client client = ClientBuilder.newClient()) {
            // SimpleLRAParticipant.START_LRA_PATH has REQUIRED LRA type meaning that
            // if this method propagates LRA context it will be reused in SimpleLRAParticipant
            // but if it doesn't, a new LRA will be started
            return client.target(context.getBaseUri())
                .path(SimpleLRAParticipant.SIMPLE_PARTICIPANT_RESOURCE_PATH)
                .path(SimpleLRAParticipant.START_LRA_PATH)
                .request()
                .get();
        }
    }
}
