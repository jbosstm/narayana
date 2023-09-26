/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.rest.integration;

import org.jboss.narayana.rest.integration.api.VolatileParticipant;

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

    private final Map<String, VolatileParticipant> volatileParticipants;

    public static ParticipantsContainer getInstance() {
        return INSTANCE;
    }

    private ParticipantsContainer() {
        participantsInformation = new ConcurrentHashMap<String, ParticipantInformation>();
        volatileParticipants = new ConcurrentHashMap<String, VolatileParticipant>();
    }

    public Map<String, ParticipantInformation> getAllParticipantsInformation() {
        return Collections.unmodifiableMap(participantsInformation);
    }

    public Map<String, VolatileParticipant> getAllVolatileParticipants() {
        return Collections.unmodifiableMap(volatileParticipants);
    }

    public ParticipantInformation getParticipantInformation(final String participantId) {
        return participantsInformation.get(participantId);
    }

    public VolatileParticipant getVolatileParticipant(final String participantId) {
        return volatileParticipants.get(participantId);
    }

    public void addParticipantInformation(final String participantId, final ParticipantInformation participantInformation) {
        participantsInformation.put(participantId, participantInformation);
    }

    public void addVolatileParticipant(final String participantId, final VolatileParticipant volatileParticipant) {
        volatileParticipants.put(participantId, volatileParticipant);
    }

    public void removeParticipantInformation(final String participantId) {
        participantsInformation.remove(participantId);
    }

    public void removeVolatileParticipant(final String participantId) {
        volatileParticipants.remove(participantId);
    }

    public void clear() {
        participantsInformation.clear();
        volatileParticipants.clear();
    }

}
