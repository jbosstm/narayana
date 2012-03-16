/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010
 * @author JBoss Inc.
 */
package org.jboss.jbossts.star.service;

import java.util.concurrent.ConcurrentHashMap;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.jbossts.star.provider.ResourceNotFoundException;
import org.jboss.jbossts.star.provider.TransactionStatusException;
import org.jboss.jbossts.star.resource.Transaction;
import org.jboss.jbossts.star.util.LinkHolder;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.logging.Logger;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

@Path(TxSupport.TX_PATH)
public class Coordinator
{
    protected final static Logger log = Logger.getLogger(Coordinator.class);

    private static Map<String, Transaction> transactions = new ConcurrentHashMap<String, Transaction>();
    // each participant may only be enlisted in one transaction
    private static Map<String, LinkHolder> participants = new ConcurrentHashMap<String, LinkHolder>();

    /**
     * Performing a GET on the transaction-manager returns a list of all transaction URIs
     * known to the coordinator (active and in recovery) separated by
     * the @see TxSupport.URI_SEPARATOR character
     * @param info http context of the request
     * @return JSON representation of active transactions and HTTP status code
     */
    @GET
    @Path(TxSupport.TX_SEGMENT)
    @Produces(TxSupport.PLAIN_MEDIA_TYPE)
    public Response getAllTransactions(@Context UriInfo info)//, String content)
    {
        log.trace("coordinator: list: transaction-coordinator");
        StringBuilder txns = new StringBuilder();
        Iterator<String> i = transactions.keySet().iterator();

        while (i.hasNext()) {
            URI uri = TxSupport.getUri(info, info.getPathSegments().size(), i.next());

            txns.append(uri.toString());

            if (i.hasNext())
                txns.append(TxSupport.URI_SEPARATOR);
        }

        /*
         * Note, to return only recovering or only active txns use:
         * getTransactions(recoveringFilter); or
         * getTransactions(activeTxFilter);
         */
        Response.ResponseBuilder builder = Response.ok(txns.toString());
        builder.header("Content-Length", txns.length());
        return Response.ok(txns.toString()).build();
    }

    /**
     * Performing a GET on the transaction url returns its status
     * @see org.jboss.jbossts.star.util.TxSupport.TX_ACTIVE
     *  etc for the format of the returned content
     * @param id URL template parameter for the id of the transaction
     * @return content representing the status of the transaction
     */
    @GET
    @Path(TxSupport.TX_SEGMENT + "/{id}")
    @Produces(TxSupport.STATUS_MEDIA_TYPE)
    public Response getTransactionStatus(@PathParam("id") String id)
    {
        log.trace("coordinator: status: transaction-coordinator/" + id);
        Transaction txn = transactions.get(id);

        if (txn == null)
            return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build();

        return Response.ok(TxSupport.toStatusContent(txn.getStatus())).build();
    }

    /**
     * Performing a DELETE on the transaction-coordinator URL will return a 403.
     * @param id transaction id
     * @return 403
     */
    @DELETE
    @Path(TxSupport.TX_SEGMENT + "/{id}")
    public Response deleteTransaction(@PathParam("id") String id)
    {
        return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
    }

    // Performing HEAD, GET, POST, DELETE and OPTIONS on the transaction
    // url generates a 400 status code
    @HEAD @Path(TxSupport.TX_SEGMENT + "/{TxId}/terminate")
    public Response tt1(@PathParam("TxId")String txId) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }
    @GET @Path(TxSupport.TX_SEGMENT + "/{TxId}/terminate")
    public Response tt2(@PathParam("TxId")String txId) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }
    @POST @Path(TxSupport.TX_SEGMENT + "/{TxId}/terminate")
    public Response tt3(@PathParam("TxId")String txId) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }
    @DELETE @Path(TxSupport.TX_SEGMENT + "/{TxId}/terminate")
    public Response tt4(@PathParam("TxId")String txId) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }
    @OPTIONS @Path(TxSupport.TX_SEGMENT + "/{TxId}/terminate")
    public Response tt5(@PathParam("TxId")String txId) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }

    /**
     *
     * Performing a POST on Transaction Manager URL @see TxSupport.TX_SEGMENT with no content
     * as shown below will start a new transaction with a default timeout.
     * A successful invocation will return 201 and the Location header MUST contain the URI
     * of the newly created transaction resource, which we will refer to as the transaction-coordinator
     * in the rest of this specification. Two related URLs MUST also be returned,
     * one for use by the terminator of the transaction (typically referred to as the client)
     * and one used for registering durable participation in the transaction (typically referred
     * to as the server). These linked URLs can be of arbitrary format.
     * The rel names for the links are:
     * @see TxSupport.TERMINATOR_LINK and @see TxSupport.PARTICIPANT_LINK
     *
     * @param info uri context
     * @param headers http headers
     * @param content empty if no transaction timeout is required otherwise the number of milliseconds
     * after which the transaction is eligible for being timed out. The content should have the format
     * @ see TxSupport.TIMEOUT_PROPERTY=<milliseconds>
     * @return http status code
     */
    @POST
    @Path(TxSupport.TX_SEGMENT + "/")
    @Consumes(TxSupport.POST_MEDIA_TYPE)
//    @Produces("application/vnd.rht.txstatus+text;version=0.1")
    public Response beginTransaction(@Context UriInfo info, @Context HttpHeaders headers, @DefaultValue("") String content)
    {
        log.trace("coordinator: POST /transaction-manager content: " + content);
        Transaction tx = new Transaction("coordinator");
        int timeout = TxSupport.getIntValue(content, TxSupport.TIMEOUT_PROPERTY, 0); // default is 0 - never timeout
        String uid = tx.get_uid().fileStringForm();

        log.trace("coordinator: timeout=" + timeout);
        transactions.put(uid, tx);

        // round up the timeout from milliseconds to seconds
        if (timeout != 0) {
            if (timeout == 0)
                timeout = 1;
            else if (timeout < 0)
                timeout = 0;
			else
            	timeout /= 1000;
        }
		
        int status = tx.begin(timeout);

        try
        {
            if (status == ActionStatus.RUNNING) {
                URI uri1 = TxSupport.getUri(info, info.getPathSegments().size(), uid);
                Response.ResponseBuilder builder = Response.created(uri1);

                TxSupport.addLinkHeader(builder, info, TxSupport.TERMINATOR_LINK,
					TxSupport.TERMINATOR_LINK, uid, "terminate");
                TxSupport.addLinkHeader(builder, info, TxSupport.PARTICIPANT_LINK,
					TxSupport.PARTICIPANT_LINK, uid);

				return builder.build();
            }

            throw new TransactionStatusException("Transaction failed to start: " + status);
        } catch (Exception e) {
            log.debug("begin", e);
            throw new TransactionStatusException("Transaction failed to start: " + e);
        }
        finally
        {
            AtomicAction.suspend();
        }
    }

    /**
     * Obtain the transaction terminator and participant enlistment URIS for the
     * specified transaction id. These are returned in link headers in the same
     * way they were returned when the transaction was started @see Coordinator.beginTransaction
     *
     * @param info request context
     * @param id URL template parameter for the transaction id
     * @return http response
     */
    @HEAD
    @Path(TxSupport.TX_SEGMENT + "/{id}")
    public Response getTransactionURIs(@Context UriInfo info, @PathParam("id") String id)
    {
        log.trace("coordinator txn head request for txn " + id);
        Transaction tx = getTransaction(id);

        if (tx == null)
            return Response.status(HttpURLConnection.HTTP_GONE).build();

        Response.ResponseBuilder builder = Response.ok();

        TxSupport.addLinkHeader(builder, info, TxSupport.TERMINATOR_LINK, TxSupport.TERMINATOR_LINK, "terminate");
        TxSupport.addLinkHeader(builder, info, TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK);

        return builder.build();
    }

    /**
     * The client can control the outcome of the transaction by by PUTing to the terminator
     * URL returned as a response to the original transaction create request.
     * Upon termination, the resource and all associated resources are implicitly deleted.
     * For any subsequent invocation then an implementation MAY return 410 if the implementation
     * records information about transactions that have completed, otherwise it should return 404
     * (not necessary for presumed rollback semantics) but at a minimum MUST return 401.
     * The invoker can assume this was a rollback. In order for an interested party to know for
     * sure the outcome of a transaction then it MUST be registered as a participant with the
     * transaction coordinator.
     *
     * @param txId URL template component containing the transaction identifier
     * @param fault mechanism for injecting faults TODO use byteman instead
     * @param content body of the request indicating a commit or abort request
     *  @see TxSupport.COMMITTED etc
     * @return http response code
    */
    @PUT
    @Path(TxSupport.TX_SEGMENT + "/{TxId}/terminate")
    public Response terminateTransaction(@PathParam("TxId")String txId, @QueryParam("fault") @DefaultValue("")String fault, String content)
    {
        log.trace("coordinator: commit: transaction-manager/" + txId + "/terminate : content: " + content);

        Transaction tx = getTransaction(txId);
        Collection<String> enlistmentIds = new ArrayList<String>();
        String how = TxSupport.getStringValue(content, TxSupport.STATUS_PROPERTY);
        String status;
        int scRes;
		int ihow;

        tx.getParticipants(enlistmentIds);

		/*
			ABORT_ONLY is not in the spec for the same reasons as it's not in the WS-TX and WS-CAF where
			it is assumed that only the txn originator can end the tx:
			- simply register a synchronization in the transaction that prevented a commit from happening;
			and I haven't implemented synchronisations yet. 
			It is unclear why allowing similar functionality via synchronisations doesn't open up a similar
			security hole.
		*/

        if (TxSupport.COMMITTED.equals(how))
            ihow = 0;
        else if (TxSupport.ABORTED.equals(how))
            ihow = 1;
        else if (TxSupport.ABORT_ONLY.equals(how))
            ihow = 2;
        else
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();

        tx.setFault(fault);
        AtomicAction.resume(tx);

		if (ihow == 0) {
            status = tx.getStatus(tx.commit(true));
        } else if (ihow == 1) {
			status = tx.getStatus(tx.abort());
        } else {
			tx.preventCommit();
            status = tx.getStatus();
        }

        AtomicAction.suspend();

        log.trace("terminate result: " + status);

        if (tx.isRunning())
            throw new TransactionStatusException("Transaction failed to terminate");

        if (!tx.isAlive()) {
            transactions.remove(txId);

            for (String enlistmentId : enlistmentIds) {
                participants.remove(enlistmentId);
            }
        } else if (tx.isFinishing()) {
            // TODO who cleans up in this case
            log.debug("transaction is still terminating: " + status);
        }

        if (status.length() == 0)
            scRes = HttpURLConnection.HTTP_INTERNAL_ERROR;
        else
            scRes = HttpURLConnection.HTTP_OK;

        return Response.status(scRes).entity(TxSupport.toStatusContent(status)).build();
    }

    /**
     * Register a participant in a tx
     * @param txId id of transaction
     * @param content body of request containing URI for driving the participant through completion
     *  (the URI should be unique within the scope of txId)
     * @return unique resource ref for the participant
     */
    @POST
    @Path(TxSupport.TX_SEGMENT + "/{TxId}")
    public Response enlistParticipant(@Context UriInfo info, @PathParam("TxId")String txId, String content)
    {
        log.trace("enlistParticipant request uri " + info.getRequestUri() + " txid: " + txId + " content: " + content);
        Transaction tx = getTransaction(txId);
        if (tx == null)
            return Response.status(HttpURLConnection.HTTP_GONE).build();

        LinkHolder links = new LinkHolder(content);
        String txURI = TxSupport.buildURI(info.getBaseUriBuilder(), info.getPathSegments().get(0).getPath(), info.getPathSegments().get(1).getPath());



        UriBuilder builder = info.getBaseUriBuilder();
        builder.path(info.getPathSegments().get(0).getPath());
        builder.path(TxSupport.RC_SEGMENT);
        String recoveryUrlBase = builder.build().toString() + '/';       
        String terminatorUrl = links.get(TxSupport.TERMINATOR_LINK);

        if (tx.isEnlisted(terminatorUrl))
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();

        String coordinatorId = tx.enlistParticipant(txURI, links.get(TxSupport.PARTICIPANT_LINK), terminatorUrl, recoveryUrlBase);

        if (coordinatorId == null) // the request was rejected (2PC processing must have started or already registerd)
            return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();

        links.put("txid", txId);
        participants.put(coordinatorId, links);

        log.debug("enlisted participant: content=" + content + " in tx " + txId + " Coordinator url base: " + recoveryUrlBase);

        return Response.created(URI.create(tx.getRecoveryUrl())).build();
    }
    
    /**
     * Get the participant url (registered during enlistParticipant) corresponding to a resource reference
     * if the coordinator crashes - the participant list will be empty but this is ok if commit hasn't been
     * called since the TM uses presumed abort semantics.
     * @param enlistmentId the resource reference
     * @return the participant url
     */
    @GET
    @Path(TxSupport.RC_SEGMENT + "/{RecCoordId}")
    public Response lookupParticipant(@PathParam("RecCoordId")String enlistmentId)
    {
        log.trace("coordinator: lookup: transaction-coordinator/" + enlistmentId);

        LinkHolder p = participants.get(enlistmentId);

        if (p == null)
            return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build();

        String pContent = TxSupport.getParticipantUrls(p.get(TxSupport.TERMINATOR_LINK), p.get(TxSupport.PARTICIPANT_LINK));

        return Response.ok(p).entity(pContent).build();
    }

    /**
     * PUT /recovery-coordinator/<RecCoordId>/<new participant URL> -
     *   overwrite the old <participant URL> with <new participant URL>
     *   (as with JTS, this will also trigger off a recovery attempt on the associated transaction)
     * A participant may use this url to notifiy the coordinator that he has moved to a new location.
     * @param enlistmentId id by the participant is known
     * @param content http body
     * @return http status code
     */
    @PUT
    @Path(TxSupport.RC_SEGMENT + "/{RecCoordId}")
    public Response replaceParticipant(@PathParam("RecCoordId")String enlistmentId, String content)
    {
        LinkHolder links = new LinkHolder(content);
        String terminator = links.get(TxSupport.TERMINATOR_LINK);

        log.trace("coordinator: replace: recovery-coordinator/" + enlistmentId + "?URL=" + terminator);
        if (terminator == null)
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();

        participants.put(enlistmentId, links);

        return Response.status(HttpURLConnection.HTTP_OK).build();
    }

    @POST
    @Path(TxSupport.RC_SEGMENT + "/{RecCoordId}")
    public Response postParticipant(@PathParam("RecCoordId")String enlistmentId)
    {
        log.trace("coordinator: replace via Post: recovery-coordinator/" + enlistmentId);
        return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).build();
    }

    @DELETE
    @Path(TxSupport.RC_SEGMENT + "/{RecCoordId}")
    public Response deleteParticipant(@PathParam("RecCoordId")String enlistmentId)
    {
        log.trace("coordinator: participant leaving via Delete: recovery-coordinator/" + enlistmentId);
        LinkHolder p = participants.get(enlistmentId);
        Transaction txn;

        if (p == null || (txn = transactions.get(p.get("txid"))) == null)
            return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build();

        if (txn.forgetParticipant(p.get(TxSupport.TERMINATOR_LINK)))
            return Response.status(HttpURLConnection.HTTP_OK).build();
        
        return Response.status(HttpURLConnection.HTTP_CONFLICT).build();
    }

    private Transaction getTransaction(String txId)
    {
        Transaction tx = transactions.get(txId);

        if (tx == null)
            throw new ResourceNotFoundException("Transaction id not found");

        return tx;
    }
}

