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
