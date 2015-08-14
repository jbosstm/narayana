package org.jboss.narayana.rest.integration;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.logging.Logger;
import org.jboss.narayana.rest.integration.api.HeuristicException;
import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.ParticipantDeserializer;
import org.jboss.narayana.rest.integration.api.ParticipantException;
import org.jboss.narayana.rest.integration.api.ParticipantsManagerFactory;
import org.jboss.narayana.rest.integration.api.PersistableParticipant;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.Link;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class RecoveryManager {

    private static final String PARTICIPANT_INFORMATION_RECORD_TYPE = "/REST-AT/Integration/ParticipantInformationRecoveryRecord";

    private static final Logger LOG = Logger.getLogger(RecoveryManager.class);

    private static final RecoveryManager INSTANCE = new RecoveryManager();

    private final Map<String, ParticipantDeserializer> deserializers = new ConcurrentHashMap<String, ParticipantDeserializer>();

    private RecoveryManager() {
    }

    public static RecoveryManager getInstance() {
        return INSTANCE;
    }

    public void registerDeserializer(final String applicationId, final ParticipantDeserializer deserializer) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("RecoveryManager.registerDeserializer: applicationId=" + applicationId + ", deserializer="
                    + deserializer);
        }

        if (!deserializers.containsKey(applicationId)) {
            deserializers.put(applicationId, deserializer);
            recoverParticipants();
        }
    }

    public void persistParticipantInformation(final ParticipantInformation participantInformation) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("RecoveryManager.persistParticipantInformation: participantInformation=" + participantInformation);
        }

        if (!isRecoverableParticipant(participantInformation.getParticipant())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("RecoveryManager.persistParticipantInformation: participant is not recoverable");
            }

            return;
        }

        try {
            final RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
            final OutputObjectState state = getParticipantInformationOutputState(participantInformation);
            final Uid uid = new Uid(participantInformation.getId());

            recoveryStore.write_committed(uid, PARTICIPANT_INFORMATION_RECORD_TYPE, state);
        } catch (Exception e) {
            LOG.warn("Failure while persisting participant information.", e);
        }
    }

    public void removeParticipantInformation(final ParticipantInformation participantInformation) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("RecoveryManager.removeParticipantInformation: participantInformation=" + participantInformation);
        }

        final RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        final Uid uid = new Uid(participantInformation.getId());

        try {
            recoveryStore.remove_committed(uid, PARTICIPANT_INFORMATION_RECORD_TYPE);
        } catch (ObjectStoreException e) {
            LOG.warn("Failure while removing participant information from the object store.", e);
        }
    }

    private OutputObjectState getParticipantInformationOutputState(final ParticipantInformation participantInformation)
            throws IOException {

        final Uid uid = new Uid(participantInformation.getId());
        final OutputObjectState state = new OutputObjectState(uid, PARTICIPANT_INFORMATION_RECORD_TYPE);

        state.packString(participantInformation.getId());
        state.packString(participantInformation.getApplicationId());
        state.packString(participantInformation.getStatus());
        state.packString(participantInformation.getRecoveryURL());
        state.packBytes(getParticipantBytes(participantInformation.getParticipant()));

        return state;
    }

    private byte[] getParticipantBytes(final Participant participant) throws IOException {
        if (participant instanceof Serializable) {
            return serializeParticipant((Serializable) participant);
        } else if (participant instanceof PersistableParticipant) {
            return ((PersistableParticipant) participant).getRecoveryState();
        }

        // Shouldn't happen
        return new byte[] {};
    }

    private byte[] serializeParticipant(final Serializable participant) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(participant);

        return byteArrayOutputStream.toByteArray();
    }

    private boolean isRecoverableParticipant(final Participant participant) {
        return (participant instanceof Serializable) || (participant instanceof PersistableParticipant);
    }

    private void recoverParticipants() {
        if (ParticipantsManagerFactory.getInstance().getBaseUrl() != null) {
            final RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
            final InputObjectState states = new InputObjectState();

            try {
                if (recoveryStore.allObjUids(PARTICIPANT_INFORMATION_RECORD_TYPE, states)) {
                    Uid uid;

                    while ((uid = UidHelper.unpackFrom(states)).notEquals(Uid.nullUid())) {
                        final ParticipantInformation participantInformation = recreateParticipantInformation(
                                recoveryStore, uid);

                        if (participantInformation != null) {
                            ParticipantsContainer.getInstance().addParticipantInformation(
                                    participantInformation.getId(), participantInformation);
                        }
                    }
                }
            } catch (ObjectStoreException e) {
                LOG.warn(e.getMessage(), e);
            } catch (IOException e) {
                LOG.warn(e.getMessage(), e);
            }
        } else {
            LOG.warn("Participants cannot be loaded from the object store, because base URL was not set.");
        }
    }

    private ParticipantInformation recreateParticipantInformation(final RecoveryStore recoveryStore, final Uid uid)
            throws ObjectStoreException, IOException {

        final InputObjectState inputObjectState = recoveryStore.read_committed(uid, PARTICIPANT_INFORMATION_RECORD_TYPE);
        final String id = inputObjectState.unpackString();

        if (ParticipantsContainer.getInstance().getParticipantInformation(id) != null) {
            // Participant is already loaded.
            return null;
        }

        final String applicationId = inputObjectState.unpackString();

        if (!deserializers.containsKey(applicationId)) {
            // There is no appropriate deserializer.
            return null;
        }

        final String status = inputObjectState.unpackString();
        final String recoveryUrl = inputObjectState.unpackString();
        final Participant participant = recreateParticipant(inputObjectState, applicationId);

        if (participant == null) {
            // Deserializer failed to recreate participant.
            return null;
        }

        final ParticipantInformation participantInformation = new ParticipantInformation(id, applicationId, recoveryUrl,
                participant, status);

        if (!synchronizeParticipantUrlWithCoordinator(participantInformation)) {
            try {
                participant.rollback();
                removeParticipantInformation(participantInformation);
                // TODO is it OK to leave participant not rolled back in case of Exception?
            } catch (HeuristicException e) {
                LOG.warn(e.getMessage(), e);
            } catch (ParticipantException e) {
                LOG.warn(e.getMessage(), e);
            }
            return null;
        }

        return participantInformation;
    }

    private Participant recreateParticipant(final InputObjectState inputObjectState, final String applicationId)
            throws IOException {

        final ParticipantDeserializer deserializer = deserializers.get(applicationId);
        final byte[] participantBytes = inputObjectState.unpackBytes();

        Participant participant = deserializer.recreate(participantBytes);

        if (participant == null) {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(participantBytes);
            final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            participant = deserializer.deserialize(objectInputStream);
        }

        return participant;
    }

    private boolean synchronizeParticipantUrlWithCoordinator(final ParticipantInformation participantInformation) {
        final String participantUrl = getParticipantUrl(participantInformation.getId());
        final Link participantLink = new Link(TxLinkNames.PARTICIPANT_RESOURCE, TxLinkNames.PARTICIPANT_RESOURCE,
                participantUrl, null, null);
        final Link terminatorLink = new Link(TxLinkNames.PARTICIPANT_TERMINATOR, TxLinkNames.PARTICIPANT_TERMINATOR,
                participantUrl, null, null);

        try {
            final ClientResponse response = new ClientRequest(participantInformation.getRecoveryURL())
                    .addLink(participantLink).addLink(terminatorLink).put();
            if (response.getStatus() == 404) {
                return false;
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return false;
        }

        return true;
    }

    private String getParticipantUrl(final String participantId) {
        String baseUrl = ParticipantsManagerFactory.getInstance().getBaseUrl();

        if (!baseUrl.substring(baseUrl.length() - 1).equals("/")) {
            baseUrl += "/";
        }

        return baseUrl + ParticipantResource.BASE_PATH_SEGMENT + "/" + participantId;
    }

}
