/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot;

import com.arjuna.ats.arjuna.logging.tsLogger;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.zip.CRC32;

/**
 * Memory-mapped file implementation of the SlotStore backend.
 * WARNING: this is currently a test implementation and is NOT SAFE for production use yet.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2020-04
 */
public class MappedDiskSlots implements BackingSlots {

    private SlotStoreEnvironmentBean config;
    private int slotSize;

    private FileChannel[] fileChannels;
    private MappedByteBuffer[] slots;

    @Override
    public synchronized void init(SlotStoreEnvironmentBean config) throws IOException {
        if (this.config != null) {
            throw new IllegalStateException("already initialized");
        }

        this.config = config;
        slotSize = config.getBytesPerSlot()+(2*Integer.BYTES); // overhead for size+checksum fields

        File storeDir = new File(config.getStoreDir());
        if (!storeDir.exists() && !storeDir.mkdirs()) {
            throw new IOException(tsLogger.i18NLogger.get_dir_create_failed(storeDir.getCanonicalPath()));
        }

        fileChannels = new FileChannel[config.getNumberOfSlots()];
        slots = new MappedByteBuffer[config.getNumberOfSlots()];

        for (int i = 0; i < slots.length; i++) {
            File file = new File(storeDir, "slot." + i);

            fileChannels[i] = (FileChannel) Files
                    .newByteChannel(file.toPath(), EnumSet.of(
                            StandardOpenOption.READ,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE));

            slots[i] = fileChannels[i].map(FileChannel.MapMode.READ_WRITE, 0, slotSize);
        }
    }

    @Override
    public void write(int slot, byte[] data, boolean sync) throws IOException {
        MappedByteBuffer mappedByteBuffer = slots[slot];
        synchronized (mappedByteBuffer) {

            mappedByteBuffer.position(0);
            mappedByteBuffer.putInt(data.length);
            mappedByteBuffer.put(data);
            mappedByteBuffer.putInt(checksum(data));

            if (sync) {
                mappedByteBuffer.force();
                // JDK 14 mappedByteBuffer.force(0, mappedByteBuffer.position());
            }
        }
    }

    @Override
    public byte[] read(int slot) throws IOException {
        MappedByteBuffer mappedByteBuffer = slots[slot];
        synchronized (mappedByteBuffer) {

            mappedByteBuffer.position(0);
            if(mappedByteBuffer.remaining() < 8) {
                return null;
            }

            try {
                int dataLength = mappedByteBuffer.getInt();
                byte[] data = new byte[dataLength];
                mappedByteBuffer.get(data);
                int expectedChecksum = mappedByteBuffer.getInt();
                if (expectedChecksum == checksum(data)) {
                    return data;
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Override
    public void clear(int slot, boolean sync) throws IOException {
        write(slot, new byte[0], sync);
    }

    private int checksum(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data, 0, data.length);
        int checksum = (int) crc32.getValue();
        return checksum;
    }
}