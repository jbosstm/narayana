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
import io.narayana.lra.annotation.CompensatorStatus;
import io.narayana.lra.client.Current;
import io.narayana.lra.client.InvalidLRAIdException;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.logging.LRALogger;

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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.narayana.lra.client.LRAClient.LRA_HTTP_HEADER;
import static io.narayana.lra.client.LRAClient.LRA_HTTP_RECOVERY_HEADER;

public class LRARecord extends AbstractRecord implements Comparable<AbstractRecord> {
    private static String TYPE_NAME = "/StateManager/AbstractRecord/LRARecord";

    private URL lraId;
    private URL recoveryURL;
    private String participantPath;

    private URL completeURI;
    private URL compensateURI;
    private URL statusURI;
    private URL forgetURI;

    private String responseData;
    private LocalTime cancelOn; // TODO make sure this acted upon during restore_state()
    private String compensatorData;
    private ScheduledFuture<?> scheduledAbort;
    private LRAService lraService;
    private CompensatorStatus status;
    boolean accepted;

    public LRARecord() {
    }

    LRARecord(LRAService lraService, String lraId, String linkURI, String compensatorData) {
        super(new Uid());

        try {
            // if compensateURI is a link parse it into compensate,complete and status urls
            if (linkURI.startsWith("<")) {
                linkURI = cannonicalForm(linkURI);
                Exception[] parseException = { null };

                Arrays.stream(linkURI.split(",")).forEach((linkStr) -> {
                    Exception e = parseLink(linkStr);
                    if (e != null)
                        parseException[0] = e;
                });

                if (parseException[0] != null)
                    throw new InvalidLRAIdException(lraId, "Invalid link URL", parseException[0]);
            } else {
                this.compensateURI = new URL(String.format("%s/compensate", linkURI));
                this.completeURI = new URL(String.format("%s/complete", linkURI));
                this.statusURI = new URL(String.format("%s", linkURI));
                this.forgetURI = new URL(String.format("%s", linkURI));

            }

            this.lraId = new URL(lraId);
            this.lraService = lraService;
            this.participantPath = linkURI;

            this.recoveryURL = null;
            this.compensatorData = compensatorData;
        } catch (MalformedURLException e) {
            LRALogger.i18NLogger.error_invalidFormatToCreateLRARecord(lraId, linkURI);
            throw new InvalidLRAIdException(lraId, "Invalid LRA id", e);
        }
    }

    String getParticipantPath() {
        return participantPath;
    }

    private static String cannonicalForm(String linkStr) {
        if (linkStr.indexOf(',') == -1)
            return linkStr;

        SortedMap<String, String> lm = new TreeMap<>();
        Arrays.stream(linkStr.split(",")).forEach(link -> lm.put(Link.valueOf(link).getRel(), link));
        StringBuilder sb = new StringBuilder();

        lm.forEach((k, v) -> appendLink(sb, v));

        return sb.toString();
    }

    private static StringBuilder appendLink(StringBuilder b, String value) {
        if (b.length() != 0)
            b.append(',');

        return b.append(value);
    }

    static String extractCompensator(String linkStr) {
        for (String lnk : linkStr.split(",")) {
            Link link;

            try {
                link = Link.valueOf(lnk);
            } catch (Exception e) {
                LRALogger.logger.infof(e, "Cannot extract compensator from link'%s'", linkStr);
                return linkStr;
            }

            if ("compensate".equals(link.getRel()))
                return link.getUri().toString();
        }

        return linkStr;
    }

    private MalformedURLException parseLink(String linkStr) {
        Link link = Link.valueOf(linkStr);
        String rel = link.getRel();
        String uri = link.getUri().toString();

        try {
            if ("compensate".equals(rel)) {
                compensateURI = new URL(uri);
            } else if ("complete".equals(rel)) {
                completeURI = new URL(uri);
            } else if ("status".equals(rel)) {
                statusURI = new URL(uri);
            } else if ("forget".equals(rel)) {
                forgetURI = new URL(uri);
            } else if ("participant".equals(rel)) {
                compensateURI = new URL(uri + "/compensate");
                completeURI = new URL(uri + "/complete");
                statusURI = forgetURI = new URL(uri);
            }

            return null;
        } catch (MalformedURLException e) {
            return e;
        }
    }

    @Override
    public int topLevelPrepare() {
        return TwoPhaseOutcome.PREPARE_OK;
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
        URL endPath;

        // cancel any timer associated with this participant
        if (scheduledAbort != null) {
            // NB this could have been called from the scheduler so don't cancel our self!
            scheduledAbort.cancel(false);
            scheduledAbort = null;
        }

        if (compensate) {
            if (isCompensated())
                return TwoPhaseOutcome.FINISH_OK; // the participant has already compensated

            endPath = compensateURI; // we are going to ask the participant to compensate
        } else {
            if (isCompelete() || completeURI == null) {
                status = CompensatorStatus.Completed;

                return TwoPhaseOutcome.FINISH_OK; // the participant has already completed
            }

            endPath = completeURI;  // we are going to ask the participant to complete
        }

        // NB trying to compensate when already completed is allowed

        Current.push(lraId); // make sure the lra id is set so that it can be included in the headers

        int httpStatus = -1;

        if (accepted) {
            // the participant has previously returned a HTTP 200 Accepted response
            // to indicate that it is in progress in which case the status URL
            // must be valid so try that first for the status
            int twoPhaseOutcome = retryGetEndStatus(endPath, compensate);

            if (twoPhaseOutcome != -1)
                return twoPhaseOutcome;
        } else {
            httpStatus = tryLocalEndInvocation(endPath); // see if participant is in the same JVM
        }

        if (httpStatus == -1) {
            // the local invocation was not made so fallback to using JAX-RS
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(URI.create(endPath.toExternalForm()));

            try {
                // ask the participant to complete or compensate
                Response response = target.request()
                        .header(LRA_HTTP_HEADER, lraId.toExternalForm())
                        .header(LRA_HTTP_RECOVERY_HEADER, recoveryURL.toExternalForm())
                        .put(Entity.entity(compensatorData, MediaType.APPLICATION_JSON));

                httpStatus = response.getStatus();

                accepted = httpStatus == Response.Status.ACCEPTED.getStatusCode();

                if (accepted && statusURI == null) {
                    // the participant could not finish immediately and we have no status URL so one should be
                    // present in the Location header
                    Object lh = response.getHeaders().getFirst(HttpHeaders.LOCATION);

                    if (lh != null)
                        try {
                            statusURI = new URL((String) lh);
                        } catch (MalformedURLException e) {
                            if (LRALogger.logger.isInfoEnabled()) {
                                LRALogger.logger.infof("LRARecord.doEnd missing Location header on ACCEPTED response %s failed: %s",
                                        target.getUri(), e.getMessage());
                            }
                        }
                }

                if (httpStatus == Response.Status.NOT_FOUND.getStatusCode()) {
                    return TwoPhaseOutcome.FINISH_OK; // the participant must have finished ok but we lost the response
                }
                if (response.hasEntity())
                    responseData = response.readEntity(String.class);
            } catch (Exception e) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("LRARecord.doEnd put %s failed: %s",
                            target.getUri(), e.getMessage());
                }
            } finally {
                client.close();
            }
        }

        if (httpStatus == Response.Status.OK.getStatusCode() && responseData != null) {
            // see if any content contains a failed status
            if (compensate && CompensatorStatus.FailedToCompensate.name().equals(responseData)) {
                return reportFailure(true, endPath.toExternalForm());
            } else if (!compensate && CompensatorStatus.FailedToComplete.name().equals(responseData)) {
                return reportFailure(false, endPath.toExternalForm());
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
                status = CompensatorStatus.Compensating; // recovery will figure out the status via the status url

                /*
                 * We are mapping compensate onto Abort. TwoPhaseCoordinator uses presumed abort
                 * so if we were to return FINISH_ERROR recovery would not replay the log.
                 * To force the record to be eligible for recovery we return a heuristic hazard.
                 */
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
            }

            status = CompensatorStatus.Completing; // recovery will figure out the status via the status url

            return TwoPhaseOutcome.FINISH_ERROR;
        }

        if (compensate)
            status = accepted ? CompensatorStatus.Compensating : CompensatorStatus.Compensated;
        else
            status = accepted ? CompensatorStatus.Completing : CompensatorStatus.Completed;

        // if the the request is still in progress (ie accepted is true) let recovery finish it
        return accepted ? TwoPhaseOutcome.HEURISTIC_HAZARD : TwoPhaseOutcome.FINISH_OK;
    }

    private int reportFailure(boolean compensate, String endPath) {
        status = compensate ? CompensatorStatus.FailedToCompensate : CompensatorStatus.FailedToComplete;

        LRALogger.logger.warnf("LRARecord: participant %s reported a failure to %s",
                endPath, compensate ? "compensate" : "complete");

        // permanently failed so tell recovery to ignore us in the future. TODO could move it to
        // another list for reporting
        return TwoPhaseOutcome.FINISH_OK;
    }

    private int retryGetEndStatus(URL endPath, boolean compensate) {
        assert accepted;

        // the participant has previously returned a HTTP 202 Accepted response so the status URL
        // must be valid - try that first for the status

        // first check that this isn't a nested coordinator running locally
        URL nestedLraId = extractParentLRA(endPath);

        if (nestedLraId != null && lraService != null) {
            Transaction transaction = lraService.getTransaction(nestedLraId);

            if (transaction != null) {
                CompensatorStatus cStatus = transaction.getLRAStatus();

                if (cStatus == null) {
                    LRALogger.logger.warnf("LRARecord.retryGetEndStatus: local LRA %s accepted but has a null status",
                            endPath);
                    return -1; // shouldn't happen since it imples it's still be active - force end to be called
                }

                switch (cStatus) {
                    case Completed:
                    case Compensated:
                        return TwoPhaseOutcome.FINISH_OK;
                    case Completing:
                    case Compensating:
                        return TwoPhaseOutcome.HEURISTIC_HAZARD;
                    case FailedToCompensate:
                    case FailedToComplete:
                        return TwoPhaseOutcome.FINISH_ERROR;
                }
            }
        } else if (statusURI != null) {
            // it is a standard participant - check the status URL
            Client client = ClientBuilder.newClient();

            try {
                WebTarget target = client.target(statusURI.toURI());

                Response response = target.request()
                        .header(LRA_HTTP_HEADER, lraId.toExternalForm())
                        .header(LRA_HTTP_RECOVERY_HEADER, recoveryURL.toExternalForm())
                        .get();

                if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                    // the participant never got the completion request so resend it
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                }

                if (response.getStatus() == Response.Status.OK.getStatusCode() &&
                        response.hasEntity()) {
                    // the participant is available again and has reported its status
                    String s = response.readEntity(String.class);

                    status = CompensatorStatus.valueOf(s);

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

                            if (forgetURI != null)
                                forgetURI = statusURI; // forget is equivalent to HTTP delete on the status URL

                            if (forgetURI != null) {
                                try {
                                    // let the participant know he can clean up
                                    WebTarget target2 = client.target(forgetURI.toURI());
                                    Response response2 = target2.request()
                                            .header(LRA_HTTP_HEADER, lraId.toExternalForm())
                                            .header(LRA_HTTP_RECOVERY_HEADER, recoveryURL.toExternalForm())
                                            .delete();

                                    if (response2.getStatus() == Response.Status.OK.getStatusCode())
                                        return TwoPhaseOutcome.FINISH_OK;
                                } catch (Exception e) {
                                    if (LRALogger.logger.isInfoEnabled()) {
                                        LRALogger.logger.infof("LRARecord.doEnd forget URI %s is invalid (%s)",
                                                forgetURI, e.getMessage());
                                    }

                                    // TODO write a test to ensure that recovery only retries the forget request
                                    return TwoPhaseOutcome.HEURISTIC_HAZARD; // force recovery to keep retrying
                                }

                            } else {
                                LRALogger.logger.warnf(
                                        "LRARecord.doEnd(%b) LRA: %s: cannot forget %s: missing forget URI",
                                        compensate, lraId, statusURI, status);
                            }

                            if (compensate)
                                return TwoPhaseOutcome.FINISH_OK;

                            return TwoPhaseOutcome.FINISH_OK;
                    }
                }
            } catch (Exception e) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("LRARecord.doEnd status URI %s is invalid (%s)",
                            statusURI, e.getMessage());
                }
            } finally {
                client.close();
            }
        }

        return -1;
    }

    // see if the participant is an LRA in the same VM as the coordinator
    private URL extractParentLRA(URL endPath) {
        if (lraService != null) {
            String[] segments = endPath.getPath().split("/");
            int pCnt = segments.length;

            if (pCnt > 1) {
                String cId;

                try {
                    cId = URLDecoder.decode(segments[pCnt - 2], "UTF-8");

                    return lraService.hasTransaction(cId) ? new URL(cId) : null;
                } catch (UnsupportedEncodingException | MalformedURLException ignore) {
                }
            }
        }

        return null;
    }

    private int tryLocalEndInvocation(URL endPath) {
        URL cId = extractParentLRA(endPath);

        if (cId != null) {
            String[] segments = endPath.getPath().split("/");
            int pCnt = segments.length;

            // this is a call from a parent LRA to end the nested LRA:
            boolean isCompensate = "compensate".equals(segments[pCnt - 1]);
            boolean isComplete = "complete".equals(segments[pCnt - 1]);
            int httpStatus;

            if (!isCompensate && !isComplete) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("LRARecord.doEnd invalid nested participant url %s" +
                                    "(should be compensate or complete)",
                            endPath.toExternalForm());
                }

                httpStatus = Response.Status.BAD_REQUEST.getStatusCode();
            } else {
                LRAStatus inVMStatus = lraService.endLRA(cId, isCompensate, true);

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

    private int xtryLocalEndInvocation(URL endPath) {
        if (lraService != null) {
            String[] segments = endPath.getPath().split("/");
            int pCnt = segments.length;

            if (pCnt > 1) {
                String cId;

                try {
                    cId = URLDecoder.decode(segments[pCnt - 2], "UTF-8");
                } catch (UnsupportedEncodingException ignore) {
                    return -1;
                }

                if (lraService.hasTransaction(cId)) {
                    try {
                        // this is a call from a parent LRA to end the nested LRA:
                        boolean isCompensate = "compensate".equals(segments[pCnt - 1]);
                        boolean isComplete = "complete".equals(segments[pCnt - 1]);
                        int httpStatus;

                        if (!isCompensate && !isComplete) {
                            if (LRALogger.logger.isInfoEnabled()) {
                                LRALogger.logger.infof("LRARecord.doEnd invalid nested participant url %s" +
                                                "(should be compensate or complete)",
                                        endPath.toExternalForm());
                            }

                            httpStatus = Response.Status.BAD_REQUEST.getStatusCode();
                        } else {
                            LRAStatus inVMStatus = lraService.endLRA(new URL(cId), isCompensate, true);

                            httpStatus = inVMStatus.getHttpStatus();

                            try {
                                responseData = inVMStatus.getEncodedResponseData();
                            } catch (IOException ignore) {
                            }
                        }

                        return httpStatus;
                    } catch (MalformedURLException e) {
                        // fall back to using JAX-RS
                    }
                }
            }
        }

        return -1;
    }

    boolean forget() {
        return true;
    }

    private boolean isCompelete() {
        return status != null && status == CompensatorStatus.Completed;
    }

    private boolean isCompensated() {
        return status != null && status == CompensatorStatus.Compensated;
    }

    String getResponseData() {
        return responseData;
    }

    @Override
    public boolean save_state(OutputObjectState os, int t) {
        if (super.save_state(os, t)) {
            try {
                packURL(os, lraId);
                packURL(os, compensateURI);
                packURL(os, recoveryURL);
                packURL(os, completeURI);
                packURL(os, statusURI);
                packURL(os, forgetURI);
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
                lraId = unpackURL(os);
                compensateURI = unpackURL(os);
                recoveryURL = unpackURL(os);
                completeURI = unpackURL(os);
                statusURI = unpackURL(os);
                forgetURI = unpackURL(os);
                unpackStatus(os);
                participantPath = os.unpackString();
                compensatorData = os.unpackString();
                accepted = status == CompensatorStatus.Completing || status == CompensatorStatus.Compensating;
            } catch (IOException e) {
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
        status = os.unpackBoolean() ? CompensatorStatus.values()[os.unpackInt()] : null;
    }

    private void packURL(OutputObjectState os, URL url) throws IOException {
        if (url == null) {
            os.packBoolean(false);
        } else {
            os.packBoolean(true);
            os.packString(url.toExternalForm());
        }
    }

    private URL unpackURL(InputObjectState os) throws IOException {
        return os.unpackBoolean() ? new URL(os.unpackString()) : null;
    }

    private static int getTypeId() {
        return RecordType.USER_DEF_FIRST0; // RecordType.LRA_RECORD; TODO we depend on swarm for narayana which is using an earlier version
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

        if (lessThan(other))
            return -1;

        if (greaterThan(other))
            return 1;

        return 0;
    }

    boolean setTimeLimit(ScheduledExecutorService scheduler, long timeLimit) {
        return scheduleCancelation(this::topLevelAbort, scheduler, timeLimit);
    }

    private boolean scheduleCancelation(Runnable runnable, ScheduledExecutorService scheduler, Long timeLimit) {
        if ((scheduledAbort != null && !scheduledAbort.cancel(false)))
            return false;

        if (timeLimit > 0) {
            cancelOn = LocalTime.now().plusNanos(timeLimit * 1000000);

            scheduledAbort = scheduler.schedule(runnable, timeLimit, TimeUnit.MILLISECONDS);
        } else {
            cancelOn = null;

            scheduledAbort = null;
        }

        return true;
    }

    public URL getRecoveryCoordinatorURL() {
        return recoveryURL;
    }

    public String getParticipantURL() {
        return participantPath;
    }

    void setRecoveryURL(String recoveryURL) {
        try {
            this.recoveryURL = new URL(recoveryURL);
        } catch (MalformedURLException e) {
            throw new InvalidLRAIdException(recoveryURL, "Invalid recovery id", e);
        }
    }

    void setRecoveryURL(String recoveryUrlBase, String txId, String coordinatorId) {
        setRecoveryURL(recoveryUrlBase + txId + '/' + coordinatorId);
    }

    public String getCompensator() {
        return compensateURI.toExternalForm();
    }

    void setLRAService(LRAService lraService) {
        this.lraService = lraService;
    }
}
