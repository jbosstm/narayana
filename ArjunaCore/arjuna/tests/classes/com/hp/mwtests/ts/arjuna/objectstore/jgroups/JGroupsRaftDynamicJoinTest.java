/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftStoreEnvironmentBean;
import org.jgroups.protocols.raft.RAFT;
import org.jgroups.protocols.raft.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests dynamic join protocol for JGroups Raft-based stores.
 * Verifies that a node with no raftMembers configured can bootstrap a new cluster
 * or auto-discover and join an existing one via JGroupsRaftSlots.init().
 *
 * Note that JGroupsSlots does not require an equivalent DynamicJoinTest because with the ReplCache that backs
 * JGroupsSlots any node that discovers the cluster via SHARED_LOOPBACK_PING / TCPPING / etc. automatically joins
 * the group and starts participating (there's no voting, no quorum requirement - it's an eventually-consistent replicated cache).
 * <p>
 * On the other hand Raft requires a quorum + leader election to exist before making progress, the discovery protocol
 * only tells nodes how to find each other on the network, not who the voting members are.
 * <p>
 * Note that when a new node joins the cluster it's just a Learner - it can observe but can't vote or accept writes
 * and has to wait to be explicitly added via addServer() (which is itself a Raft log entry that requires leader consensus).
 */
public class JGroupsRaftDynamicJoinTest extends JGroupsTestBase {

    private JGroupsRaftSlots slots1;
    private JGroupsRaftSlots slots2;

    @AfterEach
    public void tearDown() {
        if (slots2 != null) {
            try { slots2.stop(); } catch (Exception ignore) {}
        }
        if (slots1 != null) {
            try { slots1.stop(); } catch (Exception ignore) {}
        }

        removeDirectory(STORE_DIR);
    }

    /**
     * A single node with empty raftMembers should bootstrap a new cluster via
     * JGroupsRaftSlots.joinOrBootstrap and bootstrapNewChannel, becoming Leader.
     */
    @Test
    public void testSingleNodeBootstrap() throws Exception {
        String clusterName = "raft-bootstrap-" + System.currentTimeMillis();
        String storeDir = STORE_DIR + "/raft-bootstrap/node1";

        JGroupsRaftStoreEnvironmentBean config = new JGroupsRaftStoreEnvironmentBean();
        config.setJGroupsConfigFileName("jgroups-raft.xml");
        config.setNodeAddress("node1");
        config.setClusterName(clusterName);
        config.setCacheName(clusterName);
        config.setStoreDir(storeDir);
        config.setNumberOfSlots(256);
        config.setRaftLogFsync(false);
        config.setRaftTimeout(5000);
        // Empty raftMembers triggers joinOrBootstrap → bootstrapNewChannel
        config.setRaftMembers("");

        slots1 = new JGroupsRaftSlots();
        slots1.init(config);

        // Should have bootstrapped as Leader of a single-member cluster
        assertTrue(slots1.hasLeader(), "Should have a leader after bootstrap");
        assertEquals(Role.Leader.name(), slots1.getRole(), "Should be Leader of new bootstrap cluster");

        // Verify the store works (write and read via the replicated state machine)
        slots1.write(1, "hello".getBytes(), true);
        assertArrayEquals("hello".getBytes(), slots1.read(1));
    }

    /**
     * Node1 bootstraps as the founding member. Node2 starts with empty raftMembers
     * and should auto-discover node1's cluster via JGroupsRaftSlots.joinOrBootstrap,
     * joining via REDIRECT.addServer and being promoted from Learner to Follower.
     */
    @Test
    public void testDynamicJoin() throws Exception {
        String node1Msg = "from node1";
        String clusterName = "raft-join-" + System.currentTimeMillis();
        String storeDir1 = STORE_DIR + "/raft-join/node1";
        String storeDir2 = STORE_DIR + "/raft-join/node2";

        // Node1: founding member with explicit raftMembers
        JGroupsRaftStoreEnvironmentBean config1 = new JGroupsRaftStoreEnvironmentBean();
        config1.setJGroupsConfigFileName("jgroups-raft.xml");
        config1.setNodeAddress("node1");
        config1.setClusterName(clusterName);
        config1.setCacheName(clusterName);
        config1.setStoreDir(storeDir1);
        config1.setNumberOfSlots(256);
        config1.setRaftMembers("node1");
        config1.setRaftLogFsync(false);
        config1.setRaftTimeout(5000);

        slots1 = new JGroupsRaftSlots();
        slots1.init(config1);

        assertTrue(slots1.hasLeader(), "node1 should have a leader");
        assertEquals(Role.Leader.name(), slots1.getRole(), "node1 should be Leader");

        // Write data via node1 before node2 joins
        slots1.write(42, node1Msg.getBytes(), true);

        // Node2: empty raftMembers — init() triggers joinOrBootstrap which finds
        // node1 as leader and joins via REDIRECT.addServer
        JGroupsRaftStoreEnvironmentBean config2 = new JGroupsRaftStoreEnvironmentBean();
        config2.setJGroupsConfigFileName("jgroups-raft.xml");
        config2.setNodeAddress("node2");
        config2.setClusterName(clusterName);
        config2.setCacheName(clusterName);
        config2.setStoreDir(storeDir2);
        config2.setNumberOfSlots(256);
        config2.setRaftMembers("");
        config2.setRaftLogFsync(false);
        config2.setRaftTimeout(5000);

        slots2 = new JGroupsRaftSlots();
        slots2.init(config2);

        // Wait for node2 to be promoted from Learner to Follower via role change notification
        RAFT raft2 = slots2.getChannel().getProtocolStack().findProtocol(RAFT.class);
        if (!Role.Follower.name().equalsIgnoreCase(raft2.role())) {
            CountDownLatch followerLatch = new CountDownLatch(1);
            raft2.addRoleListener(role -> {
                if (role == Role.Follower) {
                    followerLatch.countDown();
                }
            });
            // Re-check after registering to avoid race
            if (!Role.Follower.name().equalsIgnoreCase(raft2.role())) {
                assertTrue(followerLatch.await(10, TimeUnit.SECONDS),
                        "node2 should be promoted to Follower");
            }
        }

        // Verify node2 received the data written by node1 (replicated via Raft log)
        waitFor(5000, "data replication to node2", () -> slots2.read(42) != null);
        assertArrayEquals(node1Msg.getBytes(), slots2.read(42),
                "Data written by node1 should be replicated to node2");

        // Verify node1's RAFT members list now includes node2
        assertNotNull(slots1.getChannel(), "node1 channel should be available");
        org.jgroups.protocols.raft.RAFT raft1 = slots1.getChannel().getProtocolStack().findProtocol(
                org.jgroups.protocols.raft.RAFT.class);
        assertTrue(raft1.members().contains("node2"), "node1's members should include node2 after addServer");

        // Write new data via node1 and verify node2 receives it
        slots1.write(43, "afterJoin".getBytes(), true);
        waitFor(5000, "new data replication to node2", () -> slots2.read(43) != null);
        assertArrayEquals("afterJoin".getBytes(), slots2.read(43));
    }
}
