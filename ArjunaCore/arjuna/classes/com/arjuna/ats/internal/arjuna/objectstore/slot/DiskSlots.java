/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot;

import com.arjuna.ats.arjuna.logging.tsLogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;

/**
 * File backed implementation of the SlotStore backend.
 * WARNING: this is currently a test implementation and is NOT SAFE for production use yet.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2020-04
 */
public class DiskSlots implements BackingSlots {

    private SlotStoreEnvironmentBean config;
    private RandomAccessFile[] slots;

    @Override
    public synchronized void init(SlotStoreEnvironmentBean config) throws IOException {
        if (this.config != null) {
            throw new IllegalStateException("already initialized");
        }

        this.config = config;

        File storeDir = new File(config.getStoreDir());
        if (!storeDir.exists() && !storeDir.mkdirs()) {
            throw new IOException(tsLogger.i18NLogger.get_dir_create_failed(storeDir.getCanonicalPath()));
        }
        slots = new RandomAccessFile[config.getNumberOfSlots()];
        for (int i = 0; i < slots.length; i++) {
            File file = new File(storeDir, "slot." + i);
            slots[i] = new RandomAccessFile(file, "rw");
        }
    }

    @Override
    public void write(int slot, byte[] data, boolean sync) throws IOException {
        RandomAccessFile randomAccessFile = slots[slot];
        synchronized (randomAccessFile) {

            // randomAccessFile.writeInt() is slower than the memory copy for our smallish records.
            byte[] record = new byte[data.length+8];
            java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.wrap(record);
            byteBuffer.putInt(data.length);
            byteBuffer.put(data);
            byteBuffer.putInt(checksum(data));

            randomAccessFile.seek(0);
            randomAccessFile.write(record);

            if (sync) {
                randomAccessFile.getFD().sync();
            }
        }
    }

    @Override
    public byte[] read(int slot) throws IOException {
        RandomAccessFile randomAccessFile = slots[slot];
        synchronized (randomAccessFile) {

            if (randomAccessFile.length() < 8) {
                return null;
            }

            randomAccessFile.seek(0);
            int dataLength = randomAccessFile.readInt();
            byte[] data = new byte[dataLength];

            try {
                randomAccessFile.read(data);
                int expectedChecksum = randomAccessFile.readInt();
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