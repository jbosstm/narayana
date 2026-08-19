/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.ByteArrayKey;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.SlotJournal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests write-ahead log serialization and recovery in {@link SlotJournal}.
 */
public class JGroupsWALRecoveryTest extends JGroupsTestBase {

    private static final String WAL_STORE_DIR = STORE_DIR + "/wal-recovery";

    @AfterEach
    void cleanup() {
        removeDirectory(WAL_STORE_DIR);
    }

    /**
     * Round-trip test for SlotJournal serialization: write entries with known
     * keys and data, stop the journal, restart it on the same directory, and
     * verify that the recovered keys and data match the originals.
     */
    @Test
    void testSlotJournalRoundTrip() throws Exception {
        String storeDir = WAL_STORE_DIR + "/roundtrip";
        int entryCount = 5;

        JGroupsStoreEnvironmentBean config = createWALConfig(storeDir, 20);

        ByteArrayKey[] keys = new ByteArrayKey[entryCount];
        byte[][] data = new byte[entryCount][];
        for (int i = 0; i < entryCount; i++) {
            keys[i] = new ByteArrayKey(("key-" + i).getBytes(StandardCharsets.UTF_8));
            data[i] = ("payload-" + i).getBytes(StandardCharsets.UTF_8);
        }

        // Phase 1: write entries and stop
        SlotJournal journal1 = new SlotJournal(config);
        journal1.start();
        for (int i = 0; i < entryCount; i++) {
            journal1.write(i, keys[i], data[i]);
        }
        journal1.stop();

        // Phase 2: restart on the same directory and verify
        SlotJournal journal2 = new SlotJournal(config);
        journal2.start();

        assertEquals(entryCount, journal2.size(), "All entries should be recovered");

        for (int i = 0; i < entryCount; i++) {
            byte[] recovered = journal2.read(i);
            assertNotNull(recovered, "Slot " + i + " data should be recovered");
            assertArrayEquals(data[i], recovered, "Slot " + i + " data mismatch");

            ByteArrayKey recoveredKey = journal2.getKey(i);
            assertNotNull(recoveredKey, "Slot " + i + " key should be recovered");
            assertEquals(keys[i], recoveredKey, "Slot " + i + " key mismatch");
        }

        journal2.stop();
    }

    // --- Helpers ---

    private JGroupsStoreEnvironmentBean createWALConfig(String storeDir, int numSlots) {
        JGroupsStoreEnvironmentBean config = new JGroupsStoreEnvironmentBean();
        config.setExperimentalEnabled(true);
        config.setNodeAddress("node1");
        config.setCacheName("wal-" + System.nanoTime());
        config.setStoreDir(storeDir);
        config.setJGroupsConfigFileName(JGROUPS_CONFIG_FILE);
        config.setWalSyncWrites(true);
        config.setWalSyncDeletes(true);
        config.setNumberOfSlots(numSlots);
        return config;
    }
}
