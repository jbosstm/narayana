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
}