/*
 * SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.arquillian.resource;

import jakarta.ws.rs.Path;

@Path("/root")
public class RootResource {

    @Path("/participant")
    public NonRootLRAParticipant getParticipant() {
        return new NonRootLRAParticipant();
    }
}