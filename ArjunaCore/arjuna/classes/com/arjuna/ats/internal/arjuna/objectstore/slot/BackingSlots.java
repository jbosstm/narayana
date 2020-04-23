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
