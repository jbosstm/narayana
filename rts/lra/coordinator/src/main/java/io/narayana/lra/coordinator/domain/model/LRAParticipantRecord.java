/*
 * SPDX short identifier: Apache-2.0
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
import io.narayana.lra.LRAData;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.logging.LRALogger;
import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.AsyncInvoker;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static io.narayana.lra.LRAConstants.AFTER;
import static io.narayana.lra.LRAConstants.NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME;
import static io.narayana.lra.LRAConstants.PARTICIPANT_TIMEOUT;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_PARENT_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;

public class LRAParticipantRecord extends AbstractRecord implements Comparable<AbstractRecord> {
    private static final String TYPE_NAME = "/StateManager/AbstractRecord/LRARecord";
    private static final String COMPENSATE_REL = "compensate";
    private static final String COMPLETE_REL = "complete";

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
    private String compensatorData;
    private String previousCompensatorData;
    private LRAService lraService;
    private ParticipantStatus status;
    private boolean accepted;
    private LongRunningAction lra;

    public LRAParticipantRecord() {
    }

    LRAParticipantRecord(LongRunningAction lra, LRAService lraService, String linkURI, String compensatorData) {
        super(new Uid());

        this.lra = lra;

        try {
            // if compensateURI is a link parse it into compensate,complete and status urls
            if (linkURI.startsWith("<")) {
                Exception[] parseException = {null};

                Arrays.stream(linkURI.split(",")).forEach((linkStr) -> {
                    Exception e = parseLink(linkStr);
                    if (e != null) {
                        parseException[0] = e;
                    }
                });

                if (parseException[0] != null) {
                    String errorMsg = LRALogger.i18nLogger.error_invalidCompensator(lra.getId(), parseException[0].getMessage(), linkURI);
                    LRALogger.logger.error(errorMsg);
                    trace_progress(errorMsg);
                    throw new WebApplicationException(errorMsg, parseException[0],
                            Response.status(BAD_REQUEST).entity(errorMsg).build());
                } else if (compensateURI == null && afterURI == null) {
                    String errorMsg = LRALogger.i18nLogger.error_missingCompensator(lra.getId(), linkURI);
                    LRALogger.logger.error(errorMsg);
                    trace_progress(errorMsg);
                    throw new WebApplicationException(errorMsg, Response.status(BAD_REQUEST).entity(errorMsg).build());
                }
            } else {
                this.compensateURI = new URI(String.format("%s/compensate", linkURI));
                this.completeURI = new URI(String.format("%s/complete", linkURI));
                this.statusURI = new URI(String.format("%s", linkURI));
                this.forgetURI = new URI(String.format("%s", linkURI));

            }

            this.lraId = lra.getId();
            this.parentId = lra.getParentId();
            this.status = ParticipantStatus.Active;

            this.lraService = lraService;
            this.participantPath = linkURI;

            this.recoveryURI = null;
            this.compensatorData = compensatorData;

            trace_progress("created");
        } catch (URISyntaxException e) {
            String logMsg = LRALogger.i18nLogger.error_invalidFormatToCreateLRAParticipantRecord(lraId.toASCIIString(), linkURI, e.getMessage());
            LRALogger.logger.error(logMsg);
            trace_progress(logMsg);

            throw new WebApplicationException(logMsg, e,
                    Response.status(BAD_REQUEST).entity(logMsg).build());
        }
    }

    void setLRA(LongRunningAction lra) {
        this.lra = lra;
        this.parentId = lra.getParentId();
    }

    String getParticipantPath() {
        return participantPath;
    }

    static String cannonicalForm(String linkStr) throws URISyntaxException {
        if (!linkStr.contains(">;")) {
            return new URI(linkStr).toASCIIString();
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

    static String extractCompensator(URI lraId, String linkStr) throws URISyntaxException {
        for (String lnk : linkStr.split(",")) {
            Link link;

            try {
                link = Link.valueOf(lnk);
            } catch (IllegalArgumentException e) {
                throw new URISyntaxException(lnk, e.getMessage());
            }

            if (COMPENSATE_REL.equals(link.getRel())) {
                return cannonicalForm(link.getUri().toString());
            }
        }

        return linkStr;
    }

    private static URI cannonicalURI(URI uri) throws URISyntaxException {
        return new URI(uri.getScheme(),
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath().replaceAll("//", "/"),
                uri.getQuery(), uri.getFragment());
    }

    private URISyntaxException parseLink(String linkStr) {
        Link link = Link.valueOf(linkStr);
        String rel = link.getRel();

        try {
            URI uri = cannonicalURI(link.getUri());

            if (COMPENSATE_REL.equals(rel)) {
                compensateURI = uri;
            } else if (COMPLETE_REL.equals(rel)) {
                completeURI = uri;
            } else if ("status".equals(rel)) {
                statusURI = uri;
            } else if (AFTER.equals(rel)) {
                afterURI = uri;
            } else if ("forget".equals(rel)) {
                forgetURI = uri;
            } else if ("participant".equals(rel)) {
                compensateURI = new URI(uri.toASCIIString() + "/compensate");
                completeURI = new URI(uri.toASCIIString() + "/complete");
                statusURI = forgetURI = uri;
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

    @Override
    // NB if a participant needs to know
    // if the lra closed then it can ask the io.narayana.lra.coordinator. A 404 status implies:
    // - all compensators completed ok, or
    // - all compensators compensated ok
    // This participant can infer which possibility happened since it will have been told to complete or compensate
    public int topLevelAbort() {
        return doEnd(lra.isCancel());
    }

    @Override
    public int topLevelOnePhaseCommit() {
        return topLevelCommit();
    }

    @Override
    public int topLevelCommit() {
        return doEnd(lra.isCancel());
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
        Client client = null;

        trace_progress("finishing");

        if (isFinished()) {
            return atEnd(status == ParticipantStatus.FailedToComplete || status == ParticipantStatus.FailedToCompensate
                    ? TwoPhaseOutcome.FINISH_ERROR : TwoPhaseOutcome.FINISH_OK);
        }

        if (ParticipantStatus.Compensating.equals(status)) {
            compensate = true;
        }

        if (compensateURI == null) {
            return atEnd(TwoPhaseOutcome.FINISH_OK);
        }

        if (compensate) {
            if (isCompensated()) {
                return atEnd(TwoPhaseOutcome.FINISH_OK); // the participant has already compensated
            }

            endPath = compensateURI; // we are going to ask the participant to compensate
            status = ParticipantStatus.Compensating;
        } else {
            if (isCompelete() || completeURI == null) {
                status = ParticipantStatus.Completed;

                return atEnd(TwoPhaseOutcome.FINISH_OK); // the participant has already completed
            }

            endPath = completeURI;  // we are going to ask the participant to complete
            status = ParticipantStatus.Completing;
        }

        // NB trying to compensate when already completed is allowed (for nested LRAs)

        int httpStatus = -1;

        if (accepted) {
            // the participant has previously returned a HTTP 202 Accepted response
            // to indicate that it is in progress in which case the status URI
            // must be valid so try that first for the status
            int twoPhaseOutcome = retryGetEndStatus(endPath, compensate);

            if (twoPhaseOutcome != -1) {
                return atEnd(twoPhaseOutcome);
            }
        } else {
            httpStatus = tryLocalEndInvocation(endPath); // see if participant is in the same JVM
        }

        if (httpStatus == -1) {
            // the local invocation was not made so fallback to using JAX-RS

            trace_progress("notifying participant");

            try {
                // ask the participant to complete or compensate
                client = ClientBuilder.newClient();
                Response response = client.target(endPath)
                        .request()
                        .header(LRA_HTTP_CONTEXT_HEADER, lraId.toASCIIString())
                        .header(LRA_HTTP_PARENT_CONTEXT_HEADER, parentId) // make the context available to participants
                        .header(LRA_HTTP_RECOVERY_HEADER, recoveryURI.toASCIIString())
                        .header(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME, compensatorData)
                        .async()
                        .put(Entity.text(""))
                        .get(PARTICIPANT_TIMEOUT, TimeUnit.SECONDS);

                httpStatus = response.getStatus();

                accepted = httpStatus == Response.Status.ACCEPTED.getStatusCode();

                if (accepted && statusURI == null && response.getHeaderString(HttpHeaders.LOCATION) != null) {
                    // the participant could not finish immediately and we have no status URI so one should be
                    // present in the Location header
                    statusURI = URI.create(response.getHeaderString(HttpHeaders.LOCATION));
                }

                if (httpStatus == Response.Status.GONE.getStatusCode()) {
                    updateStatus(compensate);
                    return atEnd(TwoPhaseOutcome.FINISH_OK); // the participant must have finished ok but we lost the response
                }

                if (response.hasEntity()) {
                    responseData = response.readEntity(String.class);
                }
            } catch (Exception e) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("LRAParticipantRecord.doEnd(%s) HTTP PUT at %s failed for LRA %s (reason: %s)",
                            compensate ? "compensate" : "complete", endPath, lraId, e.getMessage());
                    if (LRALogger.logger.isDebugEnabled()) {
                        LRALogger.logger.debug("LRAParticipantRecord.doEnd stacktrace", e);
                    }
                }
            } finally {
                trace_progress("notified participant");

                if (client != null) {
                    client.close();
                }
            }
        }

        if (responseData != null &&
                httpStatus == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            // the body should contain a valid ParticipantStatus
            try {
                return atEnd(reportFailure(compensate, endPath,
                        ParticipantStatus.valueOf(responseData).name()));
            } catch (IllegalArgumentException ignore) {
                // ignore the body and let recovery discover the status of the participant
            }
        }

        if (httpStatus != Response.Status.OK.getStatusCode()
                && httpStatus != Response.Status.NO_CONTENT.getStatusCode()
                && !accepted) {
            if (LRALogger.logger.isDebugEnabled()) {
                LRALogger.logger.debugf("LRAParticipantRecord.doEnd put %s failed with status: %d",
                        endPath, httpStatus);
            }

            // recovery will figure out the status via the status url
            status = compensate ? ParticipantStatus.Compensating : ParticipantStatus.Completing;
            accepted = true;
            trace_progress("notify participant failed");
        }

        updateStatus(compensate);

        // if the the request is still in progress (ie accepted is true) let recovery finish it
        return atEnd(accepted ? TwoPhaseOutcome.HEURISTIC_HAZARD : TwoPhaseOutcome.FINISH_OK);
    }

    boolean isFinished() {
        // nested participants must still be able to compensate even if they are closed
        if (compensateURI == null) {
            return afterURI != null;
        }

        switch (status) {
            case Completed:
                /* FALLTHRU */
            case FailedToComplete:
                return parentId == null; // completed nested LRAs must remain cancellable
            case Compensated:
                /* FALLTHRU */
            case FailedToCompensate:
                return true;
            default:
                return false;
        }
    }


    boolean isFailed() {
        return status == ParticipantStatus.FailedToCompensate || status == ParticipantStatus.FailedToComplete;
    }

    private boolean afterLRARequest(URI target, String payload) {
        Client client = null;

        try {
            client = ClientBuilder.newClient();
            Invocation.Builder builder = client.target(target)
                .request()
                .header(LRA_HTTP_RECOVERY_HEADER, recoveryURI.toASCIIString())
                .header(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME, compensatorData);

            if (target.equals(afterURI)) {
                builder.header(LRA.LRA_HTTP_ENDED_CONTEXT_HEADER, lra.getId().toASCIIString());
                if (lra.getParentId() != null) {
                    builder.header(LRA_HTTP_PARENT_CONTEXT_HEADER, lra.getParentId().toASCIIString());
                }
            } else {
                builder.header(LRA.LRA_HTTP_CONTEXT_HEADER, lra.getId().toASCIIString());
            }

            Future<Response> responseFuture =  target.equals(forgetURI) ? builder.async().delete()
                : builder.async().put(Entity.text(payload));
            Response response = responseFuture.get(PARTICIPANT_TIMEOUT, TimeUnit.SECONDS);

            if (response.getStatus() == 200) {
                trace_progress("notified participant");
                return true;
            }
        } catch (Exception e) {
            LRALogger.i18nLogger.warn_cannotNotifyAfterLRAURI(target, e);
        } finally {
            trace_progress("finished notifying participant");
            if (client != null) {
                client.close();
            }
        }

        return false;
    }

    private int atEnd(int res) {
        if (parentId != null
                && (status == ParticipantStatus.Completed || status == ParticipantStatus.FailedToComplete)) {
            if (lraService.getLRA(parentId).getStatus() == LRAStatus.Active) {
                // completed nested participants must remain compensatable
                return TwoPhaseOutcome.HEURISTIC_HAZARD; // ask to be called again
            } else {
                // the parent is finishing so this is the post LRA invocation
                return runPostLRAActions();
            }
        }

        // Only run the post LRA actions if both the LRA and participant are in an end state
        // check the participant first since it will have been removed from one of the lists
        if (!isFinished() || !lra.isFinished()) {
            if (afterURI != null) {
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
            }

            return res;
        }

        return runPostLRAActions();
    }

    private int runPostLRAActions() {
        // TODO investigate whether we can use a sync which works in recovery scenarios
        LRAStatus lraStatus = lra.getLRAStatus();
        boolean report = false;

        if (lraStatus == LRAStatus.Cancelling) {
            report = isFailed();
            lraStatus = report ? LRAStatus.FailedToCancel : LRAStatus.Cancelled;
        } else if (lraStatus == LRAStatus.Closing) {
            report = isFailed();
            lraStatus = report ? LRAStatus.FailedToClose : LRAStatus.Closed;
        }

        if (afterURI == null || afterLRARequest(afterURI, lraStatus.name())) {
            afterURI = null;

            trace_progress("runPostLRAActions with afterURI");
            // the post LRA actions succeeded so remove the participant from the intentions list otherwise retry
            return report ? reportFailure(lraStatus.name()) : TwoPhaseOutcome.FINISH_OK;
        }

        trace_progress("runPostLRAActions");

        return report ? reportFailure(lraStatus.name()) : TwoPhaseOutcome.HEURISTIC_HAZARD;
    }

    private void updateStatus(boolean compensate) {
        if (compensate) {
            status = accepted ? ParticipantStatus.Compensating : ParticipantStatus.Compensated;
        } else {
            status = accepted ? ParticipantStatus.Completing : ParticipantStatus.Completed;
        }
    }

    private int reportFailure(String failureReason) {
        if (status == ParticipantStatus.FailedToCompensate) {
            return reportFailure(true, compensateURI, failureReason);
        } else { // must be ParticipantStatus.FailedToComplete
            return reportFailure(false, completeURI, failureReason);
        }
    }

    private int reportFailure(boolean compensate, URI endPath, String failureReason) {
        status = compensate ? ParticipantStatus.FailedToCompensate : ParticipantStatus.FailedToComplete;

        trace_progress("reportFailure");
        LRALogger.logger.warnf("LRAParticipantRecord: participant %s reported a failure to %s (cause %s)",
                endPath, compensate ? COMPENSATE_REL : COMPLETE_REL, failureReason);

        // permanently failed so ask recovery to ignore us in the future.
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    private int retryGetEndStatus(URI endPath, boolean compensate) {
        assert accepted;

        // the participant has previously returned a HTTP 202 Accepted response so the status URI
        // must be valid - try that first for the status

        // first check that this isn't a nested coordinator running locally
        URI nestedLraId = extractParentLRA(endPath);

        trace_progress("retryGetEndStatus");

        if (nestedLraId != null && lraService != null) {
            LongRunningAction transaction = lraService.getTransaction(nestedLraId);

            if (transaction != null) {
                LRAStatus cStatus = transaction.getLRAStatus();

                trace_progress("retryGetEndStatus: local status " + cStatus);

                if (cStatus == null) {
                    LRALogger.logger.warnf("LRAParticipantRecord.retryGetEndStatus: local LRA %s accepted but has a null status",
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
                        return reportFailure(compensate, endPath, "unknown");
                    default:
                        return TwoPhaseOutcome.HEURISTIC_HAZARD;
                }
            }
        } else if (statusURI != null) {
            // it is a standard participant - check the status URI
            Response response;
            Client client = null;

            try {
                // since this method is called from the recovery thread do not block
                client = ClientBuilder.newClient();
                response = client.target(statusURI)//.path(getLRAId(lraId))
                        .request()
                        .header(LRA_HTTP_CONTEXT_HEADER, lraId.toASCIIString())
                        .header(LRA_HTTP_RECOVERY_HEADER, recoveryURI.toASCIIString())
                        .header(LRA_HTTP_PARENT_CONTEXT_HEADER, parentId)
                        .header(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME, compensatorData)
                        .async()
                        .get()
                        .get(PARTICIPANT_TIMEOUT, TimeUnit.SECONDS); // if the attempt times out the catch block below will return a heuristic

                // 200 and 410 are the only valid response code for reporting the participant status
                if (response.getStatus() == Response.Status.GONE.getStatusCode()) {
                    /*
                     * The specification states (in section 3.2.10. Reporting the status of a participant):
                     * If the participant has already responded successfully to an @Compensate or @Complete method
                     * invocation then it MAY report 410 Gone HTTP status code
                     *
                     * This means that if the participant was asked to compensate then it has now compensated, or
                     * if the participant was asked to complete then it has now completed.
                     */
                    status = compensate ? ParticipantStatus.Compensated : ParticipantStatus.Completed;
                    return TwoPhaseOutcome.FINISH_OK;
                } else if (response.getStatus() == Response.Status.ACCEPTED.getStatusCode() ||
                        Response.Status.Family.familyOf(response.getStatus()).equals(
                            Response.Status.Family.SERVER_ERROR)) {
                    // these response codes indicate that the implementation should retry later
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                } else if (response.getStatus() == Response.Status.OK.getStatusCode() &&
                        response.hasEntity()) {
                    // the participant is available again and has reported its status
                    status = ParticipantStatus.valueOf(response.readEntity(String.class));

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
                                    "LRAParticipantRecord.doEnd(compensate %b) get status %s did not finish: %s: WILL NOT RETRY",
                                    compensate, endPath, status);

                            if (forgetURI != null) {
                                if (!forget()) {
                                    // we will retry the forget on the next recovery cycle
                                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                                }
                            }

                            return reportFailure(compensate, endPath, "Unknown");
                        default:
                            return TwoPhaseOutcome.HEURISTIC_HAZARD;
                    }
                }
            } catch (Throwable e) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("LRAParticipantRecord.doEnd status URI %s is invalid (%s)",
                            statusURI, e.getMessage());
                }

                return TwoPhaseOutcome.HEURISTIC_HAZARD; // force recovery to keep retrying
            } finally {
                trace_progress("retryGetEndStatus");
                Current.pop();
                if (client != null) {
                    client.close();
                }
            }
        }

        return -1;
    }

    private Future<Response> getAsyncResponse(WebTarget target, String method, AsyncInvoker asyncInvoker, String cData) {
        String queryString = target.getUri().getQuery();

        if (queryString != null) {
            String[] queries = queryString.split("&");

            for (String pair : queries) {
                if (pair.contains("=")) {
                    String[] qp = pair.split("=");

                    if (qp[0].equals(LRAConstants.HTTP_METHOD_NAME)) {
                        switch (qp[1]) {
                            case "jakarta.ws.rs.GET":
                                return asyncInvoker.get();
                            case "jakarta.ws.rs.PUT":
                                return asyncInvoker.put(Entity.entity(cData, MediaType.TEXT_PLAIN));
                            case "jakarta.ws.rs.POST":
                                return asyncInvoker.post(Entity.entity(cData, MediaType.TEXT_PLAIN));
                            case "jakarta.ws.rs.DELETE":
                                return asyncInvoker.delete();
                            default:
                                break;
                        }
                    }
                }
            }
        }

        switch (method) {
            case "jakarta.ws.rs.GET":
                return asyncInvoker.get();
            case "jakarta.ws.rs.PUT":
                return asyncInvoker.put(Entity.entity(compensatorData, MediaType.TEXT_PLAIN));
            case "jakarta.ws.rs.POST":
                return asyncInvoker.post(Entity.entity(compensatorData, MediaType.TEXT_PLAIN));
            case "jakarta.ws.rs.DELETE":
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

            trace_progress("extractParentLRA: not local");
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
                    LRALogger.logger.infof("LRAParticipantRecord.doEnd invalid nested participant url %s" +
                                    "(should be compensate or complete)",
                            endPath);
                }

                httpStatus = BAD_REQUEST.getStatusCode();
            } else {
                LRAData inVMStatus = lraService.endLRA(cId, isCompensate, true);

                httpStatus = inVMStatus.getHttpStatus();
            }

            return httpStatus;
        }

        // fall back to using JAX-RS

        return -1;
    }

    boolean forget() {
        Client client = null;

        if (forgetURI != null) {
            try {
                client = ClientBuilder.newClient();
                Response response = client.target(forgetURI)//.path(getLRAId(lraId))
                    .request()
                    .header(LRA_HTTP_CONTEXT_HEADER, lraId)
                    .header(LRA_HTTP_RECOVERY_HEADER, recoveryURI)
                    .header(LRA_HTTP_PARENT_CONTEXT_HEADER, parentId)
                    .header(NARAYANA_LRA_PARTICIPANT_DATA_HEADER_NAME, compensatorData)
                    .async()
                    .delete()
                    .get(PARTICIPANT_TIMEOUT, TimeUnit.SECONDS);

                if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                    forgetURI = null; // succeeded so dispose of the endpoint
                    return true;
                }
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (!(e instanceof WebApplicationException) && cause instanceof WebApplicationException) {
                    e = (WebApplicationException) cause;
                }
                LRALogger.logger.infof("LRAParticipantRecord.forget delete %s failed for LRA %s (reason %s)",
                    forgetURI, lraId, e.getMessage());
                // TODO write a test to ensure that recovery only retries the forget request
                return false; // force recovery to keep retrying
            } finally {
                trace_progress("forget");
                Current.pop();
                if (client != null) {
                    client.close();
                }
            }

        } else {
            LRALogger.logger.warnf(
                "LRAParticipantRecord.forget() LRA: %s: cannot forget %s: missing forget URI, status: %s",
                lraId, recoveryURI, status);
        }

        return true;
    }

    private boolean isCompelete() {
        return status != null && status == ParticipantStatus.Completed;
    }

    private boolean isCompensated() {
        return status != null && status == ParticipantStatus.Compensated;
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
                LRALogger.i18nLogger.warn_saveState(e.getMessage());

                return false;
            } finally {
                trace_progress("saved");
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
                LRALogger.i18nLogger.warn_restoreState(e.getMessage());
                return false;
            } finally {
                trace_progress("restored");
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
        return os.unpackBoolean() ? new URI(Objects.requireNonNull(os.unpackString())) : null;
    }

    private static int getTypeId() {
        return RecordType.LRA_RECORD;
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
        return null;
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

    public URI getRecoveryURI() {
        return recoveryURI;
    }

    public String getParticipantURI() {
        return participantPath;
    }

    void setRecoveryURI(String recoveryURI) {
        try {
            this.recoveryURI = new URI(recoveryURI);
        } catch (URISyntaxException e) {
            String errorMsg = LRALogger.i18nLogger.error_invalidRecoveryUrlToJoinLRAURI(recoveryURI, lraId);

            LRALogger.logger.info(errorMsg);

            throw new WebApplicationException(errorMsg, e,
                    Response.status(BAD_REQUEST).entity(errorMsg).build());
        }
    }

    void setRecoveryURI(String recoveryUrlBase, String txId, String coordinatorId) {
        setRecoveryURI(recoveryUrlBase + txId + '/' + coordinatorId);
    }

    public String getCompensator() {
        return compensateURI != null ? compensateURI.toASCIIString() : null;
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

    public ParticipantStatus getStatus() {
        return status;
    }

    private String getLRAId(URI lraId) {
        String path = lraId.getPath();

        return path.substring(path.lastIndexOf('/') + 1);
    }

    public void setCompensatorData(String compensatorData) {
        String previous = this.compensatorData;

        this.previousCompensatorData = this.compensatorData;
        this.compensatorData = compensatorData;
    }

    public String getPreviousCompensatorData() {
        return previousCompensatorData;
    }

    private void trace_progress(String reason) {
        if (LRALogger.logger.isTraceEnabled()) {
            LRALogger.logger.tracef("%s: LRA id: %s, Participant id: %s, reason: %s, state: %s, accepted: %b",
                    LocalDateTime.now(ZoneOffset.UTC), // use the same time function as used for LRA timeouts
                    lraId,
                    participantPath,
                    reason,
                    status,
                    accepted);
        }
    }
}