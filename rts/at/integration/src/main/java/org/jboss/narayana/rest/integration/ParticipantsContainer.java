package org.jboss.narayana.rest.integration;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class ParticipantsContainer {

    private static final ParticipantsContainer INSTANCE = new ParticipantsContainer();

    private final Map<String, ParticipantInformation> participantsInformation;

    public static ParticipantsContainer getInstance() {
        return INSTANCE;
    }

    private ParticipantsContainer() {
        participantsInformation = new ConcurrentHashMap<String, ParticipantInformation>();
    }

    public Map<String, ParticipantInformation> getAllParticipantsInformation() {
        return Collections.unmodifiableMap(participantsInformation);
    }

    public ParticipantInformation getParticipantInformation(final String participantId) {
        return participantsInformation.get(participantId);
    }

    public void addParticipantInformation(final String participantId, final ParticipantInformation participantInformation) {
        participantsInformation.put(participantId, participantInformation);
    }

    public void removeParticipantInformation(final String participantId) {
        participantsInformation.remove(participantId);
    }

    public void clear() {
        participantsInformation.clear();
    }

}
