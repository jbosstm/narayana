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

package org.jboss.narayana.compensations.internal.utils;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An utility class to help when working with recovery store.
 *
 * New instance should be created for each required recovery store and record type.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RecoveryHelper {

    private final RecoveryStore recoveryStore;

    private final String recordType;

    /**
     * @param recoveryStore An instance of the recovery store to work with.
     * @param recordType A record type to work with.
     */
    public RecoveryHelper(RecoveryStore recoveryStore, String recordType) {
        this.recoveryStore = recoveryStore;
        this.recordType = recordType;
    }

    /**
     * Get all {@code recordType} states persisted to the {@code recoveryStore}.
     *
     * @param exceptionConsumer exception consumer to be called in case of the failure.
     *
     * @return {@link InputObjectState} with all Uids, or an empty {@link InputObjectState} if failure has occurred.
     */
    public InputObjectState getStates(Consumer<Throwable> exceptionConsumer) {
        InputObjectState states = new InputObjectState();
        try {
            recoveryStore.allObjUids(recordType, states);
        } catch (ObjectStoreException e) {
            exceptionConsumer.accept(e);
            return new InputObjectState();
        }
        return states;
    }

    /**
     * Get all {@code recordType} states persisted to the {@code recoveryStore}.
     *
     * @param exceptionFunction function to create an expected exception in case of a failure.
     * @return {@link InputObjectState} with all Uids, or an empty {@link InputObjectState} if failure has occurred.
     * @throws X if failure has occurred when getting states.
     */
    public <X extends Throwable> InputObjectState getStatesWithException(Function<Throwable, X> exceptionFunction) throws X {
        InputObjectState states = new InputObjectState();
        try {
            recoveryStore.allObjUids(recordType, states);
        } catch (ObjectStoreException e) {
            throw exceptionFunction.apply(e);
        }
        return states;
    }

    /**
     * Get all {@code recordType} Uids persisted to the {@code recoveryStore}.
     *
     * @param exceptionConsumer exception consumer to be called in case of the failure.
     * @return a set of Uids.
     */
    public Set<Uid> getAllUids(Consumer<Throwable> exceptionConsumer) {
        InputObjectState states = getStates(exceptionConsumer);
        Set<Uid> uids = new HashSet<>();
        try {
            for (Uid uid = UidHelper.unpackFrom(states); uid.notEquals(Uid.nullUid()); uid = UidHelper.unpackFrom(states)) {
                uids.add(uid);
            }
        } catch (IOException ignored) {
            // Thrown once all uids are read
        }
        return uids;
    }

    /**
     * Get all {@code recordType} Uids persisted to the {@code recoveryStore}.
     *
     * @param exceptionFunction function to create an expected exception in case of a failure.
     * @return a set of Uids.
     * @throws X if failure has occurred when getting Uids.
     */
    public <X extends Throwable> Set<Uid> getAllUidsWithException(Function<Throwable, X> exceptionFunction) throws X {
        InputObjectState states = getStatesWithException(exceptionFunction);
        Set<Uid> uids = new HashSet<>();
        try {
            for (Uid uid = UidHelper.unpackFrom(states); uid.notEquals(Uid.nullUid()); uid = UidHelper.unpackFrom(states)) {
                uids.add(uid);
            }
        } catch (IOException ignored) {
            // Thrown once all uids are read
        }
        return uids;
    }

    /**
     * Get all {@code recordType} records persisted to the {@code recoveryStore}.
     *
     * @param exceptionConsumer exception consumer to be called in case of the failure.
     * @return a set of all records.
     */
    public Set<InputObjectState> getAllRecords(Consumer<Throwable> exceptionConsumer) {
        InputObjectState states = getStates(exceptionConsumer);
        Set<InputObjectState> records = new HashSet<>();
        try {
            for (Uid uid = UidHelper.unpackFrom(states); uid.notEquals(Uid.nullUid()); uid = UidHelper.unpackFrom(states)) {
                records.add(recoveryStore.read_committed(uid, recordType));
            }
        } catch (IOException ignored) {
            // Thrown once all uids are read
        } catch (ObjectStoreException e) {
            exceptionConsumer.accept(e);
            return Collections.emptySet();
        }
        return records;
    }

    /**
     * Get a specific record of {@code recordType} persisted to the {@code recoveryStore};
     *
     * @param id Uid of the record.
     * @param exceptionConsumer exception consumer to be called in case of the failure.
     * @return {@link Optional} with the requested record if it was read, or empty if failure occurred.
     */
    public Optional<InputObjectState> getRecord(Uid id, Consumer<Throwable> exceptionConsumer) {
        try {
            return Optional.of(recoveryStore.read_committed(id, recordType));
        } catch (ObjectStoreException e) {
            exceptionConsumer.accept(e);
            return Optional.empty();
        }
    }

    /**
     * Write record of {@code recordType} to the {@code recoveryStore}.
     *
     * @param state record state to be persisted.
     * @param exceptionConsumer exception consumer to be called in case of the failure.
     * @return {@code true} if record was written successfully, or {@code false} if failure occurred.
     */
    public boolean writeRecord(OutputObjectState state, Consumer<Throwable> exceptionConsumer) {
        try {
            return recoveryStore.write_committed(state.stateUid(), recordType, state);
        } catch (ObjectStoreException e) {
            exceptionConsumer.accept(e);
            return false;
        }
    }

    /**
     * Remove record of {@code recordType} from the {@code recoveryStore}.
     *
     * @param id record to be removed.
     * @param exceptionConsumer exception consumer to be called in case of the failure.
     * @return {@code true} if record was removed successfully, or {@code false} if failure occurred.
     */
    public boolean removeRecord(Uid id, Consumer<Throwable> exceptionConsumer) {
        try {
            return recoveryStore.remove_committed(id, recordType);
        } catch (ObjectStoreException e) {
            exceptionConsumer.accept(e);
            return false;
        }
    }

    /**
     * Remove record of {@code recordType} from the {@code recoveryStore}.
     *
     * @param id record to be removed.
     * @param exceptionFunction function to create an expected exception in case of a failure.
     * @return {@code true} if record was removed successfully, or {@code false} if failure occurred.
     * @throws X if failure has occurred when removing a record.
     */
    public <X extends Throwable> boolean removeRecordWithException(Uid id, Function<Throwable, X> exceptionFunction) throws X {
        try {
            return recoveryStore.remove_committed(id, recordType);
        } catch (ObjectStoreException e) {
            throw exceptionFunction.apply(e);
        }
    }

}
