/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.rest.bridge.inbound.test.common;

import org.jboss.logging.Logger;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * REST resource which required inbound bridge to be enabled.
 *
 * @author Gytis Trikleris
 *
 */
@Transactional
@Path("/")
public class SimpleInboundBridgeResource {

    private static final Logger LOG = Logger.getLogger(SimpleInboundBridgeResource.class);

    @POST
    public Response dummyPost() {
        LOG.info("SimpleInboundBridgeResource.dummyPost");

        return Response.ok().build();
    }

}