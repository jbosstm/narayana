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
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RecoveryHelper {

    private final RecoveryStore recoveryStore;

    private final String recordType;

    public RecoveryHelper(RecoveryStore recoveryStore, String recordType) {
        this.recoveryStore = recoveryStore;
        this.recordType = recordType;
    }

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

    public <X extends Throwable> InputObjectState getStatesWithException(Function<Throwable, X> exceptionFunction) throws X {
        InputObjectState states = new InputObjectState();
        try {
            recoveryStore.allObjUids(recordType, states);
        } catch (ObjectStoreException e) {
            throw exceptionFunction.apply(e);
        }
        return states;
    }

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

    public Optional<InputObjectState> getRecord(Uid id, Consumer<Throwable> exceptionConsumer) {
        try {
            return Optional.of(recoveryStore.read_committed(id, recordType));
        } catch (ObjectStoreException e) {
            exceptionConsumer.accept(e);
            return Optional.empty();
        }
    }

    public boolean writeRecord(OutputObjectState state, Consumer<Throwable> exceptionConsumer) {
        try {
            return recoveryStore.write_committed(state.stateUid(), recordType, state);
        } catch (ObjectStoreException e) {
            exceptionConsumer.accept(e);
            return false;
        }
    }

    public boolean removeRecord(Uid id, Consumer<Throwable> exceptionConsumer) {
        try {
            return recoveryStore.remove_committed(id, recordType);
        } catch (ObjectStoreException e) {
            exceptionConsumer.accept(e);
            return false;
        }
    }

    public <X extends Throwable> boolean removeRecordWithException(Uid id, Function<Throwable, X> exceptionFunction) throws X {
        try {
            return recoveryStore.remove_committed(id, recordType);
        } catch (ObjectStoreException e) {
            throw exceptionFunction.apply(e);
        }
    }

}
