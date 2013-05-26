package org.jboss.narayana.rest.integration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.arjuna.ats.arjuna.common.Uid;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class ParticipantsContainer {

    private static final ParticipantsContainer INSTANCE = new ParticipantsContainer();

    private final Map<Uid, ParticipantInformation> participantsInformation;

    public static ParticipantsContainer getInstance() {
        return INSTANCE;
    }

    private ParticipantsContainer() {
        participantsInformation = new ConcurrentHashMap<Uid, ParticipantInformation>();
    }

    public ParticipantInformation getParticipantInformation(final Uid participantId) {
        return participantsInformation.get(participantId);
    }

    public void addParticipantInformation(final Uid participantId, final ParticipantInformation participantInformation) {
        participantsInformation.put(participantId, participantInformation);
    }

    public void removeParticipantInformation(final Uid participantId) {
        participantsInformation.remove(participantId);
    }

    public void clear() {
        participantsInformation.clear();
    }

}
