/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.internal.context;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.internal.recovery.DeserializerHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * State of the specific compensation context.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CompensationContextState {

    private static final String RECORD_TYPE = "/Compensations/Context";

    private static final Logger LOGGER = Logger.getLogger(CompensationContextState.class);

    private final Map<String, Object> resources = new ConcurrentHashMap<>();

    private final Set<String> participants = ConcurrentHashMap.newKeySet();

    private final DeserializerHelper deserializerHelper;

    private Uid id;

    private String transactionId;

    /**
     * @param deserializerHelper Helper to deserialize resources.
     */
    public CompensationContextState(DeserializerHelper deserializerHelper) {
        Objects.requireNonNull(deserializerHelper, "Deserializer helper cannot be null");
        this.deserializerHelper = deserializerHelper;
    }

    /**
     * @param id Uid of the compensation context. It is used to persist this record to the object store.
     * @param transactionId String id of the transaction to which this context belongs.
     * @param deserializerHelper Helper to deserialize resources.
     */
    public CompensationContextState(Uid id, String transactionId, DeserializerHelper deserializerHelper) {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(transactionId, "Transaction id cannot be null");
        Objects.requireNonNull(deserializerHelper, "Deserializer helper cannot be null");
        this.id = id;
        this.transactionId = transactionId;
        this.deserializerHelper = deserializerHelper;
    }

    public static String getRecordType() {
        return RECORD_TYPE;
    }

    /**
     * @return Uid of the context.
     */
    public Uid getId() {
        return new Uid(id);
    }

    /**
     * @return String id of the transaction to which this context belongs.
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * @param id String id of the resource.
     * @return Object of the resource of null, if such resource wasn't found.
     */
    public Object getResource(String id) {
        Objects.requireNonNull(id, "Id cannot be null");
        return resources.get(id);
    }

     /**
      * Registers resource with this context.
      *
      * @param id String id of the resource.
      * @param resource Object of the resource.
      */
    public void addResource(String id, Object resource) {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(resource, "Resource cannot be null");
        resources.put(id, resource);
    }

    /**
     * Removes resource from this context.
     *
     * @param id String id of the resource.
     */
    public void removeResource(String id) {
        Objects.requireNonNull(id, "Id cannot be null");
        resources.remove(id);
    }

    /**
     * Attaches participant which depends on this context.
     *
     * @param id String participant id.
     */
    public void attachParticipant(String id) {
        Objects.requireNonNull(id, "Id cannot be null");
        participants.add(id);
    }

    /**
     * Detaches participant which was dependant on this context. Once the last participant is detached, context is removed.
     * 
     * @param id String participant id
     */
    public void detachParticipant(String id) {
        Objects.requireNonNull(id, "Id cannot be null");
        participants.remove(id);
    }

    /**
     * Checks if this state has any participants attached to it.
     * @return
     */
    public boolean hasAttachedParticipants() {
        return !participants.isEmpty();
    }

    /**
     * Persists context to the object store. If record already exists, it is overridden.
     *
     * @param state Output object state to persist data.
     * @return true on success and false on failure.
     */
    public boolean persist(OutputObjectState state) {
        Objects.requireNonNull(state, "State cannot be null");
        try {
            UidHelper.packInto(id, state);
            state.packString(transactionId);
            persistResources(state, resources);
            persistParticipants(state, participants);
        } catch (IOException e) {
            LOGGER.warnf(e, "Failed to persist context '%s' for transaction '%s'", id, transactionId);
            return false;
        }

        return true;
    }

    /**
     * Restores context from the object store.
     *
     * @param state Input object state to read data from.
     * @return true on success and false on failure.
     */
    public boolean restore(InputObjectState state) {
        Objects.requireNonNull(state, "State cannot be null");
        try {
            id = UidHelper.unpackFrom(state);
            transactionId = state.unpackString();
            resources.clear();
            resources.putAll(restoreResources(state));
            participants.clear();
            participants.addAll(restoreParticipants(state));
        } catch (IOException e) {
            LOGGER.warnf(e, "Failed to restore context '%s' for transaction '%s'", id, transactionId);
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompensationContextState state = (CompensationContextState) o;

        if (!resources.equals(state.resources)) {
            return false;
        }
        if (!participants.equals(state.participants)) {
            return false;
        }
        if (id != null ? !id.equals(state.id) : state.id != null) {
            return false;
        }

        return transactionId != null ? transactionId.equals(state.transactionId) : state.transactionId == null;
    }

    @Override
    public int hashCode() {
        int result = resources.hashCode();
        result = 31 * result + participants.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        return result;
    }

    private void persistResources(OutputObjectState state, Map<String, Object> resources) throws IOException {
        state.packInt(resources.size());
        for (Map.Entry<String, Object> entry : resources.entrySet()) {
            state.packString(entry.getKey());
            state.packString(entry.getValue().getClass().getName());
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectStream = new ObjectOutputStream(outputStream)) {
                objectStream.writeObject(entry.getValue());
                state.packBytes(outputStream.toByteArray());
            }
        }
    }

    private Map<String, Object> restoreResources(InputObjectState state) throws IOException {
        int count = state.unpackInt();
        Map<String, Object> resources = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            String key = state.unpackString();
            String className = state.unpackString();
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(state.unpackBytes());
                    ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
                Object resource = deserializerHelper.deserialize(objectStream, className, Object.class)
                        .orElseThrow(() -> new IOException("Failed to read a resource " + key + " of class " + className));
                resources.put(key, resource);
            }
        }
        return resources;
    }

    private void persistParticipants(OutputObjectState state, Set<String> participants) throws IOException {
        state.packInt(participants.size());
        for (String participantId : participants) {
            state.packString(participantId);
        }
    }

    private Set<String> restoreParticipants(InputObjectState state) throws IOException {
        int count = state.unpackInt();
        Set<String> participants = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            participants.add(state.unpackString());
        }
        return participants;
    }

}
