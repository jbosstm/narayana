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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import io.narayana.lra.proxy.logging.LRAProxyLogger;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import static io.narayana.lra.client.internal.proxy.ParticipantProxyResource.LRA_PROXY_PATH;

@ApplicationScoped
@Path(LRA_PROXY_PATH)
public class ParticipantProxyResource {
    static final String LRA_PROXY_PATH = "lraproxy";
    @Inject
    private ProxyService proxyService;

    @Path("{lraId}/{pId}/complete")
    @PUT
    public Response complete(@PathParam("lraId")String lraId,
                         @PathParam("pId")String participantId,
                         String participantData) throws MalformedURLException, UnsupportedEncodingException {
        return proxyService.notifyParticipant(toURL(lraId, true), participantId, participantData, false);
    }

    @Path("{lraId}/{pId}/compensate")
    @PUT
    public Response compensate(@PathParam("lraId")String lraId,
                               @PathParam("pId")String participantId,
                               String participantData) throws MalformedURLException, UnsupportedEncodingException {
        return proxyService.notifyParticipant(toURL(lraId, true), participantId, participantData, true);
    }

    @Path("{lraId}/{pId}")
    @DELETE
    public void forget(@PathParam("lraId")String lraId,
                       @PathParam("pId")String participantId) throws MalformedURLException, UnsupportedEncodingException {
        proxyService.notifyForget(toURL(lraId, true), participantId);
    }

    @Path("{lraId}/{pId}")
    @GET
    public String status(@PathParam("lraId")String lraId,
                       @PathParam("pId")String participantId) throws UnsupportedEncodingException, InvalidLRAStateException {
        try {
            return proxyService.getStatus(toURL(lraId, true), participantId).name();
        } catch (MalformedURLException e) {
            LRAProxyLogger.i18NLogger.error_gettingParticipantStatus(participantId, lraId, e);
            throw new InvalidLRAStateException("Caller provided an invalid LRA: " + lraId, e);
        }
    }

    private URL toURL(String url, boolean decode) throws MalformedURLException, UnsupportedEncodingException {
        if (url == null)
            return null;

        if (decode)
            url = URLDecoder.decode(url, "UTF-8");

        return new URL(url);
    }
}
