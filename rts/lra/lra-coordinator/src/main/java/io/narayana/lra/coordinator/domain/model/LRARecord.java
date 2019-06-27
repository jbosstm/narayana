/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.coordinator.domain.model;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import io.narayana.lra.Current;
import io.narayana.lra.LRAConstants;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.logging.LRALogger;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static io.narayana.lra.LRAConstants.AFTER;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_PARENT_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

public class LRARecord extends AbstractRecord implements Comparable<AbstractRecord> {
    private static String TYPE_NAME = "/StateManager/AbstractRecord/LRARecord";
    private static long PARTICIPANT_TIMEOUT = 1; // number of seconds to wait for requests
    private static final String COMPENSATE_REL = "compensate";
    private static final String COMPLETE_REL = "complete";

    private Transaction lra;
    private URI lraId;
    private URI parentId;
    private URI recoveryURI;
    private String participantPath;

    private URI completeURI;
    private URI compensateURI;
    private URI statusURI;
    private URI forgetURI;
    private URI afterURI;

    private String responseData;
    private LocalTime cancelOn; // TODO make sure this acted upon during restore_state()
    private String compensatorData;
    private ScheduledFuture<?> scheduledAbort;
    private LRAService lraService;
    private ParticipantStatus status;
    boolean accepted;

    public LRARecord() {
    }

    LRARecord(LRAService lraService, String lraId, String linkURI, String compensatorData) {
        super(new Uid());

        try {
            // if compensateURI is a link parse it into compensate,complete and status urls
            if (linkURI.startsWith("<")) {
                linkURI = cannonicalForm(linkURI);
                Exception[] parseException = {null};

                Arrays.stream(linkURI.split(",")).forEach((linkStr) -> {
                    Exception e = parseLink(linkStr);
                    if (e != null) {
                        parseException[0] = e;
                    }
                });

                if (parseException[0] != null) {
                    throw new WebApplicationException(lraId + ": Invalid link URI: " + parseException[0], BAD_REQUEST);
                } else if (compensateURI == null && afterURI == null) {
                    throw new WebApplicationException(lraId + ": Invalid link URI: missing compensator", BAD_REQUEST);
                }
            } else {
                this.compensateURI = new URI(String.format("%s/compensate", linkURI));
                this.completeURI = new URI(String.format("%s/complete", linkURI));
                this.statusURI = new URI(String.format("%s", linkURI));
                this.forgetURI = new URI(String.format("%s", linkURI));

            }

            this.lraId = new URI(lraId);
            this.parentId = lraService.getTransaction(this.lraId).getParentId();

            this.lraService = lraService;
            this.participantPath = linkURI;

            this.recoveryURI = null;
            this.compensatorData = compensatorData;
        } catch (URISyntaxException e) {
            LRALogger.i18NLogger.error_invalidFormatToCreateLRARecord(lraId, linkURI);
            throw new WebApplicationException(lraId +  ": Invalid LRA id: " + e.getMessage(), BAD_REQUEST);
        }
    }

    String getParticipantPath() {
        return participantPath;
    }

    private static String cannonicalForm(String linkStr) {
        if (linkStr.indexOf(',') == -1) {
            return linkStr;
        }

        SortedMap<String, String> lm = new TreeMap<>();
        Arrays.stream(linkStr.split(",")).forEach(link -> lm.put(Link.valueOf(link).getRel(), link));
        StringBuilder sb = new StringBuilder();

        lm.forEach((k, v) -> appendLink(sb, v));

        return sb.toString();
    }

    private static StringBuilder appendLink(StringBuilder b, String value) {
        if (b.length() != 0) {
            b.append(',');
        }

        return b.append(value);
    }

    static String extractCompensator(URI lraId, String linkStr) {
        for (String lnk : linkStr.split(",")) {
            Link link;

            try {
                link = Link.valueOf(lnk);
            } catch (Exception e) {
                throw new WebApplicationException(Response.status(PRECONDITION_FAILED.getStatusCode())
                        .entity(String.format("Invalid compensator join request: cannot parse link '%s'", linkStr)).build());
            }

            if (COMPENSATE_REL.equals(link.getRel())) {
                return link.getUri().toString();
            }
        }

        return linkStr;
    }

    private URISyntaxException parseLink(String linkStr) {
        Link link = Link.valueOf(linkStr);
        String rel = link.getRel();
        String uri = link.getUri().toString();

        try {
            if (COMPENSATE_REL.equals(rel)) {
                compensateURI = new URI(uri);
            } else if (COMPLETE_REL.equals(rel)) {
                completeURI = new URI(uri);
            } else if ("status".equals(rel)) {
                statusURI = new URI(uri);
            } else if (AFTER.equals(rel)) {
                afterURI = new URI(uri);
            } else if ("forget".equals(rel)) {
                forgetURI = new URI(uri);
            } else if ("participant".equals(rel)) {
                compensateURI = new URI(uri + "/compensate");
                completeURI = new URI(uri + "/complete");
                statusURI = forgetURI = new URI(uri);
            }

            return null;
        } catch (URISyntaxException e) {
            return e;
        }
    }

    @Override
    public int topLevelPrepare() {
        return TwoPhaseOutcome.PREPARE_OK;
    }

    private void topLevelAbortFromTimer() {
        if (lra != null) {
            lra.timedOut(this);
        } else {
            doEnd(true);
        }
    }

    @Override
    // NB if a participant needs to know
    // if the lra completed then it can ask the io.narayana.lra.coordinator. A 404 status implies:
    // - all compensators completed ok, or
    // - all compensators compensated ok
    // This participant can infer which possibility happened since it will have been told to complete or compensate
    public int topLevelAbort() {
        return doEnd(true);
    }

    @Override
    public int topLevelOnePhaseCommit() {
        return topLevelCommit();
    }

    @Override
    public int topLevelCommit() {
        return doEnd(false);
    }

    private int doEnd(boolean compensate) {
        assert lraService != null;

        ReentrantLock lock = lraService.lockTransaction(lraId);

        try {
            return tryDoEnd(compensate);
        } finally {
            lock.unlock();
        }
    }

    private int tryDoEnd(boolean compensate) {
        URI endPath;

        // cancel any timer associated with this participant
        if (scheduledAbort != null) {
            // NB this could have been called from the scheduler so don't cancel our self!
            scheduledAbort.cancel(false);
            scheduledAbort = null;
        }

        if (ParticipantStatus.Compensating.equals(status)) {
            compensate = true;
        }

        if (compensateURI == null) {
            return TwoPhaseOutcome.FINISH_OK;
        }

        if (compensate) {
            if (isCompensated()) {
                return TwoPhaseOutcome.FINISH_OK; // the participant has already compensated
            }

            endPath = compensateURI; // we are going to ask the participant to compensate
            status = ParticipantStatus.Compensating;
        } else {
            if (isCompelete() || completeURI == null) {
                status = ParticipantStatus.Completed;

                return TwoPhaseOutcome.FINISH_OK; // the participant has already completed
            }

            endPath = completeURI;  // we are going to ask the participant to complete
            status = ParticipantStatus.Completing;
        }

        // NB trying to compensate when already completed is allowed (for nested LRAs)

        int httpStatus = -1;

        if (accepted) {
            // the participant has previously returned a HTTP 200 Accepted response
            // to indicate that it is in progress in which case the status URI
            // must be valid so try that first for the status
            int twoPhaseOutcome = retryGetEndStatus(endPath, compensate);

            if (twoPhaseOutcome != -1) {
                return twoPhaseOutcome;
            }
        } else {
            httpStatus = tryLocalEndInvocation(endPath); // see if participant is in the same JVM
        }

        if (httpStatus == -1) {
            // the local invocation was not made so fallback to using JAX-RS
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(URI.create(endPath.toASCIIString()));

            try {
                // ask the participant to complete or compensate
                AsyncInvoker asyncInvoker = target.request()
                        .header(LRA_HTTP_CONTEXT_HEADER, lraId.toASCIIString())
                        .header(LRA_HTTP_PARENT_CONTEXT_HEADER, parentId) // make the context available to participants
                        .header(LRA_HTTP_RECOVERY_HEADER, recoveryURI.toASCIIString())
                        .property(LRA_HTTP_CONTEXT_HEADER, lraId) // make the context available to the jaxrs filters
                        .property(LRA_HTTP_PARENT_CONTEXT_HEADER, parentId) // make the context available to jaxrs filters
                        .async();

                Future<Response> asyncResponse = getAsyncResponse(target, PUT.class.getName(), asyncInvoker, compensatorData, MediaType.WILDCARD);

                // the catch block below catches any Timeout exception
                Response response = asyncResponse.get(PARTICIPANT_TIMEOUT, TimeUnit.SECONDS);

                httpStatus = response.getStatus();

                accepted = httpStatus == Response.Status.ACCEPTED.getStatusCode();

                if (accepted && statusURI == null) {
                    // the participant could not finish immediately and we have no status URI so one should be
                    // present in the Location header
                    Object lh = response.getHeaders().getFirst(HttpHeaders.LOCATION);

                    if (lh != null) {
                        try {
                            statusURI = new URI((String) lh);
                        } catch (URISyntaxException e) {
                            if (LRALogger.logger.isInfoEnabled()) {
                                LRALogger.logger.infof("LRARecord.doEnd missing Location header on ACCEPTED response %s failed: %s",
                                        target.getUri(), e.getMessage());
                            }
                        }
                    }
                }

                if (httpStatus == Response.Status.NOT_FOUND.getStatusCode()) {
                    updateStatus(compensate);
                    return TwoPhaseOutcome.FINISH_OK; // the participant must have finished ok but we lost the response
                }

                if (response.hasEntity()) {
                    responseData = response.readEntity(String.class);
                }
            } catch (Exception e) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("LRARecord.doEnd put %s failed: %s",
                            target.getUri(), e.getMessage());
                }
            } finally {
                client.close();
            }
        }

        if (responseData != null) {
            String failureReason = null;

            if (httpStatus == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
                // the body should contain a valid ParticipantStatus
                try {
                    failureReason = ParticipantStatus.valueOf(responseData).name();
                } catch (IllegalArgumentException ignore) {
                    // ignore the body and let recovery discover the status of the participant
                }
            } else if (httpStatus == Response.Status.OK.getStatusCode()) {
                // see if any content contains a failed status
                if (compensate && LRAStatus.FailedToCancel.name().equals(responseData)) {
                    failureReason = responseData;
                } else if (!compensate && LRAStatus.FailedToClose.name().equals(responseData)) {
                    failureReason = responseData;
                } // else recovery will discover the status
            }

            if (failureReason != null) {
                return reportFailure(compensate, endPath.toASCIIString(), failureReason);
            }
        }

        if (httpStatus != Response.Status.OK.getStatusCode()
                && httpStatus != Response.Status.NO_CONTENT.getStatusCode()
                && !accepted) {
            if (LRALogger.logger.isDebugEnabled()) {
                LRALogger.logger.debugf("LRARecord.doEnd put %s failed with status: %d",
                        endPath, httpStatus);
            }

            if (compensate) {
                status = ParticipantStatus.Compensating; // recovery will figure out the status via the status url

                /*
                 * We are mapping compensate onto Abort. TwoPhaseCoordinator uses presumed abort
                 * so if we were to return FINISH_ERROR recovery would not replay the log.
                 * To force the record to be eligible for recovery we return a heuristic hazard.
                 */
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
            }

            status = ParticipantStatus.Completing; // recovery will figure out the status via the status url

            return TwoPhaseOutcome.FINISH_ERROR;
        }

        updateStatus(compensate);

        // if the the request is still in progress (ie accepted is true) let recovery finish it
        return accepted ? TwoPhaseOutcome.HEURISTIC_HAZARD : TwoPhaseOutcome.FINISH_OK;
    }

    private void updateStatus(boolean compensate) {
        if (compensate) {
            status = accepted ? ParticipantStatus.Compensating : ParticipantStatus.Compensated;
        } else {
            status = accepted ? ParticipantStatus.Completing : ParticipantStatus.Completed;
        }
    }

    private int reportFailure(boolean compensate, String endPath, String failureReason) {
        status = compensate ? ParticipantStatus.FailedToCompensate : ParticipantStatus.FailedToComplete;

        LRALogger.logger.warnf("LRARecord: participant %s reported a failure to %s (cause %s)",
                endPath, compensate ? COMPENSATE_REL : COMPLETE_REL, failureReason);

        // permanently failed so tell recovery to ignore us in the future. TODO could move it to
        // another list for reporting
        return TwoPhaseOutcome.FINISH_OK;
    }

    private int retryGetEndStatus(URI endPath, boolean compensate) {
        assert accepted;

        // the participant has previously returned a HTTP 202 Accepted response so the status URI
        // must be valid - try that first for the status

        // first check that this isn't a nested coordinator running locally
        URI nestedLraId = extractParentLRA(endPath);

        if (nestedLraId != null && lraService != null) {
            Transaction transaction = lraService.getTransaction(nestedLraId);

            if (transaction != null) {
                LRAStatus cStatus = transaction.getLRAStatus();

                if (cStatus == null) {
                    LRALogger.logger.warnf("LRARecord.retryGetEndStatus: local LRA %s accepted but has a null status",
                            endPath);
                    return -1; // shouldn't happen since it imples it's still be active - force end to be called
                }

                switch (cStatus) {
                    case Closed:
                    case Cancelled:
                        return TwoPhaseOutcome.FINISH_OK;
                    case Closing:
                    case Cancelling:
                        return TwoPhaseOutcome.HEURISTIC_HAZARD;
                    case FailedToClose:
                    case FailedToCancel:
                        return TwoPhaseOutcome.FINISH_ERROR;
                    default:
                        return TwoPhaseOutcome.HEURISTIC_HAZARD;
                }
            }
        } else if (statusURI != null) {
            // it is a standard participant - check the status URI
            Client client = ClientBuilder.newClient();

            try {
                WebTarget target = client.target(statusURI);

                // since this method is called from the recovery thread do not block
                AsyncInvoker asyncInvoker = target.request()
                        .header(LRA_HTTP_CONTEXT_HEADER, lraId.toASCIIString())
                        .property(LRA_HTTP_PARENT_CONTEXT_HEADER, parentId) // make the context available to participants
                        .header(LRA_HTTP_RECOVERY_HEADER, recoveryURI.toASCIIString())
                        .property(LRA_HTTP_CONTEXT_HEADER, lraId)  // make the context available to the jaxrs filters
                        .async();

                Future<Response> asyncResponse = getAsyncResponse(target, GET.class.getName(), asyncInvoker,
                        "", MediaType.TEXT_PLAIN);

                // if the attempt times out the catch block below will return a heuristic
                Response response = asyncResponse.get(PARTICIPANT_TIMEOUT, TimeUnit.SECONDS);

                // 200 is the only valid response code for reporting the participant status
                // NB 412 used to be used before the ParticipantStatus#Active state was added to the state model
                if (response.getStatus() == Response.Status.OK.getStatusCode() &&
                        response.hasEntity()) {
                    // the participant is available again and has reported its status
                    String s = response.readEntity(String.class);

                    status = ParticipantStatus.valueOf(s);

                    switch (status) {
                        case Completed:
                        case Compensated:
                            return TwoPhaseOutcome.FINISH_OK;
                        case Completing:
                        case Compensating:
                            // still in progress - make sure recovery keeps retrying it
                            return TwoPhaseOutcome.HEURISTIC_HAZARD;
                        case FailedToCompensate:
                        case FailedToComplete:
                            // the participant could not finish - log a warning and forget
                            LRALogger.logger.warnf(
                                    "LRARecord.doEnd(compensate %b) get status %s did not finish: %s: WILL NOT RETRY",
                                    compensate, target.getUri(), status);

                            if (forgetURI != null) {
                                try {
                                    // let the participant know he can clean up
                                    WebTarget target2 = client.target(forgetURI);
                                    AsyncInvoker asyncInvoker2 = target2.request()
                                            .header(LRA_HTTP_CONTEXT_HEADER, lraId.toASCIIString())
                                            .property(LRA_HTTP_PARENT_CONTEXT_HEADER, parentId) // make the context available to participants
                                            .header(LRA_HTTP_RECOVERY_HEADER, recoveryURI.toASCIIString())
                                            .property(LRA_HTTP_CONTEXT_HEADER, lraId)  // make the context available to the jaxrs filters
                                            .async();

                                    Future<Response> asyncResponse2 = getAsyncResponse(
                                            target, DELETE.class.getName(), asyncInvoker2, "", MediaType.TEXT_PLAIN);

                                    if (asyncResponse2.get(PARTICIPANT_TIMEOUT, TimeUnit.SECONDS).getStatus() ==
                                            Response.Status.OK.getStatusCode()) {
                                        return TwoPhaseOutcome.FINISH_OK;
                                    }
                                } catch (Exception e) {
                                    if (LRALogger.logger.isInfoEnabled()) {
                                        LRALogger.logger.infof("LRARecord.doEnd forget URI %s is invalid (%s)",
                                                forgetURI, e.getMessage());
                                    }

                                    // TODO write a test to ensure that recovery only retries the forget request
                                    return TwoPhaseOutcome.HEURISTIC_HAZARD; // force recovery to keep retrying
                                } finally {
                                    Current.pop();
                                }

                            } else {
                                LRALogger.logger.warnf(
                                        "LRARecord.doEnd(%b) LRA: %s: cannot forget %s: missing forget URI",
                                        compensate, lraId, statusURI, status);
                            }

                            if (compensate) {
                                return TwoPhaseOutcome.FINISH_OK;
                            }

                            return TwoPhaseOutcome.FINISH_OK;
                        default:
                            return TwoPhaseOutcome.HEURISTIC_HAZARD;
                    }
                }
            } catch (Throwable e) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("LRARecord.doEnd status URI %s is invalid (%s)",
                            statusURI, e.getMessage());
                }

                return TwoPhaseOutcome.HEURISTIC_HAZARD; // force recovery to keep retrying
            } finally {
                Current.pop();
                client.close();
            }
        }

        return -1;
    }

    private Future<Response> getAsyncResponse(WebTarget target, String method, AsyncInvoker asyncInvoker, String compensatorData, String mediaType) {
        String queryString = target.getUri().getQuery();

        if (queryString != null) {
            String[] queries = queryString.split("&");

            for (String pair : queries) {
                if (pair.contains("=")) {
                    String[] qp = pair.split("=");

                    if (qp[0].equals(LRAConstants.HTTP_METHOD_NAME)) {
                        switch (qp[1]) {
                            case "javax.ws.rs.GET":
                                return asyncInvoker.get();
                            case "javax.ws.rs.PUT":
                                return asyncInvoker.put(Entity.text(null));
                              //  return asyncInvoker.put(Entity.entity(compensatorData, mediaType));
                            case "javax.ws.rs.POST":
                                //return asyncInvoker.post(Entity.entity(compensatorData, mediaType));
                                return asyncInvoker.post(Entity.text(null));
                            case "javax.ws.rs.DELETE":
                                return asyncInvoker.delete();
                            default:
                                break;
                        }
                    }
                }
            }
        }

        switch (method) {
            case "javax.ws.rs.GET":
                return asyncInvoker.get();
            case "javax.ws.rs.PUT":
                return asyncInvoker.put(Entity.entity(compensatorData, mediaType));
            case "javax.ws.rs.POST":
                return asyncInvoker.post(Entity.entity(compensatorData, mediaType));
            case "javax.ws.rs.DELETE":
                return asyncInvoker.delete();
            default:
                return asyncInvoker.get();
        }
    }

    // see if the participant is an LRA in the same VM as the coordinator
    private URI extractParentLRA(URI endPath) {
        if (lraService != null) {
            String[] segments = endPath.getPath().split("/");
            int pCnt = segments.length;

            if (pCnt > 1) {
                String cId;

                try {
                    cId = URLDecoder.decode(segments[pCnt - 2], "UTF-8");

                    return lraService.hasTransaction(cId) ? new URI(cId) : null;
                } catch (UnsupportedEncodingException | URISyntaxException ignore) {
                }
            }
        }

        return null;
    }

    private int tryLocalEndInvocation(URI endPath) {
        URI cId = extractParentLRA(endPath);

        if (cId != null) {
            String[] segments = endPath.getPath().split("/");
            int pCnt = segments.length;

            // this is a call from a parent LRA to end the nested LRA:
            boolean isCompensate = COMPENSATE_REL.equals(segments[pCnt - 1]);
            boolean isComplete = COMPLETE_REL.equals(segments[pCnt - 1]);
            int httpStatus;

            if (!isCompensate && !isComplete) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("LRARecord.doEnd invalid nested participant url %s" +
                                    "(should be compensate or complete)",
                            endPath.toASCIIString());
                }

                httpStatus = BAD_REQUEST.getStatusCode();
            } else {
                LRAStatusHolder inVMStatus = lraService.endLRA(cId, isCompensate, true);

                httpStatus = inVMStatus.getHttpStatus();

                try {
                    responseData = inVMStatus.getEncodedResponseData();
                } catch (IOException ignore) {
                }
            }

            return httpStatus;
        }

        // fall back to using JAX-RS

        return -1;
    }

    boolean forget() {
        return true;
    }

    private boolean isCompelete() {
        return status != null && status == ParticipantStatus.Completed;
    }

    private boolean isCompensated() {
        return status != null && status == ParticipantStatus.Compensated;
    }

    String getResponseData() {
        return responseData;
    }

    @Override
    public boolean save_state(OutputObjectState os, int t) {
        if (super.save_state(os, t)) {
            try {
                packURI(os, lraId);
                packURI(os, compensateURI);
                packURI(os, recoveryURI);
                packURI(os, completeURI);
                packURI(os, afterURI);
                packURI(os, statusURI);
                packURI(os, forgetURI);
                packStatus(os);
                os.packString(participantPath);
                os.packString(compensatorData);
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean restore_state(InputObjectState os, int t) {
        if (super.restore_state(os, t)) {
            try {
                lraId = unpackURI(os);
                compensateURI = unpackURI(os);
                recoveryURI = unpackURI(os);
                completeURI = unpackURI(os);
                afterURI = unpackURI(os);
                statusURI = unpackURI(os);
                forgetURI = unpackURI(os);
                unpackStatus(os);
                participantPath = os.unpackString();
                compensatorData = os.unpackString();
                accepted = status == ParticipantStatus.Completing || status == ParticipantStatus.Compensating;
            } catch (IOException | URISyntaxException e) {
                return false;
            }
        }

        return true;
    }

    private void packStatus(OutputObjectState os) throws IOException {
        if (status == null) {
            os.packBoolean(false);
        } else {
            os.packBoolean(true);
            os.packInt(status.ordinal());
        }
    }

    private void unpackStatus(InputObjectState os) throws IOException {
        status = os.unpackBoolean() ? ParticipantStatus.values()[os.unpackInt()] : null;
    }

    private void packURI(OutputObjectState os, URI url) throws IOException {
        if (url == null) {
            os.packBoolean(false);
        } else {
            os.packBoolean(true);
            os.packString(url.toASCIIString());
        }
    }

    private URI unpackURI(InputObjectState os) throws IOException, URISyntaxException {
        return os.unpackBoolean() ? new URI(os.unpackString()) : null;
    }

    private static int getTypeId() {
        return RecordType.USER_DEF_FIRST0; // RecordType.LRA_RECORD; TODO we depend on thorntail for narayana which is using an earlier version
    }

    public int typeIs() {
        return getTypeId();
    }

    public int nestedAbort() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedPrepare() {
        return TwoPhaseOutcome.PREPARE_OK; // do nothing
    }

    public int nestedOnePhaseCommit() {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public String type() {
        return TYPE_NAME;
    }

    public boolean doSave() {
        return true;
    }

    public void merge(AbstractRecord a) {
    }

    public void alter(AbstractRecord a) {
    }

    public boolean shouldAdd(AbstractRecord a) {
        return (a.typeIs() == typeIs());
    }

    public boolean shouldAlter(AbstractRecord a) {
        return false;
    }

    public boolean shouldMerge(AbstractRecord a) {
        return false;
    }

    public boolean shouldReplace(AbstractRecord a) {
        return false;
    }

    @Override
    public Object value() {
        return null; // LRA does not support heuristics
    }

    @Override
    public void setValue(Object o) {
    }

    @Override
    public int compareTo(AbstractRecord other) {

        if (lessThan(other)) {
            return -1;
        }

        if (greaterThan(other)) {
            return 1;
        }

        return 0;
    }

    boolean setTimeLimit(ScheduledExecutorService scheduler, long timeLimit, Transaction lra) {
        this.lra = lra;

        return scheduleCancelation(this::topLevelAbortFromTimer, scheduler, timeLimit);
    }

    private boolean scheduleCancelation(Runnable runnable, ScheduledExecutorService scheduler, Long timeLimit) {
        if ((scheduledAbort != null && !scheduledAbort.cancel(false))) {
            return false;
        }

        if (timeLimit > 0) {
            cancelOn = LocalTime.now().plusNanos(timeLimit * 1000000);

            scheduledAbort = scheduler.schedule(runnable, timeLimit, TimeUnit.MILLISECONDS);
        } else {
            cancelOn = null;

            scheduledAbort = null;
        }

        return true;
    }

    public URI getRecoveryCoordinatorURI() {
        return recoveryURI;
    }

    public String getParticipantURI() {
        return participantPath;
    }

    void setRecoveryURI(String recoveryURI) {
        try {
            this.recoveryURI = new URI(recoveryURI);
        } catch (URISyntaxException e) {
            throw new WebApplicationException(recoveryURI + ": Invalid recovery id: " + e.getMessage(), BAD_REQUEST);
        }
    }

    void setRecoveryURI(String recoveryUrlBase, String txId, String coordinatorId) {
        setRecoveryURI(recoveryUrlBase + txId + '/' + coordinatorId);
    }

    public String getCompensator() {
        return compensateURI.toASCIIString();
    }

    void setLRAService(LRAService lraService) {
        this.lraService = lraService;
    }

    public void setLraService(LRAService lraService) {
        this.lraService = lraService;
    }

    public URI getEndNotificationUri() {
        return afterURI;
    }
}
