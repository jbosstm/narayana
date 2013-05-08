package org.jboss.narayana.rest.integration;

import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.rest.integration.api.HeuristicType;
import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.ParticipantsManager;

import com.arjuna.ats.arjuna.common.Uid;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class ParticipantsManagerImpl implements ParticipantsManager {

    public String baseUrl;

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Uid enlist(final TxSupport txSupport, final Participant participant) {
        return enlist(txSupport.getDurableParticipantEnlistmentURI(), participant);
    }

    @Override
    public Uid enlist(final String participantEnlistmentURL, final Participant participant) {
        if (baseUrl == null) {
            throw new IllegalStateException("Base URL was not defined.");
        }

        final Uid participantId = new Uid();
        final String participantUrl = getParticipantUrl(participantId, baseUrl);

        String participantRecoveryURL = enlistParticipant(participantUrl, participantEnlistmentURL);

        ParticipantInformation participantInformation = new ParticipantInformation(participantId, participantRecoveryURL,
                baseUrl, participant);
        participantInformation.setStatus(TxStatus.TransactionActive.name());

        ParticipantsContainer.getInstance().addParticipantInformation(participantId, participantInformation);

        return participantId;
    }

    @Override
    public void reportHeuristic(Uid participantId, HeuristicType heuristicType) {
        final ParticipantInformation participantInformation = ParticipantsContainer.getInstance().getParticipantInformation(
                participantId);

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
        }

    private String enlistParticipant(final String participantUrl, final String participantEnlistmentURL) {
        TxSupport txSupport = new TxSupport();
        String participantLinkHeader = txSupport.makeTwoPhaseAwareParticipantLinkHeader(participantUrl, participantUrl);
        String recoveryUrl = txSupport.enlistParticipant(participantEnlistmentURL, participantLinkHeader);

        return recoveryUrl;
    }

    private String getParticipantUrl(final Uid participantId, String baseUrl) {
        if (!baseUrl.substring(baseUrl.length() - 1).equals("/")) {
            baseUrl += "/";
        }

        return baseUrl + ParticipantResource.BASE_PATH_SEGMENT + "/" + participantId;
    }

}
