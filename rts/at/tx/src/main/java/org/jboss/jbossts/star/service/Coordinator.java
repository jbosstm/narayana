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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import org.jboss.jbossts.star.logging.RESTATLogger;
import org.jboss.jbossts.star.provider.ResourceNotFoundException;
import org.jboss.jbossts.star.provider.TransactionStatusException;
import org.jboss.jbossts.star.resource.RESTRecord;
import org.jboss.jbossts.star.resource.RecoveringTransaction;
import org.jboss.jbossts.star.resource.Transaction;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxStatusMediaType;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.jbossts.star.util.media.txstatusext.CoordinatorElement;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionManagerElement;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionStatisticsElement;
import org.jboss.jbossts.star.util.media.txstatusext.TwoPhaseAwareParticipantElement;
import org.jboss.logging.Logger;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

@Path(TxSupport.TX_PATH)
public class Coordinator {
    static final String RC_SEGMENT = "recovery-coordinator";

    protected static final Logger log = Logger.getLogger(Coordinator.class);

    private static final String REST_TXN_TYPE = new AtomicAction().type();

    private static Map<String, Transaction> transactions = new ConcurrentHashMap<String, Transaction>();
    // each participant may only be enlisted in one transaction - map each registration id to a map of
    // link name to link uri
    private static Map<String, HashMap<String, String>> participants = new ConcurrentHashMap<String, HashMap<String, String>>();

    private static Map<String, RecoveringTransaction> recoveringTransactions = getRecoveringTransactions(transactions);

    // active and prepared signalized how many transactions are currently active and/or prepared
    private static final AtomicInteger active = new AtomicInteger(0);
    private static final AtomicInteger prepared = new AtomicInteger(0);
    // commited and aborted makes a sum of how many transactions were committed/aborted till now
    private static final AtomicInteger committed = new AtomicInteger(0);
    private static final AtomicInteger aborted = new AtomicInteger(0);

    private static long age = System.currentTimeMillis();

    /**
     * Performing a GET on the transaction-manager returns a list of all transaction URIs
     * known to the coordinator (active and in recovery) separated by
     * the @see TxSupport.URI_SEPARATOR character
     * @param info http context of the request
     * @return JSON representation of active transactions and HTTP status code
     */
    @GET
    @Path(TxSupport.TX_SEGMENT)
    @Produces(TxMediaType.TX_LIST_MEDIA_TYPE)
    public Response getAllTransactions(@Context UriInfo info) {
        log.trace("coordinator: list: transaction-coordinator");
        StringBuilder txns = new StringBuilder();
        Iterator<String> i;
        String statisticsUri = TxSupport.extractUri(info, TxLinkNames.STATISTICS);

        updateTransactions();
        i = transactions.keySet().iterator();

        while (i.hasNext()) {
            URI uri = TxSupport.getUri(info, info.getPathSegments().size(), i.next());
            txns.append(uri.toASCIIString());

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

        TxSupport.setLinkHeader(builder, "Transaction Statistics", TxLinkNames.STATISTICS,
                statisticsUri, TxMediaType.TX_STATUS_EXT_MEDIA_TYPE);

        return builder.build();
    }

    /**
     * Performing a GET on the transaction-manager URI with media type application/txstatusext+xml
     * returns extended information about the transaction-manager resource such as how long it has
     * been up and all transaction-coordinator URIs.
     *
     * @param info Request context
     * @return TransactionManagerElement
     */
    @GET
    @Path(TxSupport.TX_SEGMENT)
    @Produces(TxMediaType.TX_STATUS_EXT_MEDIA_TYPE)
    public TransactionManagerElement getTransactionManagerInfo(@Context UriInfo info) {
        TransactionManagerElement tm = new TransactionManagerElement();

        updateTransactions();

        for (String s : transactions.keySet()) {
            URI uri = TxSupport.getUri(info, info.getPathSegments().size(), s);
            tm.addCoordinator(uri.toASCIIString());
        }

        tm.setCreated(new Date(age));
        tm.setStatistics(new TransactionStatisticsElement(
                active.get(), prepared.get(), committed.get(), aborted.get()));

        return tm;
    }

    /**
     * Performing a GET on the transaction-manager URI sufficed with /statistics
     * returns statistics of the transaction manager.
     * Numbers of active, prepared, committed, and aborted transactions are returned.
     *
     * @return TransactionStatisticsElement
     */
    @GET
    @Path(TxSupport.TX_SEGMENT + TxLinkNames.STATISTICS)
    @Produces(TxMediaType.TX_STATUS_EXT_MEDIA_TYPE)
    public TransactionStatisticsElement getTransactionStatistics() {
        // TODO: for prepared we could go through every transaction and get its status
        return new TransactionStatisticsElement(
                active.get(), prepared.get(), committed.get(), aborted.get());
    }

    /**
     * Obtain the transaction terminator and participant enlistment URIs for the
     * specified transaction id. These are returned in link headers in the same
     * way they were returned when the transaction was started @see Coordinator#beginTransaction
     *
     * @param info request context
     * @param id URL template parameter for the transaction id
     * @return http response
     */
    @HEAD
    @Path(TxSupport.TX_SEGMENT + "{id}")
    @Produces(TxMediaType.TX_LIST_MEDIA_TYPE)
    public Response getTransactionURIs(@Context UriInfo info, @PathParam("id") String id) {
        log.tracef("coordinator txn head request for txn %s", id);
        getTransaction(id); // throws an exception if the transaction does not exist

        String terminator = TxLinkNames.TERMINATOR;
        Response.ResponseBuilder builder = Response.ok();

        TxSupport.addLinkHeader(builder, info, terminator, terminator, terminator);
        TxSupport.addLinkHeader(builder, info, TxLinkNames.PARTICIPANT, TxLinkNames.PARTICIPANT);

        return builder.build();
    }

    /**
     * Performing a GET on the transaction url returns its status
     * @see org.jboss.jbossts.star.util.TxStatusMediaType#TX_ACTIVE
     *  etc for the format of the returned content
     * @param info request context
     * @param id URL template parameter for the id of the transaction
     * @return content representing the status of the transaction
     */
    @GET
    @Path(TxSupport.TX_SEGMENT + "{id}")
    @Produces(TxMediaType.TX_STATUS_MEDIA_TYPE)
    public Response getTransactionStatus(@Context UriInfo info, @PathParam("id") String id) {
        log.tracef("coordinator: status: transaction-coordinator/%s", id);
        Transaction txn = getTransaction(id);
        Response.ResponseBuilder builder = Response.ok(TxSupport.toStatusContent(txn.getStatus()));

        return addTransactionHeaders(builder, info, txn, false).build();
    }

    /**
     * Performing a GET on the transaction URL with media type application/txstatusext+xml
     * returns extended information about the transaction, such as its status,
     * number of participants, and their individual URIs.
     * @param info Request context
     * @param id URL template parameter for the id of the transaction
     * @return HTTP response representing extended transaction status information
     */
    @GET
    @Path(TxSupport.TX_SEGMENT + "{id}")
    @Produces(TxMediaType.TX_STATUS_EXT_MEDIA_TYPE)
    public Response getTransactionExtStatus(@Context UriInfo info, @PathParam("id") String id) {
        log.tracef("coordinator: status: transaction-coordinator/%s", id);
        Transaction txn = getTransaction(id);
        String terminator = TxLinkNames.TERMINATOR;
        Collection<String> enlistmentIds = new ArrayList<String>();
        CoordinatorElement coordinatorElement = txn.toXML();
        String txnURI = TxSupport.getUri(info, info.getPathSegments().size()).toASCIIString();
        URI terminateURI = TxSupport.getUri(info, info.getPathSegments().size(),terminator);
        URI volatileURI = TxSupport.getUri(info, info.getPathSegments().size(), TxLinkNames.VOLATILE_PARTICIPANT);

        coordinatorElement.setTxnURI(txnURI);
        coordinatorElement.setTerminateURI(terminateURI.toASCIIString());
        coordinatorElement.setDurableParticipantEnlistmentURI(txnURI);
        coordinatorElement.setVolatileParticipantEnlistmentURI(volatileURI.toASCIIString());

        txn.getParticipants(enlistmentIds);

        for (String enlistmentId : enlistmentIds) {
            Map<String, String> participantInfo = participants.get(enlistmentId);
            String terminatorURI = participantInfo.get(TxLinkNames.PARTICIPANT_TERMINATOR);
            String participantURI = participantInfo.get(TxLinkNames.PARTICIPANT_RESOURCE);
            String recoveryURI = participantInfo.get(TxLinkNames.PARTICIPANT_RECOVERY);
            TwoPhaseAwareParticipantElement participantElement = new TwoPhaseAwareParticipantElement();

            participantElement.setTerminatorURI(terminatorURI);
            participantElement.setRecoveryURI(recoveryURI);
            participantElement.setResourceURI(participantURI);
            txn.getStatus(participantElement, participantURI);

            coordinatorElement.addTwoPhaseAware(participantElement);
        }

        return addTransactionHeaders(Response.ok(coordinatorElement), info, txn, false).build();
    }

    /**
     * Performing a DELETE on the transaction-coordinator URL will return a 403.
     * @param id transaction id
     * @return 403
     */
    @SuppressWarnings({"UnusedDeclaration"})
    @DELETE
    @Path(TxSupport.TX_SEGMENT + "{id}")
    public Response deleteTransaction(@PathParam("id") String id) {
        return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
    }

    // Performing HEAD, GET, POST, DELETE and OPTIONS on the transaction
    // url generates a 400 status code
    @SuppressWarnings({"UnusedDeclaration"})
    @HEAD @Path(TxSupport.TX_SEGMENT + "{TxId}/" + TxLinkNames.TERMINATOR)
    public Response tt1(@PathParam("TxId")String txId) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }
    @SuppressWarnings({"UnusedDeclaration"})
    @GET @Path(TxSupport.TX_SEGMENT + "{TxId}/" + TxLinkNames.TERMINATOR)
    public Response tt2(@PathParam("TxId")String txId) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }
    @SuppressWarnings({"UnusedDeclaration"})
    @POST @Path(TxSupport.TX_SEGMENT + "{TxId}/" + TxLinkNames.TERMINATOR)
    public Response tt3(@PathParam("TxId")String txId) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }
    @SuppressWarnings({"UnusedDeclaration"})
    @DELETE @Path(TxSupport.TX_SEGMENT + "{TxId}/" + TxLinkNames.TERMINATOR)
    public Response tt4(@PathParam("TxId")String txId) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }
    @SuppressWarnings({"UnusedDeclaration"})
    @OPTIONS @Path(TxSupport.TX_SEGMENT + "{TxId}/" + TxLinkNames.TERMINATOR)
    public Response tt5(@PathParam("TxId")String txId) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
    }

    private Response.ResponseBuilder addTransactionHeaders(Response.ResponseBuilder builder, UriInfo info,
                                                           Transaction tx, boolean includeTxId) {
        String uid = tx.get_uid().fileStringForm();
        String terminator = TxLinkNames.TERMINATOR;
        String participant = TxLinkNames.PARTICIPANT;
        String vparticipant = TxLinkNames.VOLATILE_PARTICIPANT;

        if (includeTxId) {
            TxSupport.addLinkHeader(builder, info, terminator, terminator, uid, terminator);
            TxSupport.addLinkHeader(builder, info, participant, participant, uid);
            TxSupport.addLinkHeader(builder, info, vparticipant, vparticipant, uid, vparticipant);
        } else {
            TxSupport.addLinkHeader(builder, info, terminator, terminator, terminator);
            TxSupport.addLinkHeader(builder, info, participant, participant);
            TxSupport.addLinkHeader(builder, info, vparticipant, vparticipant, vparticipant);
        }

        return builder;
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
     * @see org.jboss.jbossts.star.util.TxLinkNames#TERMINATOR and @see TxLinkNames#PARTICIPANT
     *
     * @param info uri context
     * @param headers http headers
     * @param content empty if no transaction timeout is required otherwise the number of milliseconds
     * after which the transaction is eligible for being timed out. The content should have the format
     * TxSupport#TIMEOUT_PROPERTY milliseconds
     * @return http status code
     */
    @SuppressWarnings({"UnusedDeclaration"})
    @POST
    @Path(TxSupport.TX_SEGMENT)
    @Consumes(TxMediaType.POST_MEDIA_TYPE)
//    @Produces("application/vnd.rht.txstatus+text;version=0.1")
    public Response beginTransaction(@Context UriInfo info, @Context HttpHeaders headers, @DefaultValue("") String content) {
        log.tracef("coordinator: POST /transaction-manager content: %s", content);
        Transaction tx = new Transaction(this, "coordinator");
        int timeout = TxSupport.getIntValue(content, TxMediaType.TIMEOUT_PROPERTY, 0); // default is 0 - never timeout
        String uid = tx.get_uid().fileStringForm();

        log.tracef("coordinator: timeout=%d", timeout);
        transactions.put(uid, tx);

        // round up the timeout from milliseconds to seconds
        if (timeout != 0) {
            if (timeout < 0)
                timeout = 0;
            else
                timeout /= 1000;
        }

        int status = tx.begin(timeout);

        try {
            if (status == ActionStatus.RUNNING) {
                active.incrementAndGet();

                URI uri1 = TxSupport.getUri(info, info.getPathSegments().size(), uid);
                Response.ResponseBuilder builder = Response.created(uri1);
                return addTransactionHeaders(builder, info, tx, true).build();
            }

            throw new TransactionStatusException("Transaction failed to start: " + status);
        } catch (Exception e) {
            RESTATLogger.atI18NLogger.warn_failedToStartTransactionCorrdinator(e);
            throw new TransactionStatusException("Transaction failed to start: " + e);
        } finally {
            AtomicAction.suspend();
        }
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
     *  @see TxStatus#TransactionCommitted etc
     * @return http response code
    */
    @PUT
    @Path(TxSupport.TX_SEGMENT + "{TxId}/" + TxLinkNames.TERMINATOR)
    public Response terminateTransaction(@PathParam("TxId")String txId, @QueryParam("fault") @DefaultValue("")String fault, String content) {
        log.tracef("coordinator: commit: transaction-manager/%s/terminator : content: %s", txId, content);

        Transaction tx = getTransaction(txId);
        String how = TxSupport.getStringValue(content, TxStatusMediaType.STATUS_PROPERTY);
        TxStatus currentStatus = tx.getTxStatus();
        String status;
        int scRes;

        /*
         * 275If the transaction no longer exists then an implementation MAY return 410 if the implementation
         * 276records information about transactions that have rolled back, (not necessary for presumed
         * 277rollback semantics) but at a minimum MUST return 404.
         */
        if (!currentStatus.isRunning() && !currentStatus.isRollbackOnly())
            return Response.status(HttpURLConnection.HTTP_PRECON_FAILED).build();

        /*
            ABORT_ONLY is not in the spec for the same reasons as it's not in the WS-TX and WS-CAF where
            it is assumed that only the txn originator can end the tx:
            - simply register a synchronization in the transaction that prevented a commit from happening;
            and I haven't implemented synchronisations yet.
            It is unclear why allowing similar functionality via synchronisations doesn't open up a similar
            security hole.
       */
        TxStatus txStatus = TxStatus.fromStatus(how);
        tx.setFault(fault);
        AtomicAction.resume(tx);

        switch (txStatus) {
            case TransactionCommitted:
                prepared.incrementAndGet();
                status = tx.getStatus(tx.commit(true));
                break;
            case TransactionRolledBack:
                prepared.incrementAndGet();
                status = tx.getStatus(tx.abort());
                break;
            case TransactionRollbackOnly:
                tx.preventCommit();
                status = tx.getStatus();
                break;
            default:
                AtomicAction.suspend();
                return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }

        AtomicAction.suspend();

        log.tracef("terminate result: %s", status);

/*        if (tx.isRunning())
            throw new TransactionStatusException("Transaction failed to terminate");*/

        // Cleanup is done as part of the org.jboss.jbossts.star.resource.Transaction.afterCompletion()

        scRes = status.length() == 0 ? HttpURLConnection.HTTP_INTERNAL_ERROR : HttpURLConnection.HTTP_OK;

        return Response.status(scRes).entity(TxSupport.toStatusContent(status)).build();
    }

    public void removeTxState(int status, Transaction tx, final Collection<String> enlistmentIds) {

        String txId = tx.get_uid().fileStringForm();
        transactions.remove(txId);

        prepared.decrementAndGet();
        active.decrementAndGet();
        if (status == ActionStatus.COMMITTED)
            committed.incrementAndGet();
        else if (status == ActionStatus.ABORTED)
            aborted.incrementAndGet();

        if (enlistmentIds == null) {
            // Cleanup synchronization could not pass in the participants (tx timed out)
            // locate the enlistment ids
            Iterator<Entry<String, HashMap<String, String>>> j = participants.entrySet().iterator();
            while (j.hasNext()) {
                Map.Entry<java.lang.String, HashMap<String, String>> entry = j.next();
                HashMap<String, String> linkHolder = entry.getValue();
                String participantTxId = linkHolder.get(TxLinkNames.TRANSACTION);
                if (txId.equals(participantTxId)) {
                    j.remove();
                }
            }
        } else {
            for (String enlistmentId : enlistmentIds) {
                participants.remove(enlistmentId);
            }
        }
    }

    /**
     * Register a participant in a tx
     * @param linkHeader link header
     * @param info  URI info
     * @param txId id of transaction
     * @param content body of request containing URI for driving the participant through completion
     *  (the URI should be unique within the scope of txId)
     * @return unique resource ref for the participant
     */
    @POST
    @Path(TxSupport.TX_SEGMENT + "{TxId}")
    public Response enlistParticipant(@HeaderParam("Link") String linkHeader, @Context UriInfo info,
                                      @PathParam("TxId")String txId, String content) {
        log.tracef("enlistParticipant request uri %s txid: %s Link: %s content: %s",
                info.getRequestUri(), txId,
                linkHeader != null ? linkHeader : "null",
                content != null ? content : "null");

        Transaction tx = getTransaction(txId);

        /*
         * If the transaction is not TransactionActive then the implementation MUST return a 412 status
         * code
         */
        if (!tx.isRunning())
            return Response.status(HttpURLConnection.HTTP_PRECON_FAILED).build();

        Map<String, String> links = TxSupport.decodeLinkHeader(linkHeader);
//        Map<String, String> links = new HashMap<String, String>();
//        for (Map.Entry<String, Link> link : linkHeader.getLinksByRelationship().entrySet())
//            links.put(link.getKey(), link.getValue().getHref());

        if (links.containsKey(TxLinkNames.VOLATILE_PARTICIPANT))
            tx.addVolatileParticipant(links.get(TxLinkNames.VOLATILE_PARTICIPANT));

        String participantURI = links.get(TxLinkNames.PARTICIPANT_RESOURCE);
        String terminatorURI = links.get(TxLinkNames.PARTICIPANT_TERMINATOR);

        if (participantURI == null)
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                    .entity("Missing Enlistment Link Header").build();

        String txURI = TxSupport.buildURI(info.getBaseUriBuilder(), info.getPathSegments().get(0).getPath(),
                info.getPathSegments().get(1).getPath());
        UriBuilder builder = info.getBaseUriBuilder();
        String recoveryUrlBase = builder.path(info.getPathSegments().get(0).getPath()).path(RC_SEGMENT)
                .build().toString() + '/';
        String coordinatorId;

        if (tx.isEnlisted(participantURI)) {
            return Response.created(URI.create(tx.getRecoveryUrl())).build(); // participant is already enlisted
            // was return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity("participant is already enlisted").build();
        }

        if (terminatorURI == null) {
            String commitURI = links.get(TxLinkNames.PARTICIPANT_COMMIT);
            String prepareURI = links.get(TxLinkNames.PARTICIPANT_PREPARE);
            String rollbackURI = links.get(TxLinkNames.PARTICIPANT_ROLLBACK);
            String commitOnePhaseURI = links.get(TxLinkNames.PARTICIPANT_COMMIT_ONE_PHASE);

            if (commitURI == null || prepareURI == null || rollbackURI == null)
                return Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                        .entity("Missing TwoPhaseUnawareLink Header").build();

            coordinatorId = tx.enlistParticipant(txURI, participantURI, recoveryUrlBase,
                    commitURI, prepareURI, rollbackURI, commitOnePhaseURI);
        } else {
            coordinatorId = tx.enlistParticipant(txURI, participantURI, recoveryUrlBase, terminatorURI);
        }

        if (coordinatorId == null) // the request was rejected (2PC processing must have started)
            return Response.status(HttpURLConnection.HTTP_FORBIDDEN).entity("2PC has started").build();

        links.put(TxLinkNames.PARTICIPANT_RECOVERY, tx.getRecoveryUrl());
        links.put(TxLinkNames.TRANSACTION, txId);

        participants.put(coordinatorId, new HashMap<String, String>(links));

        log.debug("enlisted participant: content=" + content + " in tx " + txId + " Coordinator url base: " + recoveryUrlBase);

        return Response.created(URI.create(tx.getRecoveryUrl())).build();
    }

    /**
     * Register a volatile participant in a tx
     *
     * @param linkHeader link header
     * @param info  URI info
     * @param txId id of transaction
     * @return HTTP status code
     */
    @PUT
    @Path(TxSupport.TX_SEGMENT + "{TxId}/" + TxLinkNames.VOLATILE_PARTICIPANT)
    public Response enlistVolatileParticipant(@HeaderParam("Link") String linkHeader, @Context UriInfo info,
                                      @PathParam("TxId")String txId) {
        log.tracef("enlistParticipant request uri %s txid:  %s", info.getRequestUri(), txId);
        Transaction tx = getTransaction(txId);

        if (tx.isFinishing())
            return Response.status(HttpURLConnection.HTTP_PRECON_FAILED).build();

        Map<String, String> links = TxSupport.decodeLinkHeader(linkHeader);
        String vparticipantURI = links.get(TxLinkNames.VOLATILE_PARTICIPANT);

        if (vparticipantURI == null)
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                    .entity("Missing Enlistment Link Header").build();

        tx.addVolatileParticipant(vparticipantURI);

        return Response.ok().build();
    }

    /**
     * Get the participant url (registered during enlistParticipant) corresponding to a resource reference
     * if the coordinator crashes - the participant list will be empty but this is ok if commit hasn't been
     * called since the TM uses presumed abort semantics.
     *
     * @param txId transaction id that this recovery url belongs to
     * @param enlistmentId the resource reference
     * @return the participant url
     */
    @GET
    @Path(RC_SEGMENT + "/{TxId}/{RecCoordId}")
    public Response lookupParticipant(@PathParam("TxId")String txId, @PathParam("RecCoordId")String enlistmentId) {
        log.tracef("coordinator: lookup: transaction-coordinator: %s/%s", txId, enlistmentId);
        HashMap<String, String> p = participants.get(enlistmentId);

        if (p == null)
            return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build();

        String linkHeader = new TxSupport().makeTwoPhaseParticipantLinkHeader(p);

        if (linkHeader == null)
            return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build();

        return Response.ok().header("Link", linkHeader).build();
    }

    /**
     * PUT /recovery-coordinator/_RecCoordId_/_new participant URL_ -
     *   overwrite the old participant URL with new participant URL
     *   (as with JTS, this will also trigger off a recovery attempt on the associated transaction)
     * A participant may use this url to notifiy the coordinator that he has moved to a new location.
     *
     * @param linkHeader link header containing participant links
     * @param txId transaction id that this recovery url belongs to
     * @param enlistmentId id by the participant is known
     * @return http status code
     */
    @PUT
    @Path(RC_SEGMENT + "/{TxId}/{RecCoordId}")
    public Response replaceParticipant(@HeaderParam("Link") String linkHeader, @PathParam("TxId")String txId,
                                       @PathParam("RecCoordId")String enlistmentId) {
        Map<String, String> links = TxSupport.decodeLinkHeader(linkHeader);
        String terminatorUrl = links.get(TxLinkNames.PARTICIPANT_TERMINATOR);
        String participantUrl = links.get(TxLinkNames.PARTICIPANT_RESOURCE);

        if (participantUrl == null)
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity("Missing Link Header").build();

        log.tracef("coordinator: replace: recovery-coordinator/%s?URL=%s", enlistmentId, terminatorUrl);

        // check whether the transaction or log still exists
        Transaction tx = getTransaction(txId); // throws not found exception if the txn has finished

        links.put(TxLinkNames.TRANSACTION, txId);
        links.put(TxLinkNames.PARTICIPANT_RECOVERY, tx.getRecoveryUrl());

        participants.put(enlistmentId, new HashMap<String, String>(links));

        return Response.status(HttpURLConnection.HTTP_OK).build();
    }

    @POST
    @Path(RC_SEGMENT + "/{RecCoordId}")
    public Response postParticipant(@PathParam("RecCoordId")String enlistmentId) {
        log.tracef("coordinator: replace via Post: recovery-coordinator/%s", enlistmentId);
        return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).build();
    }

    /**
     * Performing DELETE on participant's recovery URL removes participant from the transaction.
     *
     * @param enlistmentId The resource reference
     * @return HTTP status code
     */
    @DELETE
    @Path(RC_SEGMENT + "/{RecCoordId}")
    public Response deleteParticipant(@PathParam("RecCoordId")String enlistmentId) {
        log.tracef("coordinator: participant leaving via Delete: recovery-coordinator/%s", enlistmentId);
        HashMap<String, String> p = participants.get(enlistmentId);
        Transaction txn;

        if (p == null || (txn = transactions.get(p.get(TxLinkNames.TRANSACTION))) == null)
            return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build();

        if (txn.forgetParticipant(p.get(TxLinkNames.PARTICIPANT_RESOURCE)))
            return Response.status(HttpURLConnection.HTTP_OK).build();

        return Response.status(HttpURLConnection.HTTP_CONFLICT).build();
    }

    private Transaction getTransaction(String txId) {
        Transaction tx = transactions.get(txId);

        if (tx == null) {
            updateTransactions();

            if ((tx = transactions.get(txId)) == null)
                throw new ResourceNotFoundException("Transaction id not found");
        }

        return tx;
    }

    // Look up all log records for a given type
    private static Set<Uid> getUids(Set<Uid> uids, String type) {
        try {
            RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
            InputObjectState states = new InputObjectState();

            if (recoveryStore.allObjUids(type, states) && states.notempty()) {
                boolean finished = false;

                do {
                    Uid uid = UidHelper.unpackFrom(states);

                    if (uid.notEquals(Uid.nullUid())) {
                        uids.add(uid);
                    } else {
                        finished = true;
                    }

                } while (!finished);
            }
        } catch (Exception e) {
            RESTATLogger.atI18NLogger.warn_getUidsVolatileParticipantResource(e.getMessage(), e);
        }

        return uids;
    }

    private void updateTransactions() {
        Map<String, RecoveringTransaction> txns = new HashMap<String, RecoveringTransaction>(recoveringTransactions);

        // remove all those uids that are still recovering
        for (Uid uid : getUids(new HashSet<Uid>(), REST_TXN_TYPE)) {
            txns.remove(uid.fileStringForm());
        }

        // the remaining entries must have been recovered
        for (String txId : txns.keySet()) {
            recoveringTransactions.remove(txId);
            transactions.remove(txId);
        }
    }

    private static Map<String, RecoveringTransaction> getRecoveringTransactions(Map<String, Transaction> transactions) {
        Map<String, RecoveringTransaction> recoveringTransactions = new ConcurrentHashMap<String, RecoveringTransaction>();

        for (Uid uid : getUids(new HashSet<Uid>(), REST_TXN_TYPE)) {
            String key =  uid.fileStringForm();
            RecoveringTransaction txn = new RecoveringTransaction(uid);

            try {
                // the recoverable transaction contains the recovery urls of each of its participants
                // so it needs activate in order to make it available to anyone that wants to obtain it:
                if (txn.activate()) {
                    for (RESTRecord r : txn.getParticipants(new ArrayList<RESTRecord>())) {
                        Map<String, String> links = new HashMap<String, String>();

                        links.put(TxLinkNames.PARTICIPANT_RECOVERY, r.getRecoveryURI());
                        links.put(TxLinkNames.TRANSACTION, r.getTxId());

                        participants.put(r.getCoordinatorURI(), new HashMap<>(links));
                    }
                }
            } catch (Throwable e) {
                RESTATLogger.atI18NLogger.warn_getRecoveringTransactions(e.getMessage(), e,
                txn.get_uid().fileStringForm());
            }

            recoveringTransactions.put(key, txn);
            transactions.put(key, txn);
        }

        return recoveringTransactions;
    }
}
