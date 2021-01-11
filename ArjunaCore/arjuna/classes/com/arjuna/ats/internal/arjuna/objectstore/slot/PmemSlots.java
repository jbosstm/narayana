/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc. and/or its affiliates,
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
 * (C) 2021,
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot;

import com.arjuna.ats.arjuna.logging.tsLogger;
import io.mashona.logwriting.ArrayStore;
import io.mashona.logwriting.PmemUtil;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of the SlotStore backend interface that uses the mashona logwriting library
 * to support fs-dax mode persistent memory storage.
 * This will compile with JDK-8+ but runtime use requires JDK-14+ for JEP-352
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2021-01
 */
public class PmemSlots implements BackingSlots {

    private SlotStoreEnvironmentBean config;
    private ArrayStore arrayStore;

    @Override
    public void init(SlotStoreEnvironmentBean config) throws IOException {
        if (this.config != null) {
            throw new IllegalStateException(tsLogger.i18NLogger.get_pmemslots_already_initialized());
        }
        this.config = config;

        File storeDir = new File(config.getStoreDir());
        if (!storeDir.exists() && !storeDir.mkdirs()) {
            throw new IOException(tsLogger.i18NLogger.get_dir_create_failed(storeDir.getCanonicalPath()));
        }

        if(!PmemUtil.isPmemSupportedFor(storeDir)) {
            throw new IOException(tsLogger.i18NLogger.get_pmem_not_supported(config.getStoreDir()));
        }

        File storeFile = new File(storeDir, "slotstore.pmem");
        arrayStore = PmemUtil.arrayStoreFor(storeFile, config.getNumberOfSlots(), config.getBytesPerSlot());
    }

    @Override
    public void write(int slot, byte[] data, boolean sync) throws IOException {
        arrayStore.write(slot, data, sync);

    }

    @Override
    public byte[] read(int slot) throws IOException {
        return arrayStore.readAsByteArray(slot);
    }

    @Override
    public void clear(int slot, boolean sync) throws IOException {
        arrayStore.clear(slot, sync);
    }
}
