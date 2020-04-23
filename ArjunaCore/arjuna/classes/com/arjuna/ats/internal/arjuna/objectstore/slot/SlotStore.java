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

import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputBuffer;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A storage system presenting a key-value API, implemented (conceptually) using a fixed sized array.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2020-03
 */
public class SlotStore {

    private final SlotStoreEnvironmentBean config;

    private final String storeDirCanonicalPath;

    /*
     * The slotIdIndex tracks the key to slot mapping for all in-use slots,
     * whilst the free list tracks all unoccupied slots. Care must be taken with the
     * relative ordering of operations on these structures, to avoid losing or double allocating slots.
     */
    public final ConcurrentHashMap<SlotStoreKey, Integer> slotIdIndex = new ConcurrentHashMap<>();
    public final Deque<Integer> freeList = new ConcurrentLinkedDeque<>();

    public final BackingSlots slots;

    /**
     * Create a new instance with the given configuration.
     *
     * @param config The configuration parameters for the instance
     * @throws IOException if the required backing storage can't be initialized.
     */
    public SlotStore(SlotStoreEnvironmentBean config) throws IOException {

        this.config = config;

        // unused for now, but eventually we'll have a disk backend...
        File storeDir = new File(config.getStoreDir());
        storeDirCanonicalPath = storeDir.getCanonicalPath();

        slots = config.getBackingSlots();
        slots.init(config);

        // internal recovery to rebuild the slotIdIndex and freeList
        for (int i = 0; i < config.getNumberOfSlots(); i++) {
            byte[] data = slots.read(i);
            if (data == null || data.length == 0) {
                freeList.add(i); // slot does not contain a valid entry, is free for use
            } else {
                InputBuffer inputBuffer = new InputBuffer(data);
                SlotStoreKey slotStoreKey = SlotStoreKey.unpackFrom(inputBuffer);
                slotIdIndex.put(slotStoreKey, i);
            }
        }
    }

    /**
     * @return the "name" of the object store. Where in the hierarchy it appears, e.g., /ObjectStore/MyName/...
     */
    public String getStoreName() {
        return this.getClass().getSimpleName() + ":" + storeDirCanonicalPath;
    }

    /**
     * Retrieve the serialized state for an entry.
     *
     * @param key The unique identifier for the entry
     * @return The serialized state
     * @throws IOException if the entry is not found
     */
    public InputObjectState read(SlotStoreKey key) throws IOException {

        Integer slotId = slotIdIndex.get(key);
        if (slotId == null) {
            throw new IOException("record not found for " + key);
        }
        byte[] data = slots.read(slotId);
        // it's possible, though unlikely, for the data to be null here due to a concurrent remove
        if (data == null) {
            throw new IOException("record not found for " + key);
        }
        InputBuffer inputBuffer = new InputBuffer(data);
        SlotStoreKey.unpackFrom(inputBuffer);

        InputObjectState inputObjectState = new InputObjectState();
        inputObjectState.unpackFrom(inputBuffer);

        return inputObjectState;
    }

    /**
     * Remove the state for an entry, freeing the slot.
     * Depending on the configuration, this change may not be immediately persistent.
     *
     * @param key The unique identifier for the entry
     * @return true on success, false otherwise
     * @throws IOException unused for now in this impl.
     */
    public boolean remove(SlotStoreKey key) throws IOException {

        Integer slotId = slotIdIndex.remove(key);
        if (slotId == null) {
            return false;
        }

        slots.clear(slotId, config.isSyncDeletes());

        freeList.add(slotId);

        return true;
    }

    /**
     * Write (or overwrite) an entry with the given key and value.
     *
     * @param key               The unique identifier for the entry
     * @param outputObjectState The serialized state
     * @return true on success, false otherwise e.g. when the store is full.
     * @throws IOException if serialization fails
     */
    public boolean write(SlotStoreKey key, OutputObjectState outputObjectState) throws IOException {

        OutputBuffer record = new OutputBuffer();
        key.packInto(record);
        outputObjectState.packInto(record);
        byte[] data = record.buffer();

        if (data.length > config.getBytesPerSlot()) {
            throw new IOException("data too big for slot");
        }

        // We always write to a new slot, as overwrite in place may be non-atomic and
        // risks leaving us with neither the before or after version for crash recovery.

        Integer slotId = freeList.poll();
        if (slotId == null) {
            return false;
        }

        slots.write(slotId, data, config.isSyncWrites());

        Integer previousSlot = slotIdIndex.put(key, slotId);

        // If it's a rewrite, we need to release the older version's slot
        if (previousSlot != null) {
            slots.clear(previousSlot, config.isSyncWrites());
            freeList.add(previousSlot);
        }

        return true;
    }

    /**
     * Determines if a given entry is present in the store.
     *
     * @param key The unique identifier for the entry
     * @return true if found, false otherwise
     */
    public boolean contains(SlotStoreKey key) {
        return slotIdIndex.containsKey(key);
    }

    /**
     * Return a list of all the distinct type names for records in the store
     *
     * @return a list, possibly empty but not null, of distinct types names.
     */
    public String[] getKnownTypes() {

        Set<String> types = new HashSet<>();

        for (SlotStoreKey key : slotIdIndex.keySet()) {
            types.add(key.getTypeName());
        }

        return types.toArray(new String[0]);
    }

    /**
     * Return all keys in the store having the same typename and (matching or unknown) state as the provided key.
     *
     * @param templateKey a template key to match in the search. Uid part is ignored, typename and state are used in matching
     * @return an array, possible empty but non-null, of matching keys.
     */
    public SlotStoreKey[] getMatchingKeys(SlotStoreKey templateKey) {

        List<SlotStoreKey> matchingKeys = new ArrayList<>();

        for (SlotStoreKey candidateKey : slotIdIndex.keySet()) {
            if (candidateKey.getTypeName().equalsIgnoreCase(templateKey.getTypeName()) &&
                    // OS_UNKNOWN in the template acts as a wildcard.
                    (templateKey.getStateStatus() == StateStatus.OS_UNKNOWN || candidateKey.getStateStatus() == templateKey.getStateStatus())) {
                matchingKeys.add(candidateKey);
            }
        }

        return matchingKeys.toArray(new SlotStoreKey[0]);
    }
}
