/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.rest.integration;

import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.logging.Logger;
import org.jboss.narayana.rest.integration.api.ParticipantDeserializer;
import org.jboss.narayana.rest.integration.api.HeuristicType;
import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.ParticipantException;
import org.jboss.narayana.rest.integration.api.ParticipantsManager;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.narayana.rest.integration.api.VolatileParticipant;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class ParticipantsManagerImpl implements ParticipantsManager {

    private static final Logger LOG = Logger.getLogger(ParticipantsManagerImpl.class);

    private String baseUrl;

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(final String baseUrl) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("ParticipantsManagerImpl.setBaseUrl: baseUrl=" + baseUrl);
        }

        this.baseUrl = baseUrl;
    }

    @Override
    public String enlist(final String applicationId, final String participantEnlistmentURL, final Participant participant) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("ParticipantsManagerImpl.enlist: applicationId=" + applicationId + ", participantEnlistmentURL="
                    + participantEnlistmentURL + ", participant=" + participant);
        }

        if (baseUrl == null) {
            throw new IllegalStateException("Base URL was not defined.");
        }

        final String participantId = new Uid().toString();
        final String participantUrl = getParticipantUrl(participantId, baseUrl);

        String participantRecoveryURL = enlistParticipant(participantUrl, participantEnlistmentURL);

        ParticipantInformation participantInformation = new ParticipantInformation(participantId, applicationId,
                participantRecoveryURL, participant);
        participantInformation.setStatus(TxStatus.TransactionActive.name());

        ParticipantsContainer.getInstance().addParticipantInformation(participantId, participantInformation);

        if (LOG.isTraceEnabled()) {
            LOG.trace("ParticipantsManagerImpl.enlist: participant enlisted. participantUrl=" + participantUrl
                    + ", participantInformation=" + participantInformation);
        }

        return participantId;
    }

    @Override
    public void enlistVolatileParticipant(final String volatileParticipantEnlistmentURL,
            final VolatileParticipant volatileParticipant) {

        if (LOG.isTraceEnabled()) {
            LOG.trace("ParticipantsManagerImpl.enlistVolatileParticipant: volatileParticipantEnlistmentURL="
                    + volatileParticipantEnlistmentURL + ", volatileParticipant=" + volatileParticipant);
        }

        if (baseUrl == null) {
            throw new IllegalStateException("Base URL was not defined.");
        }

        final String participantId = new Uid().toString();
        final String participantUrl = getVolatileParticipantUrl(participantId, baseUrl);

        enlistVolatileParticipant(participantUrl, volatileParticipantEnlistmentURL);

        ParticipantsContainer.getInstance().addVolatileParticipant(participantId, volatileParticipant);

        if (LOG.isTraceEnabled()) {
            LOG.trace("ParticipantsManagerImpl.enlistVolatileParticipant: participant enlisted. participantId="
                    + participantId + ", participantUrl=" + participantUrl);
        }
    }

    @Override
    public void registerDeserializer(final String applicationId, final ParticipantDeserializer deserializer) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("ParticipantsManagerImpl.registerDeserializer: applicationId=" + applicationId + ", deserializer="
                    + deserializer);
        }

        RecoveryManager.getInstance().registerDeserializer(applicationId, deserializer);
    }

    @Override
    public void reportHeuristic(String participantId, HeuristicType heuristicType)  {
        if (LOG.isTraceEnabled()) {
            LOG.trace("ParticipantsManagerImpl.reportHeuristic: participantId=" + participantId + ", heuristicType="
                    + heuristicType);
        }

        final ParticipantInformation participantInformation = ParticipantsContainer.getInstance()
                .getParticipantInformation(participantId);

        switch (heuristicType) {
            case HEURISTIC_ROLLBACK:
                participantInformation.setStatus(TxStatus.TransactionHeuristicRollback.name());
                break;

            case HEURISTIC_COMMIT:
                participantInformation.setStatus(TxStatus.TransactionHeuristicCommit.name());
                break;

            case HEURISTIC_HAZARD:
                participantInformation.setStatus(TxStatus.TransactionHeuristicHazard.name());
                break;

            case HEURISTIC_MIXED:
                participantInformation.setStatus(TxStatus.TransactionHeuristicMixed.name());
                break;

            default:
                throw new IllegalArgumentException("Unknown heuristic type");
        }

        RecoveryManager.getInstance().persistParticipantInformation(participantInformation);
    }

    private String enlistParticipant(final String participantUrl, final String participantEnlistmentURL) {
        TxSupport txSupport = new TxSupport();
        String participantLinkHeader = txSupport.makeTwoPhaseAwareParticipantLinkHeader(participantUrl, participantUrl);

        final String recoveryUrl;
        try {
            recoveryUrl = txSupport.enlistParticipant(participantEnlistmentURL, participantLinkHeader);
        } catch (HttpResponseException e) {
            throw new ParticipantException("Failed to enlist participant", e);
        }

        return recoveryUrl;
    }

    private void enlistVolatileParticipant(final String participantUrl, final String volatileParticipantEnlistmentURL) {
        final StringBuilder linkHeader = new StringBuilder();
        linkHeader.append("<").append(participantUrl).append(">; rel=\"")
                .append(TxLinkNames.VOLATILE_PARTICIPANT).append("\"");

        final String participantLinkHeader = linkHeader.toString();

        try {
            new TxSupport().enlistVolatileParticipant(volatileParticipantEnlistmentURL, participantLinkHeader);
        } catch (HttpResponseException e) {
            throw new ParticipantException("Failed to enlist volatile participant", e);
        }
    }

    private String getParticipantUrl(final String participantId, String baseUrl) {
        if (!baseUrl.substring(baseUrl.length() - 1).equals("/")) {
            baseUrl += "/";
        }

        return baseUrl + ParticipantResource.BASE_PATH_SEGMENT + "/" + participantId;
    }

    private String getVolatileParticipantUrl(final String participantId, String baseUrl) {
        if (!baseUrl.substring(baseUrl.length() - 1).equals("/")) {
            baseUrl += "/";
        }

        return baseUrl + VolatileParticipantResource.BASE_PATH_SEGMENT + "/" + participantId;
    }

}
