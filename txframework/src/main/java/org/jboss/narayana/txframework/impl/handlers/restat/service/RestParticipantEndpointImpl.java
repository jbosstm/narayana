package org.jboss.narayana.txframework.impl.handlers.restat.service;

import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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

        // draft 8 of the REST-AT spec uses link headers for participant registration
        TxSupport txSupport = new TxSupport();
        String linkHeader = txSupport.makeTwoPhaseAwareParticipantLinkHeader(
                info.getAbsolutePath().toString(), txid, String.valueOf(pid));
        System.out.println("Service: Enlisting " + linkHeader);
        String recoveryUrl = txSupport.enlistParticipant(enlistUrl, linkHeader);
        System.out.println("Service: recoveryURI: " + recoveryUrl);

/*
        String path = info.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String serviceURL = info.getBaseUri() + path;
        String terminator = serviceURL + txid + "/" + pid + "/terminate";
        String participant = serviceURL + txid + "/" + pid + "/terminator";

        String pUrls = TxSupport.getParticipantUrls(terminator, participant);
        System.out.println("Service: Enlisting " + pUrls);


        TxSupport txSupport = new TxSupport();
        String response = txSupport.httpRequest(new int[]{HttpURLConnection.HTTP_CREATED}, enlistUrl,
                "POST", TxSupport.POST_MEDIA_TYPE, pUrls, null);
        //todo: check response
*/
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
    public Response terminate(@PathParam("txid") @DefaultValue("") String txid, @PathParam("pId") @DefaultValue("") Integer pId, String content) {

        RESTAT2PCParticipant participant = participants.get(pId);
        String status = TxSupport.getStatus(content);

        if (TxStatus.isPrepare(status)) {

            boolean prepared = participant.prepare();
            if (prepared) {
                return Response.ok(TxSupport.toStatusContent(TxStatus.TransactionPrepared.name())).build();
            } else {
                return Response.ok(HttpURLConnection.HTTP_CONFLICT).build();
            }

        } else if (TxStatus.isCommit(status)) {
            participant.commit();
            return Response.ok(TxSupport.toStatusContent(TxStatus.TransactionCommitted.name())).build();

        } else if (TxStatus.isAbort(status)) {
            participant.rollback();
            return Response.ok(TxSupport.toStatusContent(TxStatus.TransactionRolledBack.name())).build();

        } else {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }
        //todo: shouldn't we get a FORGET here? If so, that is the time to remove the participant entry from the participants map.
    }
}
