/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.impl.handlers.restat.service;

import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.txframework.impl.Participant;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author paul.robinson@redhat.com 07/04/2012
 */
@Path("/")
public class RestParticipantEndpointImpl {

    private static String FAIL_COMMIT;

    //todo: should this be a uuid?
    private static AtomicInteger currentParticipantId = new AtomicInteger(0);

    //todo: Entries are never removed. They should be when the TX is forgotten
    private static Map<Integer, RESTAT2PCParticipant> participants = new ConcurrentHashMap<Integer, RESTAT2PCParticipant>();


    public static Participant enlistParticipant(String txid, UriInfo info, String enlistUrl, ServiceInvocationMeta serviceInvocationMeta) throws ParticipantRegistrationException {

        //todo: use a @Notnull annotation.
        checkNotNull(info, "txid");
        checkNotNull(info, "info");
        checkNotNull(enlistUrl, "enlistUrl");
        checkNotNull(serviceInvocationMeta, "serviceImpl");

        final int pid = currentParticipantId.getAndIncrement();

        RESTAT2PCParticipant participant = new RESTAT2PCParticipant(serviceInvocationMeta);
        participants.put(pid, participant);
        participant.resume();

        TxSupport txSupport = new TxSupport();
        /*
         * Draft 8 of the REST-AT spec uses link headers for participant registration
         *
         * The next call constructs the participant-resource and participant-terminator URIs for participants
         * in the format: "<baseURI>/{uid1}/{uid2}/participant" and "<baseURI>/{uid1}/{uid2}/terminator"
         */
        String linkHeader = txSupport.makeTwoPhaseAwareParticipantLinkHeader(
                info.getAbsolutePath().toString(), txid, String.valueOf(pid));
        String recoveryUri = txSupport.enlistParticipant(enlistUrl, linkHeader);

        // TODO the recovery URI should be used by the framework to provide recovery support
        return participant;
    }

    private static void checkNotNull(Object object, String name) throws ParticipantRegistrationException {

        if (object == null) {
            throw new ParticipantRegistrationException(name + " is null");
        }
    }


    /*
    * this method handles PUT requests to the url that the participant gave to the REST Atomic Transactions implementation
    * (in the someServiceRequest method). This is the endpoint that the transaction manager interacts with when it needs
    * participants to prepare/commit/rollback their transactional work.commitCount
    */
    @PUT
    @Path("{whats_this}/{txid}/{pId}/terminator")
    public Response terminate(@PathParam("pId") @DefaultValue("") Integer pId, String content) {

        RESTAT2PCParticipant participant = participants.get(pId);
        participant.resume();
        TxStatus status = TxSupport.toTxStatus(content);

        if (status.isPrepare()) {

            if (!participant.prepare()) {
                return Response.ok(HttpURLConnection.HTTP_CONFLICT).build();
            }

        } else if (status.isCommit()) {
            participant.commit();
        } else if (status.isAbort()) {
            participant.rollback();
        } else {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }

        RESTAT2PCParticipant.suspend();

        return Response.ok(TxSupport.toStatusContent(status.name())).build();
        //todo: shouldn't we get a FORGET here? If so, that is the time to remove the participant entry from the participants map.
    }

    /*
     * This method handles requests from the REST-AT coordinator for the participant terminator URI
     */
    @HEAD
    @Path("{whats_this}/{txid}/{pId}/participant")
    public Response getTerminator(@Context UriInfo info, @PathParam("pId") @DefaultValue("") String wId) {

        String serviceURL = info.getBaseUri() + info.getPath();
        String linkHeader = new TxSupport().makeTwoPhaseAwareParticipantLinkHeader(serviceURL, false, wId, null);

        return Response.ok().header("Link", linkHeader).build();
    }
}
