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
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import io.narayana.lra.client.Current;
import io.narayana.lra.client.InvalidLRAId;
import io.narayana.lra.coordinator.domain.service.LRAService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
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
    private URL lraId;
    private URL recoveryURL;
    private String participantPath;

    private URL completeURI;
    private URL compensateURI;
//    private URL statusURI;
//    private URL forgetURI;

    private boolean isCompelete;
    private boolean isCompensated;
    private boolean isFailed;

    private String responseData;
    private LocalTime cancelOn; // TODO make sure this acted upon during restore_state()
    private String compensatorData;
    private ScheduledFuture<?> scheduledAbort;
    private LRAService lraService;

    public LRARecord() {
    }

    LRARecord(LRAService lraService, String lraId, String linkURI, String compensatorData) {
        super(new Uid());

        try {
            // if compensateURI is a link parse it into compensate,complete and status urls
            if (linkURI.startsWith("<")) {
                linkURI = cannonicalForm(linkURI);
                Exception parseException[] = {null};

                Arrays.stream(linkURI.split(",")).forEach((linkStr) -> {
                    Exception e = parseLink(linkStr);
                    if (e != null)
                        parseException[0] = e;
                });

                if (parseException[0] != null)
                    throw new InvalidLRAId(lraId, "Invalid link URL", parseException[0]);
            } else {
                this.compensateURI = new URL(String.format("%s/compensate", linkURI));
                this.completeURI = new URL(String.format("%s/complete", linkURI));
//                this.statusURI = new URL(String.format("%s/status", linkURI));
//                this.forgetURI = new URL(String.format("%s/forget", linkURI));

            }

            this.lraId = new URL(lraId);
            this.lraService = lraService;
            this.participantPath = linkURI;

            this.recoveryURL = null;
            this.compensatorData = compensatorData;
        } catch (MalformedURLException e) {
            throw new InvalidLRAId(lraId, "Invalid LRA id", e);
        }
    }

    String getParticipantPath() {
        return participantPath;
    }

    static String cannonicalForm(String linkStr) {
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

    public static String extractCompensator(String linkStr) {
        for (String lnk : linkStr.split(",")) {
            Link link;

            try {
                link = Link.valueOf(lnk);
            } catch (Exception e) {
                e.printStackTrace();
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
            if ("compensate".equals(rel))
                compensateURI = new URL(uri);
            else if ("complete".equals(rel))
                completeURI = new URL(uri);
//            else if ("status".equals(rel))
//                statusURI = new URL(uri);
//            else if ("forget".equals(rel))
//                forgetURI = new URL(uri);

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
    // (? should complete actions be mapped onto abort since complete is best effort - ie it is compensate that recovery
    // needs to retry). If a compensator needs to know
    // if the lra completed then it can ask the io.narayana.lra.coordinator. A 404 status implies:
    // - all compensators completed ok, or
    // - all compensators compensated ok
    // This compensator can infer which possibility happened since it will have been told to complete or compensate
    public int topLevelAbort() {
        // TODO if this wa due to a timeout then we need to return doEnd(true);

        // put to compensateURI
        return doEnd(true);
    }

    @Override
    public int topLevelOnePhaseCommit() {

        return topLevelCommit();
    }

    @Override
    // ? what about mapping compensate actions commit (since we need recovery to kick in if any compensation fails)
    public int topLevelCommit() {
        return doEnd(false);
    }

    private int doEnd(boolean compensate) {
        // put to completeURI
        URL endPath;
        Client client = null;

        if (scheduledAbort != null) {
            // NB this could have been called from the scheduler so don't cancel our self!
            scheduledAbort.cancel(false);
            scheduledAbort = null;
        }

        if (compensate) {
            if (isCompensated())
                return TwoPhaseOutcome.FINISH_OK;
            // run the compensator
            endPath = compensateURI;
        } else {
            if (isCompelete() || completeURI == null) {
                isCompelete = true;
                isCompensated = false;
                return TwoPhaseOutcome.FINISH_OK;
            }
            // run complete
            endPath = completeURI;
        }

        // NB trying to compensate when already completed is allowed

        Current.push(lraId);

        Response response = null;
        LRAStatus inVMStatus = null;

        if (lraService != null) {
            String[] segments = endPath.getPath().split("/");
            int pCnt = segments.length;

            if (pCnt > 1) {
                String cId = null;

                try {
                    cId = URLDecoder.decode(segments[pCnt - 2], "UTF-8");
                } catch (UnsupportedEncodingException ignore) {
                }

                if (cId != null && lraService.hasTransaction(cId)) {
                    try {
                        // this is a call from a parent LRA to end the nested LRA:
                        boolean isCompensate = "compensate".equals(segments[pCnt - 1]);
                        boolean isComplete = "complete".equals(segments[pCnt - 1]);

                        if (!isCompensate && !isComplete) {
                            if (tsLogger.logger.isInfoEnabled()) {
                                tsLogger.logger.infof("LRARecord.doEnd invalid nested compensator url %s" +
                                                "(should be compensate or complete)",
                                        endPath.toExternalForm());
                            }

                            response = Response.status(Response.Status.BAD_REQUEST).build();
                        } else {
                            inVMStatus = lraService.endLRA(new URL(cId), isCompensate, true);

                            response = Response.ok().status(inVMStatus.getHttpStatus()).build();

                            try {
                                responseData = inVMStatus.getEncodedResponseData();
                            } catch (IOException ignore) {
                            }
                        }
                    } catch (MalformedURLException e) {
                        // fall back to using JAX-RS
                    }
                }
            }
        }

        if (response == null) {
            client = ClientBuilder.newClient();
            WebTarget target = client.target(URI.create(endPath.toExternalForm()));

            try {
                response = target.request()
                        .header(LRA_HTTP_HEADER, lraId.toExternalForm())
                        .header(LRA_HTTP_RECOVERY_HEADER, recoveryURL.toExternalForm())
                        .post(Entity.entity(compensatorData, MediaType.APPLICATION_JSON));

                if (response.hasEntity())
                    responseData = response.readEntity(String.class);
            } catch (Exception e) {
                response = null;
                if (tsLogger.logger.isInfoEnabled()) {
                    tsLogger.logger.infof("LRARecord.doEnd post %s failed: %s",
                            target.getUri(), e.getMessage());
                }
            } finally {
                client.close();
            }
        }

        if (response == null || response.getStatus() != Response.Status.OK.getStatusCode()) {
            isFailed = true;

            if (response != null && tsLogger.logger.isDebugEnabled()) {
                tsLogger.logger.debugf("LRARecord.doEnd post %s failed with status: %d",
                        endPath, response.getStatus());
            }

            if (compensate) {
                // the LRA record will be deleted so remember that this compensator failed to compensate
                return TwoPhaseOutcome.HEURISTIC_HAZARD;
            }

            return TwoPhaseOutcome.FINISH_ERROR;
        }

        if (compensate) {
            isCompensated = true;
            isCompelete = false;
        } else {
            isCompelete = true;
            isCompensated = false;
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    public boolean forget() {
        return true;
    }

    public boolean isCompelete() {
        return isCompelete;
    }

    public boolean isCompensated() {
        return isCompensated;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public String getResponseData() {
        return responseData;
    }

    @Override
    public boolean save_state(OutputObjectState os, int t) {
        if (super.save_state(os, t)) {
            try {
                os.packString(lraId.toExternalForm());
                os.packString(recoveryURL.toExternalForm());
                os.packString(participantPath);
                if (completeURI == null) {
                    os.packBoolean(false);
                } else {
                    os.packBoolean(true);
                    os.packString(completeURI.toExternalForm());
                }
                os.packString(compensateURI.toExternalForm());
//                os.packString(statusURI.toString());
                os.packString(compensatorData);

                os.packBoolean(isCompelete);
                os.packBoolean(isCompensated);
                os.packBoolean(isFailed);
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
                lraId = new URL(os.unpackString());
                recoveryURL = new URL(os.unpackString());
                participantPath = os.unpackString();
                completeURI = os.unpackBoolean() ? new URL(os.unpackString()) : null;
                compensateURI = new URL(os.unpackString());
//                statusURI = new URL(os.unpackString());
                compensatorData = os.unpackString();

                isCompelete = os.unpackBoolean();
                isCompensated = os.unpackBoolean();
                isFailed = os.unpackBoolean();
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    static int getTypeId() {
        return RecordType.USER_DEF_FIRST0; // RecordType.LRA_RECORD; TODO we dependend on swarm for narayana which is using an earlier version
    }

    public int typeIs() {
        return getTypeId();
    }

    public int nestedAbort()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedPrepare()
    {
        return TwoPhaseOutcome.PREPARE_OK; // do nothing
    }

    public int nestedOnePhaseCommit()
    {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public String type()
    {
        return LRARecord.typeName();
    }

    public static String typeName()
    {
        return "/StateManager/AbstractRecord/LRARecord";
    }

    public boolean doSave()
    {
        return true;
    }

    public void merge(AbstractRecord a) {
    }

    public void alter(AbstractRecord a) {
    }

    public boolean shouldAdd(AbstractRecord a)
    {
        return (a.typeIs() == typeIs());
    }

    public boolean shouldAlter(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldMerge(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldReplace(AbstractRecord a)
    {
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

    public void setTimeLimit(ScheduledExecutorService scheduler, long timeLimit) {
        scheduleCancelation(this::topLevelAbort, scheduler, timeLimit);
    }

    private int scheduleCancelation(Runnable runnable, ScheduledExecutorService scheduler, Long timeLimit) {
        if ((scheduledAbort != null && !scheduledAbort.cancel(false)))
            return Response.Status.PRECONDITION_FAILED.getStatusCode();

        if (timeLimit > 0) {
            cancelOn = LocalTime.now().plusNanos(timeLimit * 1000000);

            scheduledAbort = scheduler.schedule(runnable, timeLimit, TimeUnit.MILLISECONDS);
        } else {
            cancelOn = null;

            scheduledAbort = null;
        }

        return Response.Status.OK.getStatusCode();
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
            throw new InvalidLRAId(recoveryURL, "Invalid recovery id", e);
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
