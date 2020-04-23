/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2020,
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreAPI;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.io.IOException;
import java.io.SyncFailedException;
import java.util.HashSet;
import java.util.Set;

/**
 * Adaptor class that wraps the SlotStore to make it look like an ObjectStore.
 * Modelled on HornetqObjectStoreAdaptor.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2020-03
 */
public class SlotStoreAdaptor implements ObjectStoreAPI {

    private final SlotStore store;

    // used for standalone bootstrap via StoreManager
    public SlotStoreAdaptor() throws IOException {
        SlotStoreEnvironmentBean envBean = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);
        store = new SlotStore(envBean);
    }

    // used for beans wiring type bootstrap when running embedded.
    public SlotStoreAdaptor(SlotStore slotStore) {
        this.store = slotStore;
    }

    /**
     * Obtain all of the Uids for a specified type.
     *
     * @param typeName       The type to scan for.
     * @param foundInstances The object state in which to store the Uids
     * @param matchState     The file type to look for (e.g., committed, shadowed). [StateStatus] Note: matchState=OS_UNKNOWN matches any state.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */
    @Override
    public boolean allObjUids(String typeName, InputObjectState foundInstances, int matchState) throws ObjectStoreException {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("SlotStore.allObjUids(" + typeName + ", " + matchState + ")");
        }

        SlotStoreKey key = new SlotStoreKey(Uid.nullUid(), typeName, matchState);

        SlotStoreKey[] matchingKeys = store.getMatchingKeys(key);

        OutputObjectState buffer = new OutputObjectState();

        try {
            for (SlotStoreKey matchingKey : matchingKeys) {
                UidHelper.packInto(matchingKey.getUid(), buffer);
            }
            UidHelper.packInto(Uid.nullUid(), buffer);
        } catch (IOException e) {
            throw new ObjectStoreException(e);
        }

        foundInstances.setBuffer(buffer.buffer());

        return true;
    }

    /**
     * Obtain all of the Uids for a specified type, regardless of their state.
     *
     * @param typeName       The type to scan for.
     * @param foundInstances The object state in which to store the Uids
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */
    @Override
    public boolean allObjUids(String typeName, InputObjectState foundInstances) throws ObjectStoreException {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("SlotStore.allObjUids(" + typeName + ")");
        }

        return allObjUids(typeName, foundInstances, StateStatus.OS_UNKNOWN);
    }

    /**
     * Obtain all types of objects stored in the object store.
     *
     * @param foundTypes The state in which to store the types.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */
    @Override
    public boolean allTypes(InputObjectState foundTypes) throws ObjectStoreException {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("SlotStore.allTypes()");
        }

        String[] knownTypes = store.getKnownTypes();
        Set<String> typeSet = new HashSet<>();

        if (knownTypes == null || knownTypes.length == 0) {
            return true;
        }

        OutputObjectState buffer = new OutputObjectState();

        try {
            for (String typeName : knownTypes) {
                if (typeName.startsWith("/")) {
                    typeName = typeName.substring(1);
                }

                if (typeName.contains("/")) {
                    String value = "";
                    String[] parents = typeName.split("/");
                    for (String parent : parents) {
                        if (parent.length() == 0) {
                            continue;
                        }
                        if (value.length() > 0) {
                            value = value + "/";
                        }
                        value = value + parent;
                        if (!typeSet.contains(value)) {
                            typeSet.add(value);
                            buffer.packString(value);
                        }
                    }
                } else {
                    buffer.packString(typeName);
                }
            }
            buffer.packString("");
        } catch (IOException e) {
            throw new ObjectStoreException(e);
        }

        foundTypes.setBuffer(buffer.buffer());

        return true;
    }

    /**
     * @param uid      The object to query.
     * @param typeName The type of the object to query.
     * @return the current state of the object's state (e.g., shadowed,
     * committed ...) [StateStatus]
     */
    @Override
    public int currentState(Uid uid, String typeName) throws ObjectStoreException {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("SlotStore.currentState(" + uid + ", " + typeName + ")");
        }

        SlotStoreKey key = new SlotStoreKey(uid, typeName, StateStatus.OS_COMMITTED);

        if (store.contains(key)) {
            return StateStatus.OS_COMMITTED;
        } else {
            return StateStatus.OS_UNKNOWN;
        }
    }

    /**
     * Hide the object's state in the object store. Used by crash
     * recovery.
     *
     * @param u  The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */
    @Override
    public boolean hide_state(Uid u, String tn) throws ObjectStoreException {
        throw new ObjectStoreException(tsLogger.i18NLogger.get_method_not_implemented());
    }

    /**
     * Reveal a hidden object's state.
     *
     * @param u  The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */
    @Override
    public boolean reveal_state(Uid u, String tn) throws ObjectStoreException {
        throw new ObjectStoreException(tsLogger.i18NLogger.get_method_not_implemented());
    }

    /**
     * Read the object's committed state.
     *
     * @param uid      The object to work on.
     * @param typeName The type of the object to work on.
     * @return the state of the object.
     */
    @Override
    public InputObjectState read_committed(Uid uid, String typeName) throws ObjectStoreException {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("SlotStore.read_committed(" + uid + ", " + typeName + ")");
        }

        SlotStoreKey key = new SlotStoreKey(uid, typeName, StateStatus.OS_COMMITTED);

        try {
            return store.read(key);
        } catch (IOException e) {
            throw new ObjectStoreException(e);
        }
    }

    /**
     * Is the current state of the object the same as that provided as the last
     * parameter?
     *
     * @param u  The object to work on.
     * @param tn The type of the object.
     * @param st The expected type of the object. [StateType]
     * @return <code>true</code> if the current state is as expected,
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isType(Uid u, String tn, int st) throws ObjectStoreException {
        throw new ObjectStoreException(tsLogger.i18NLogger.get_method_not_implemented());
    }

    /**
     * Remove the object's committed state.
     *
     * @param uid      The object to work on.
     * @param typeName The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */
    @Override
    public boolean remove_committed(Uid uid, String typeName) throws ObjectStoreException {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("SlotStore.remove_committed(" + uid + ", " + typeName + ")");
        }

        SlotStoreKey key = new SlotStoreKey(uid, typeName, StateStatus.OS_COMMITTED);

        try {
            return store.remove(key);
        } catch (IOException e) {
            throw new ObjectStoreException(e);
        }
    }

    /**
     * Write a new copy of the object's committed state.
     *
     * @param uid      The object to work on.
     * @param typeName The type of the object to work on.
     * @param buff     The state to write.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */
    @Override
    public boolean write_committed(Uid uid, String typeName, OutputObjectState buff) throws ObjectStoreException {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("SlotStore.write_committed(" + uid + ", " + typeName + ")");
        }

        SlotStoreKey key = new SlotStoreKey(uid, typeName, StateStatus.OS_COMMITTED);

        try {
            return store.write(key, buff);
        } catch (IOException e) {
            throw new ObjectStoreException(e);
        }
    }

    @Override
    public void sync() throws SyncFailedException, ObjectStoreException {
        // no-op
    }

    /////////////

    @Override
    public boolean commit_state(Uid u, String tn) throws ObjectStoreException {
        throw new ObjectStoreException(tsLogger.i18NLogger.get_method_not_implemented());
    }

    @Override
    public InputObjectState read_uncommitted(Uid u, String tn) throws ObjectStoreException {
        throw new ObjectStoreException(tsLogger.i18NLogger.get_method_not_implemented());
    }

    @Override
    public boolean remove_uncommitted(Uid u, String tn) throws ObjectStoreException {
        throw new ObjectStoreException(tsLogger.i18NLogger.get_method_not_implemented());
    }

    @Override
    public boolean write_uncommitted(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException {
        throw new ObjectStoreException(tsLogger.i18NLogger.get_method_not_implemented());
    }

    /////////////

    @Override
    public boolean fullCommitNeeded() {
        return false;
    }

    @Override
    public String getStoreName() {
        return store.getStoreName();
    }

    @Override
    public void start() {
        // no-op
    }

    @Override
    public void stop() {
        // no-op
    }
}
