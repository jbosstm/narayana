/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot;

import java.io.IOException;

/**
 * Interface for pluggable internal implementations of the SlotStore backend.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2020-04
 */
public interface BackingSlots {

    /**
     * Initialize the instance from the given configuration.
     * This must be called once per instance, before any other method is called.
     *
     * @param config The configuration to apply.
     */
    void init(SlotStoreEnvironmentBean config) throws IOException;

    /**
     * Update the given slot with the provided data, overwriting (non-atomically) any existing data.
     * The update may not be immediately persistent, depending on config syncWrites
     *
     * @param slot the index, from 0 to config numberOfSlots-1
     * @param sync true for immediate persistence, false otherwise
     * @param data the content.
     */
    void write(int slot, byte[] data, boolean sync) throws IOException;

    /**
     * Read the given slot, returning its contents.
     *
     * @param slot the index, from 0 to config numberOfSlots-1
     * @return the content, or null if the slot has not been written or has been cleared.
     */
    byte[] read(int slot) throws IOException;

    /**
     * Update the given slot, discarding the contents.
     * The update may not be immediately persistent, depending on config syncDeletes and isSyncWrites
     *
     * @param slot the index, from 0 to config numberOfSlots-1
     * @param sync true for immediate persistence, false otherwise
     */
    void clear(int slot, boolean sync) throws IOException;

    /**
     * Release resources held by this instance.
     * Called once when the store is being shut down.
     * The default implementation is a no-op.
     */
    default void stop() throws IOException {
    }

    /**
     * Indicates whether the backing store may contain changes that are not
     * yet reflected in {@link SlotStore}'s in-memory index.
     *
     * <p>This can happen when a distributed consensus protocol (e.g. Raft)
     * replicates slot data to this node via its own {@code apply()} path,
     * bypassing {@link SlotStore#write} and {@link SlotStore#remove}.
     * For example, the Raft implementation sets this flag on leader election
     * and snapshot install, since either event may introduce slot data that
     * the local {@link SlotStore} has not indexed.
     *
     * <p>When this method returns {@code true}, {@link SlotStore} calls
     * {@link SlotStore#refreshIndex()} before index-dependent operations
     * ({@link SlotStore#read}, {@link SlotStore#getMatchingKeys},
     * {@link SlotStore#getKnownTypes}) to reconcile the index with the
     * current state of the backing store.
     *
     * <p>The default implementation returns {@code false}, which is correct
     * for backends where all mutations go through {@link SlotStore}.
     *
     * @return {@code true} if the index may be stale and a refresh is needed,
     *         {@code false} otherwise
     */
    default boolean isIndexStale() {
        return false;
    }

    /**
     * Acknowledges that {@link SlotStore#refreshIndex()} has run and the
     * index is now up to date.  Implementations that set a staleness flag
     * should clear it here.
     */
    default void clearIndexStale() {
    }
}