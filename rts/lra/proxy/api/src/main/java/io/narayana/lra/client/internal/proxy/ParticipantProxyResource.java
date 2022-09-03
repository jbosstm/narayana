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

package io.narayana.lra.client.internal.proxy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import io.narayana.lra.proxy.logging.LRAProxyLogger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

@ApplicationScoped
@Path(ParticipantProxyResource.LRA_PROXY_PATH)
public class ParticipantProxyResource {
    static final String LRA_PROXY_PATH = "lraproxy";
    @Inject
    private ProxyService proxyService;

    @Path("{lraId}/{pId}/complete")
    @PUT
    public Response complete(@PathParam("lraId")String lraId,
                         @PathParam("pId")String participantId,
                         String participantData) throws URISyntaxException, UnsupportedEncodingException {
        return proxyService.notifyParticipant(toURI(lraId, true), participantId, participantData, false);
    }

    @Path("{lraId}/{pId}/compensate")
    @PUT
    public Response compensate(@PathParam("lraId")String lraId,
                               @PathParam("pId")String participantId,
                               String participantData) throws URISyntaxException, UnsupportedEncodingException {
        return proxyService.notifyParticipant(toURI(lraId, true), participantId, participantData, true);
    }

    @Path("{lraId}/{pId}")
    @DELETE
    public void forget(@PathParam("lraId")String lraId,
                       @PathParam("pId")String participantId) throws URISyntaxException, UnsupportedEncodingException {
        proxyService.notifyForget(toURI(lraId, true), participantId);
    }

    @Path("{lraId}/{pId}")
    @GET
    public String status(@PathParam("lraId")String lraId,
                       @PathParam("pId")String participantId) throws UnsupportedEncodingException, InvalidLRAStateException {
        try {
            return proxyService.getStatus(toURI(lraId, true), participantId).name();
        } catch (URISyntaxException e) {
            String logMsg = LRAProxyLogger.i18NLogger.error_gettingParticipantStatus(participantId, lraId, e);
            LRAProxyLogger.logger.error(logMsg);
            throw new InvalidLRAStateException(logMsg);
        }
    }

    private URI toURI(String url, boolean decode) throws URISyntaxException, UnsupportedEncodingException {
        if (url == null) {
            return null;
        }

        if (decode) {
            url = URLDecoder.decode(url, "UTF-8");
        }

        return new URI(url);
    }
}
