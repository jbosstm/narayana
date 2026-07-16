/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftStoreEnvironmentBean;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test single-node JGroupsRaftSlots operations (read/write/clear).
 * <p>
 * For multi-node cluster tests, @see {@link JGroupsRaftClusterTest}.
 */
public class JGroupsRaftSlotsTest extends JGroupsTestBase {

    private static final String STORE_DIR = System.getProperty("user.dir") + "/target/jgroups-raft-test";
    private static final String NODE_NAME = "node1";

    private JGroupsRaftSlots slots;
    private JGroupsRaftStoreEnvironmentBean config;

    @BeforeEach
    public void setUp() throws Exception {
        removeDirectory(STORE_DIR); // Clean any previous test data

        // Create configuration
        config = new JGroupsRaftStoreEnvironmentBean();

        config.setJGroupsConfigFileName("jgroups-raft.xml");
        config.setNodeAddress(NODE_NAME);
        config.setClusterName("raft-test-" + System.currentTimeMillis());
        config.setCacheName(config.getClusterName());
        config.setStoreDir(STORE_DIR);
        config.setNumberOfSlots(256);
        config.setReplicationCount((short) -1);
        config.setSlotKeyGeneratorClassName(SharedSlotKeyGenerator.class.getName());

        // Raft config
        config.setRaftMembers(NODE_NAME);  // Single node for basic tests see JGroupsRaftClusterTest for multi-node
        config.setRaftLogFsync(false);  // Disable fsync for faster tests
        config.setRaftTimeout(2000);

        // Create and initialize slots
        slots = new JGroupsRaftSlots();
        slots.init(config);

        // Wait for Raft to elect leader (single node elects itself)
        waitForLeader(slots, 5000);
    }

    @AfterEach
    public void tearDown() {;
        if (slots != null) {
            slots.stop();
        }

        // Clean up test directory
        removeDirectory(STORE_DIR);
    }

    @Test
    public void testWrite() throws Exception {
        int slotId = 0;
        byte[] data = "transaction-log-entry".getBytes();

        // Write data
        slots.write(slotId, data, true);

        // Read back
        byte[] result = slots.read(slotId);

        assertNotNull(result, "Should have data at slot " + slotId);
        assertArrayEquals(data, result, "Data mismatch");
    }

    @Test
    public void testClear() throws Exception {
        int slotId = 1;
        byte[] data = "data-to-clear".getBytes();

        // Write and verify
        slots.write(slotId, data, true);
        assertNotNull(slots.read(slotId), "Should have data before clear");

        // Clear
        slots.clear(slotId, true);

        // Verify cleared
        byte[] result = slots.read(slotId);
        assertNull(result, "Should have null data after clear");
    }

    @Test
    public void testMultipleSlots() throws Exception {
        // Write to multiple slots
        for (int i = 0; i < 5; i++) {
            byte[] data = ("slot" + i).getBytes();
            slots.write(i, data, true);
        }

        // Read back all slots
        for (int i = 0; i < 5; i++) {
            byte[] expected = ("slot" + i).getBytes();
            byte[] actual = slots.read(i);
            assertNotNull(actual, "Should have data at slot" + i);
            assertArrayEquals(expected, actual, "Slot" + i + " data mismatch");
        }
    }

    @Test
    public void testUpdate() throws Exception {
        int slotId = 2;

        byte[] data1 = "v1".getBytes();
        slots.write(slotId, data1, true);
        assertArrayEquals(data1, slots.read(slotId), "Should have version 1");

        // Update to v2
        byte[] data2 = "v2".getBytes();
        slots.write(slotId, data2, true);
        assertArrayEquals(data2, slots.read(slotId), "Should have version 2");
    }

    @Test
    public void testLeaderElection() {
        // Single node should elect itself as leader
        assertTrue(slots.hasLeader(), "Should have a leader");
    }

    /**
     * Test crash recovery: write data, stop node, restart, verify that the data is recovered from the Raft log.
     * This test verifies that the in-built Raft FileBasedLog persistent write-ahead log works correctly.
     */
    @Test
    public void testCrashRecovery() throws Exception {
        int slotId = 1;
        byte[] data = "raft-crash-recovery-data".getBytes();

        slots.write(slotId, data, true);

        byte[] result = slots.read(slotId);
        assertArrayEquals(data, result, "Data should be written");

        // Simulate a crash - shutdown node
        slots.stop(); // simulating a node crash by shutting down the backing slots
        slots = null;

        // Restart node - should recover from Raft log

        // Reuse same config so the restart sees the same storeDir (where the Raft write-ahead log files are located)
        slots = new JGroupsRaftSlots();
        slots.init(config);

        // Wait for leader election (single node elects itself)
        waitForLeader(slots, 5000);

        // Verify that the data is recovered from Raft log
        byte[] recovered = slots.read(slotId);
        assertNotNull(recovered, "Data should be recovered from the Raft log");
        assertArrayEquals(data, recovered, "Recovered data mismatch");
    }

    private void waitForLeader(JGroupsRaftSlots slots, long millis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + millis;

        while (System.currentTimeMillis() < deadline) {
            if (slots.hasLeader()) {
                return;
            }
            assertDoesNotThrow(() -> TimeUnit.MILLISECONDS.sleep(50), "interrupted");
        }

        throw new AssertionError("No leader elected within " + millis + "ms");
    }
}
