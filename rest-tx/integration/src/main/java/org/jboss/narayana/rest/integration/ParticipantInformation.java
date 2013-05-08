package org.jboss.narayana.rest.integration;

import org.jboss.narayana.rest.integration.api.Participant;

import com.arjuna.ats.arjuna.common.Uid;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class ParticipantInformation {

    private final Uid id;

    private final String recoveryURL;

    private final Participant participant;

    private final String baseUrl;

    private String status;

    public ParticipantInformation(final Uid id, final String recoveryURL, final String baseUrl, final Participant participant) {
        this(id, recoveryURL, baseUrl, participant, null);
    }

    public ParticipantInformation(final Uid id, final String recoveryURL, final String baseUrl, final Participant participant, final String status) {
        this.id = id;
        this.recoveryURL = recoveryURL;
        this.baseUrl = baseUrl;
        this.participant = participant;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public Uid getId() {
        return id;
    }

    public String getRecoveryURL() {
        return recoveryURL;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Participant getParticipant() {
        return participant;
    }

}
