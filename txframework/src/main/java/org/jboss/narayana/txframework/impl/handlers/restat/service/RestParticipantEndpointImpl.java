package org.jboss.narayana.txframework.impl.handlers.restat.service;

import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PUT;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author paul.robinson@redhat.com 07/04/2012
 */
@Path("/")
public class RestParticipantEndpointImpl {

    private static String FAIL_COMMIT;

    //todo: should this be a uuid?
    private static AtomicInteger currentParticipantId = new AtomicInteger(0);

    //todo: Entries are nevr removed. They should be when the TX is forgotten
    private static Map<Integer, RESTAT2PCParticipant> participants = new ConcurrentHashMap<Integer, RESTAT2PCParticipant>();


    public static void enlistParticipant(String txid, UriInfo info, String enlistUrl, Object serviceImpl) throws ParticipantRegistrationException {

        //todo: use a @Notnull annotation.
        checkNotNull(info, "txid");
        checkNotNull(info, "info");
        checkNotNull(enlistUrl, "enlistUrl");
        checkNotNull(serviceImpl, "serviceImpl");

        final int pid = currentParticipantId.getAndIncrement();

        participants.put(pid, new RESTAT2PCParticipant(serviceImpl, true));

        TxSupport txSupport = new TxSupport();
        /*
         * Draft 8 of the REST-AT spec uses link headers for participant registration
         *
         * The next call constructs the participant-resource and participant-terminator URIs for participants
         * in the format: "<baseURI>/{uid1}/{uid2}/participant" and "<baseURI>/{uid1}/{uid2}/terminator"
         */
        String linkHeader = txSupport.makeTwoPhaseAwareParticipantLinkHeader(
                info.getAbsolutePath().toString(), txid, String.valueOf(pid));
        System.out.println("Service: Enlisting " + linkHeader);
        String recoveryUri = txSupport.enlistParticipant(enlistUrl, linkHeader);
        System.out.println("Service: recoveryURI: " + recoveryUri);

        // TODO the recovery URI should be use by the framework to provide recovery support
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

        return Response.ok(TxSupport.toStatusContent(status.name())).build();
        //todo: shouldn't we get a FORGET here? If so, that is the time to remove the participant entry from the participants map.
    }

    /*
     * This method handles requests from the REST-AT coordinator for the participant terminator URI
     */
    @HEAD
    @Path("{whats_this}/{txid}/{pId}/participant")
    public Response getTerminator(@Context UriInfo info, @PathParam("pId") @DefaultValue("")String wId) {
        String serviceURL = info.getBaseUri() + info.getPath();
        String linkHeader = new TxSupport().makeTwoPhaseAwareParticipantLinkHeader(serviceURL, false, wId, null);

        return Response.ok().header("Link", linkHeader).build();
    }
}
