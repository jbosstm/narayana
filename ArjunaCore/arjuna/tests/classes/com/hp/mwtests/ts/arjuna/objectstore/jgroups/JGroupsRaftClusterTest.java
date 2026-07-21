/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftStoreEnvironmentBean;
import org.jgroups.JChannel;
import org.jgroups.protocols.raft.RAFT;
import org.jgroups.protocols.raft.Role;
import org.jgroups.raft.blocks.ReplicatedStateMachine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JGroups Raft-based object store cluster formation and replication.
 * Raft provides strong consistency via a consensus protocol with a persistent write-ahead log.
 */
public class JGroupsRaftClusterTest extends JGroupsTestBase {

    private static final String TYPE_NAME = "StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction";

    private RaftStore store1;
    private RaftStore store2;
    private RaftStore store3;

    /**
     * Wrapper for Raft store configuration and lifecycle.
     */
    static class RaftStore {
        final JGroupsRaftStoreEnvironmentBean config;
        final JGroupsRaftSlots slots;
        final String nodeName;
        final String storeDir;
        JChannel channel;
        ReplicatedStateMachine<Integer, byte[]> stateMachine;

        RaftStore(String nodeName, String clusterName, String storeDir, String raftMembers) {
            this.nodeName = nodeName;
            this.storeDir = storeDir;
            this.config = new JGroupsRaftStoreEnvironmentBean();
            this.slots = new JGroupsRaftSlots();

            // Basic configuration
            config.setJGroupsConfigFileName("jgroups-raft.xml"); // the JGroups+RAFT protocol stack
            config.setNodeAddress(nodeName);
            config.setClusterName(clusterName);
            config.setCacheName(clusterName);
            config.setStoreDir(storeDir);
            config.setNumberOfSlots(256);
            config.setSlotKeyGeneratorClassName(SharedSlotKeyGenerator.class.getName());

            // Raft-specific configuration
            config.setRaftMembers(raftMembers);
            config.setRaftLogFsync(false);  // Disable fsync for faster tests
            config.setRaftTimeout(5000);    // 5 second timeout for leader election

            // Set the backing slots
            config.setBackingSlots(slots);
        }

        /**
         * Manually create and start the Raft channel and state machine.
         * This allows starting multiple nodes in parallel before creating the RecoveryStore.
         */
        void createAndStartRaftChannel() throws Exception {
            // Create JGroups channel
            channel = new JChannel(config.getJGroupsConfigFileName()).name(nodeName);

            // Configure RAFT protocol
            RAFT raft = channel.getProtocolStack().findProtocol(RAFT.class);
            if (raft == null) {
                throw new IllegalStateException("RAFT protocol not found in JGroups stack");
            }

            raft.logDir(storeDir);
            raft.logUseFsync(config.isRaftLogFsync());
            raft.members(java.util.Arrays.asList(config.getRaftMembers().split(",")));

            // Create replicated state machine
            stateMachine = new ReplicatedStateMachine<>(channel);
            stateMachine.raftId(nodeName);
            stateMachine.timeout(config.getRaftTimeout());

            // Connect to cluster (returns immediately, leader election happens asynchronously)
            channel.connect(config.getClusterName());

            // Set pre-configured instances in the bean
            config.setPreConfiguredChannel(channel);
            config.setPreConfiguredStateMachine(stateMachine);
        }

        void waitForLeader(long timeout, TimeUnit unit) throws Exception {
            RAFT raft = null;
            if (channel != null) {
                raft = channel.getProtocolStack().findProtocol(RAFT.class);
            } else if (slots != null && slots.getChannel() != null) {
                raft = slots.getChannel().getProtocolStack().findProtocol(RAFT.class);
            }
            assertNotNull(raft, "RAFT protocol not available");
            awaitLeaderElection(raft, timeout, unit);
        }

        String getRole() {
            // Try channel first (for pre-configured case), fall back to slots
            if (channel != null) {
                try {
                    RAFT raft = channel.getProtocolStack().findProtocol(RAFT.class);
                    if (raft == null) {
                        return "UNKNOWN";
                    }
                    String role = raft.role();

                    return role != null ? role : "UNKNOWN";
                } catch (Exception e) {
                    return "UNKNOWN";
                }
            } else if (slots != null) {
                return slots.getRole();
            }
            return "UNKNOWN";
        }

        void stop() {
            if (channel != null) {
                try {
                    channel.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (store1 != null) {
            store1.stop();
        }
        if (store2 != null) {
            store2.stop();
        }
        if (store3 != null) {
            store3.stop();
        }

        StoreManager.shutdown();

        removeDirectory(STORE_DIR);
    }

    /**
     * Test that a Raft-backed store can start and perform basic write/read operations
     */
    @Test
    public void testBasicStoreOperations() throws Throwable {
        String clusterName = "raft-basic-" + System.currentTimeMillis();
        String storeDir = STORE_DIR + "/raft-basic/node1";
        String DATA = "raft-data";

        // Single-node Raft cluster
        store1 = new RaftStore("node1", clusterName, storeDir, "node1");

        // Initialize the recovery store (this starts the Raft channel and waits for leader)
        resetAtomicActionRecoveryModule();
        RecoveryStore recoveryStore = startRecoveryStore(store1.config);

        // Verify leader was elected
        store1.waitForLeader(10, TimeUnit.SECONDS);
        assertEquals(Role.Leader.name(), store1.getRole(), "Single node should be Leader");

        // Perform basic operations
        Uid uid = new Uid();
        OutputObjectState data = new OutputObjectState();
        data.packString(DATA);

        assertTrue(recoveryStore.write_committed(uid, TYPE_NAME, data), "Write should succeed");

        var result = recoveryStore.read_committed(uid, TYPE_NAME);
        assertNotNull(result, "Should be able to read back written data");
        assertEquals(DATA, result.unpackString(), "Data mismatch");

        // Remove
        assertTrue(recoveryStore.remove_committed(uid, TYPE_NAME), "Remove should succeed");

        // Verify removed
        try {
            InputObjectState afterRemove = recoveryStore.read_committed(uid, TYPE_NAME);
            assertNull(afterRemove, "should not contain removed record");
        } catch (ObjectStoreException ignore) {
        }
    }

    /**
     * Test that data written to one Raft node is replicated to a econd Raft node.
     * <p>
     * Unlike JGroupsClusterTest, Raft requires a quorum to elect a leader, so we use
     * a 3-node cluster where we can verify replication by shutting down one node and
     * reading from another.
     * <p>
     * Pattern:
     * 1. Start 3 Raft nodes (minimum for fault tolerance)
     * 2. Wait for leader election
     * 3. Write data via node1
     * 4. Shutdown node1
     * 5. Read from node2 - a successful read verifies Raft replication
     */
    @Test
    public void testThreeNodeReplication() throws Throwable {
        String DATA = "replicated-data";
        String clusterName = "raft-replication-" + System.currentTimeMillis();
        String raftMembers = "node1,node2,node3";

        // Create three Raft nodes
        store1 = new RaftStore("node1", clusterName, STORE_DIR + "/raft-replication/node1", raftMembers);
        store2 = new RaftStore("node2", clusterName, STORE_DIR + "/raft-replication/node2", raftMembers);
        store3 = new RaftStore("node3", clusterName, STORE_DIR + "/raft-replication/node3", raftMembers);

        // Start all three Raft channels in parallel - they need to connect simultaneously to form a quorum
        store1.createAndStartRaftChannel();
        store2.createAndStartRaftChannel();
        store3.createAndStartRaftChannel();

        // Wait for leader election to complete
        store1.waitForLeader(10, TimeUnit.SECONDS);
        store2.waitForLeader(10, TimeUnit.SECONDS);
        store3.waitForLeader(10, TimeUnit.SECONDS);

        // Verify cluster formed - one leader, two followers
        int leaderCount = 0;
        int followerCount = 0;

        if (store1.getRole().equals(Role.Leader.name()))
            leaderCount += 1;
        if (store2.getRole().equals(Role.Leader.name()))
            leaderCount += 1;
        if (store3.getRole().equals(Role.Leader.name()))
            leaderCount += 1;
        if (store1.getRole().equals(Role.Follower.name()))
            followerCount += 1;
        if (store2.getRole().equals(Role.Follower.name()))
            followerCount += 1;
        if (store3.getRole().equals(Role.Follower.name()))
            followerCount += 1;

        assertEquals(1, leaderCount, "Should have exactly one leader");
        assertEquals(2, followerCount, "Should have exactly two followers");

        // Now create a RecoveryStore with store1 and write data to it
        resetAtomicActionRecoveryModule();
        RecoveryStore recoveryStore1 = startRecoveryStore(store1.config);

        Uid uid = new Uid();
        OutputObjectState writeData = new OutputObjectState();
        writeData.packString(DATA);

        assertTrue(recoveryStore1.write_committed(uid, TYPE_NAME, writeData), "Write should succeed");
        // Raft put() is synchronous: it blocks until committed to a majority, so no sleep needed.

        // Shutdown node1
        store1.stop();

        // Shutdown the RecoveryStore and recreate it with store2
        StoreManager.shutdown();
        resetAtomicActionRecoveryModule();

        RecoveryStore recoveryStore2 = startRecoveryStore(store2.config);

        // Read from node2 - should succeed because Raft replicated to majority
        InputObjectState readData = recoveryStore2.read_committed(uid, TYPE_NAME);
        assertNotNull(readData, "Data should be replicated to node2 via Raft");
        assertEquals(DATA, readData.unpackString(), "Replicated data mismatch");
    }
}
