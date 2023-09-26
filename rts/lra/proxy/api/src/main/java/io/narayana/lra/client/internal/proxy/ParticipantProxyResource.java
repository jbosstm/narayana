/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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