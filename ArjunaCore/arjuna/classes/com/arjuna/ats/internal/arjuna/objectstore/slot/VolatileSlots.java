/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot;

/**
 * Trivial in-memory backend for the SlotStore, useful for benchmarking but not much else.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2020-03
 */
public class VolatileSlots implements BackingSlots {

    private byte[][] slots = null;

    @Override
    public synchronized void init(SlotStoreEnvironmentBean config) {
        if (slots == null) {
            slots = new byte[config.getNumberOfSlots()][];
        } else {
            throw new IllegalStateException("already initialized");
        }
    }

    @Override
    public void write(int slot, byte[] data, boolean sync) {
        slots[slot] = data;
    }

    @Override
    public byte[] read(int slot) {
        return slots[slot];
    }

    @Override
    public void clear(int slot, boolean sync) {
        slots[slot] = null;
    }
}