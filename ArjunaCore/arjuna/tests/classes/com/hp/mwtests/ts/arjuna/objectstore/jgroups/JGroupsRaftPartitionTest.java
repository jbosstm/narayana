/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputBuffer;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreKey;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsRaftStoreEnvironmentBean;
import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.protocols.DISCARD;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.raft.ELECTION;
import org.jgroups.protocols.raft.NO_DUPES;
import org.jgroups.protocols.raft.RAFT;
import org.jgroups.protocols.raft.REDIRECT;
import org.jgroups.raft.blocks.ReplicatedStateMachine;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests Raft consensus behavior under network partitions and unreliable networks, exercised through the
 * {@link RecoveryStore} and {@link AtomicAction} APIs.
 * <p>
 * Channels are built programmatically via {@link Util#getTestStack} (which omits VERIFY_SUSPECT, FD_ALL3,
 * and MERGE3) so that injected suspicions are not automatically reversed - in more detail:
 * <p>
 * The partition tests simulate network failures by injecting Event.SUSPECT events into the bottom of the
 * protocol stack. This tells the GMS layer that certain members have crashed, causing it to install a new
 * view that excludes them - effectively splitting the cluster.
 * <p>
 *   The production stack includes protocols that would undo this:
 *   <ul>
 *       <li><b>VERIFY_SUSPECT</b> receives the SUSPECT event before GMS does and probes the suspected
 *       member to confirm it's actually unreachable. Since our nodes are all alive on SHARED_LOOPBACK,
 *       the probe succeeds, VERIFY_SUSPECT silently drops the SUSPECT event, and GMS never sees it so
 *       no partition occurs.
 *       <li><b>FD_ALL3 (failure detection)</b> periodically heartbeats all members. Even if we managed
 *       to get past VERIFY_SUSPECT and install a split view, FD_ALL3 would detect that the "failed"
 *       members are still sending heartbeats and wouldn't generate new SUSPECT events to sustain the
 *       partition. Meanwhile, members in separate partitions would continue receiving heartbeats from
 *       each other through SHARED_LOOPBACK.
 *       <li><b>MERGE3</b> periodically has coordinators exchange view information. When two coordinators
 *       discover they have different views of the same cluster, MERGE3 automatically triggers a merge -
 *       healing any partition we created. The partition would last only until the next MERGE3 interval
 *       (1-5 seconds typically).
 *   </ul>
 * <p>
 *   Util.getTestStack() builds a minimal stack (SHARED_LOOPBACK, SHARED_LOOPBACK_PING, NAKACK2, UNICAST3,
 *   STABLE, GMS, FRAG2) that has none of these. Injected SUSPECT events reach GMS unverified, the split
 *   views persist indefinitely, and merges only happen when we explicitly inject Event.MERGE - giving the
 *   test full control over partition timing.
 * <p>
 *   For the DISCARD-based packet loss test, these protocols would also interfere: FD_ALL3 might suspect
 *   nodes whose heartbeats were dropped, and VERIFY_SUSPECT would then probe them, adding traffic and
 *   state changes that make the test nondeterministic.
 * <p>
 * Three scenarios are tested:
 * <ol>
 *   <li><b>Minority partition</b> (3 nodes, isolate 1): the 2-node majority retains quorum and can commit;
 *   the isolated node cannot.</li>
 *   <li><b>Split brain</b> (4 nodes, 2+2): quorum is 3, so neither half can commit. After merge the cluster
 *   recovers.</li>
 *   <li><b>Packet loss</b> (3 nodes, 30% drop rate): Raft's retry mechanism ensures all committed entries
 *   eventually replicate despite random message loss.</li>
 * </ol>
 */
public class JGroupsRaftPartitionTest extends JGroupsTestBase {

    private static final int RAFT_TIMEOUT_MS = 3000;
    private static final String TYPE_NAME = "StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction";

    private final List<RaftNode> nodes = new ArrayList<>();

    /**
     * Prepare succeeds but commit fails so the transaction record stays in the store
     * (BasicAction.updateState sees a non-empty failedList and re-writes the log).
     */
    static class CrashInCommitRecord extends AbstractRecord {
        @Override public int topLevelPrepare()                   { return TwoPhaseOutcome.PREPARE_OK; }
        @Override public int topLevelCommit()                    { return TwoPhaseOutcome.FINISH_ERROR; }
        @Override public int topLevelAbort()                     { return TwoPhaseOutcome.FINISH_OK; }
        @Override public int nestedPrepare()                     { return 0; }
        @Override public int nestedCommit()                      { return 0; }
        @Override public int nestedAbort()                       { return 0; }
        @Override public int typeIs()                            { return 0; }
        @Override public Object value()                          { return null; }
        @Override public void setValue(Object o)                 { }
        @Override public void merge(AbstractRecord a)            { }
        @Override public void alter(AbstractRecord a)            { }
        @Override public boolean shouldAdd(AbstractRecord a)     { return true; }
        @Override public boolean shouldAlter(AbstractRecord a)   { return false; }
        @Override public boolean shouldMerge(AbstractRecord a)   { return false; }
        @Override public boolean shouldReplace(AbstractRecord a) { return false; }
    }

    /**
     * Wraps a Raft node: JChannel, ReplicatedStateMachine, and the environment bean needed by
     * {@link JGroupsRaftSlots} / {@link RecoveryStore}.
     */
    static class RaftNode {
        final String name;
        final String storeDir;
        final JGroupsRaftStoreEnvironmentBean config;
        final JGroupsRaftSlots slots;
        JChannel channel;
        ReplicatedStateMachine<Integer, byte[]> sm;

        RaftNode(String name, String storeDir) {
            this.name = name;
            this.storeDir = storeDir;
            this.config = new JGroupsRaftStoreEnvironmentBean();
            config.setExperimentalEnabled(true);
            this.slots = new JGroupsRaftSlots();

            config.setStoreDir(storeDir);
            config.setNumberOfSlots(256);
            config.setRaftLogFsync(false);
            config.setRaftTimeout(RAFT_TIMEOUT_MS);
            config.setRaftElectionMaxInterval(10_000);
            config.setBackingSlots(slots);
        }

        void start(String clusterName, List<String> members) throws Exception {
            RAFT raft = new RAFT();
            raft.logDir(storeDir);
            raft.logUseFsync(false);
            raft.members(members);

            channel = new JChannel(Util.getTestStack(
                    new NO_DUPES(), new ELECTION(), raft, new REDIRECT()))
                    .name(name);

            sm = new ReplicatedStateMachine<>(channel);
            sm.raftId(name);
            sm.timeout(RAFT_TIMEOUT_MS);

            channel.connect(clusterName);

            config.setPreConfiguredChannel(channel);
            config.setPreConfiguredStateMachine(sm);
            config.setNodeAddress(name);
            config.setClusterName(clusterName);
            config.setCacheName(clusterName);
            config.setRaftMembers(String.join(",", members));
        }

        void stop() {
            if (channel != null) {
                try {
                    channel.close();
                } catch (Exception ignore) {
                }
            }
        }

        RAFT raft() {
            return channel.getProtocolStack().findProtocol(RAFT.class);
        }

        boolean isLeader() {
            return raft().isLeader();
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        StoreManager.shutdown();
        for (RaftNode node : nodes) {
            node.stop();
        }
        nodes.clear();
        removeDirectory(STORE_DIR);
    }

    private RaftNode createAndStartNode(String name, String clusterName, List<String> members) throws Exception {
        String storeDir = STORE_DIR + "/partition-test/" + clusterName + "/" + name;
        RaftNode node = new RaftNode(name, storeDir);
        nodes.add(node);
        node.start(clusterName, members);
        return node;
    }

    private RaftNode findLeader() {
        return nodes.stream().filter(RaftNode::isLeader).findFirst()
                .orElseThrow(() -> new AssertionError("No leader found"));
    }

    private void awaitAllLeaders() throws Exception {
        waitFor(30_000, "every node to report a Raft leader",
                () -> nodes.stream().allMatch(n -> n.raft().leader() != null));
    }

    /**
     * Simulate a network partition by injecting {@link Event#SUSPECT} events at the
     * bottom of each node's protocol stack. Every node in one partition suspects all
     * nodes in the other. Because the test stack omits VERIFY_SUSPECT, the suspicions
     * reach GMS unverified, and it installs a new view excluding the suspected members.
     * Blocks until each partition's channels converge on their own sub-view.
     *
     * @param part1 nodes in the first partition
     * @param part2 nodes in the second partition
     * @throws TimeoutException if the sub-views do not converge within 30 seconds
     */
    private void partition(List<RaftNode> part1, List<RaftNode> part2) throws TimeoutException {
        List<List<RaftNode>> parts = List.of(part1, part2);
        for (List<RaftNode> part : parts) {
            List<Address> suspects = parts.stream()
                    .filter(other -> other != part) // exclude own partition
                    .flatMap(List::stream)
                    .map(n -> n.channel.address())
                    .collect(Collectors.toList());
            // for each node in this partition send a SUSPECT event containing the nodes in the other partition
            for (RaftNode n : part) {
                n.channel.stack().getBottomProtocol().up(new Event(Event.SUSPECT, suspects));
            }
            Util.waitUntilAllChannelsHaveSameView(30_000, 1000,
                    part.stream().map(n -> n.channel).toArray(JChannel[]::new));
        }
    }

    /**
     * Heal a partition by injecting {@link Event#MERGE} events at the view coordinators
     * of each sub-partition. The MERGE event carries a map of coordinator addresses to
     * their current views; GMS uses this to run its merge protocol and install a single
     * unified view across all nodes. Because the test stack omits MERGE3, this is the
     * only way a split view is healed. Blocks until both coordinators finish their
     * merge tasks.
     *
     * @param part1 nodes in the first partition
     * @param part2 nodes in the second partition
     * @throws TimeoutException if the merge does not complete within 30 seconds
     */
    private void mergePartitions(List<RaftNode> part1, List<RaftNode> part2) throws TimeoutException {
        RaftNode coord1 = findViewCoordinator(part1);
        RaftNode coord2 = findViewCoordinator(part2);
        Map<Address, View> views = Map.of(
                coord1.channel.address(), coord1.channel.view(),
                coord2.channel.address(), coord2.channel.view());
        coord1.channel.stack().getBottomProtocol().up(new Event(Event.MERGE, views));
        coord2.channel.stack().getBottomProtocol().up(new Event(Event.MERGE, views));

        GMS gms1 = coord1.channel.stack().findProtocol(GMS.class);
        GMS gms2 = coord2.channel.stack().findProtocol(GMS.class);
        Util.waitUntil(30_000, 1000, () -> !gms1.isMergeTaskRunning());
        Util.waitUntil(30_000, 1000, () -> !gms2.isMergeTaskRunning());
    }

    /**
     * Find the view coordinator within a partition. The coordinator is the first
     * member in the JGroups view and is the node that GMS designates to drive
     * merge and membership-change protocols.
     */
    private RaftNode findViewCoordinator(List<RaftNode> partition) {
        Address coordAddr = partition.get(0).channel.view().getCoord();
        return partition.stream()
                .filter(n -> n.channel.address().equals(coordAddr))
                .findFirst().orElseThrow();
    }

    // Helper methods for RecoveryStore and AtomicAction

    /**
     * Activate the {@link RecoveryStore} backed by the given node's
     * pre-configured Raft channel and state machine.
     */
    private RecoveryStore activateStore(RaftNode node) throws Throwable {
        arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier(node.name);
        resetAtomicActionRecoveryModule();
        return startRecoveryStore(node.config);
    }

    private Uid createInDoubtTransaction() {
        AtomicAction aa = new AtomicAction();
        aa.begin();
        aa.add(new CrashInCommitRecord());
        aa.add(new CrashInCommitRecord());
        aa.commit(true);
        return aa.getSavingUid();
    }

    // Tests

    /**
     * Minority partition: isolate 1 of 3 nodes.
     * <p>
     * The 2-node majority retains quorum and can commit new transactions via {@link RecoveryStore}.
     * After the partition heals, the isolated node catches up and the data is visible through its store.
     */
    @Test
    void testMinorityPartition() throws Throwable {
        String cluster = "raft-minority-" + System.currentTimeMillis();
        List<String> members = List.of("P1", "P2", "P3");

        createAndStartNode("P1", cluster, members);
        createAndStartNode("P2", cluster, members);
        createAndStartNode("P3", cluster, members);
        awaitAllLeaders();

        RaftNode leader = findLeader();
        List<RaftNode> followers = nodes.stream().filter(n -> !n.isLeader()).toList();
        assertEquals(2, followers.size(),
                "Expected exactly one leader and two followers, found followers: " + followers.size());
        RaftNode isolated = followers.get(0);
        RaftNode remaining = followers.get(1);

        // Write initial data through RecoveryStore on the leader
        RecoveryStore rs = activateStore(leader);

        Uid uid1 = new Uid();
        OutputObjectState data1 = new OutputObjectState();
        data1.packString("before-partition");
        assertTrue(rs.write_committed(uid1, TYPE_NAME, data1),
                "Pre-partition write should succeed");

        // Partition: isolate one follower
        List<RaftNode> majority = List.of(leader, remaining);
        List<RaftNode> minority = List.of(isolated);
        partition(majority, minority);

        // Majority (2 nodes, quorum=2) can still commit via RecoveryStore
        Uid uid2 = new Uid();
        OutputObjectState data2 = new OutputObjectState();
        data2.packString("during-partition");
        assertTrue(rs.write_committed(uid2, TYPE_NAME, data2),
                "Majority-side write should succeed during partition");

        // Merge
        mergePartitions(majority, minority);
        Util.waitUntilAllChannelsHaveSameView(30_000, 1000,
                nodes.stream().map(n -> n.channel).toArray(JChannel[]::new));
        awaitAllLeaders();

        // Switch RecoveryStore to the isolated node and verify both records arrived
        RecoveryStore isolatedRs = activateStore(isolated);
        InputObjectState read1 = isolatedRs.read_committed(uid1, TYPE_NAME);
        assertNotNull(read1, "Isolated node should see pre-partition data after merge");
        assertEquals("before-partition", read1.unpackString());

        InputObjectState read2 = isolatedRs.read_committed(uid2, TYPE_NAME);
        assertNotNull(read2, "Isolated node should see data written during partition");
        assertEquals("during-partition", read2.unpackString());
    }

    /**
     * Split brain: 4-node cluster split into two halves of 2.
     * <p>
     * Quorum for 4 nodes is 3, so neither half can commit new entries via
     * {@link RecoveryStore}. After the partition heals, a leader is elected
     * and normal operation resumes.
     * <p>
     * An even-sized cluster is intentional - it creates a true split where neither
     * side has a majority.
     */
    @Test
    void testSplitBrain() throws Throwable {
        String cluster = "raft-split-" + System.currentTimeMillis();
        List<String> members = List.of("S1", "S2", "S3", "S4");

        createAndStartNode("S1", cluster, members);
        createAndStartNode("S2", cluster, members);
        createAndStartNode("S3", cluster, members);
        createAndStartNode("S4", cluster, members);
        awaitAllLeaders();

        RaftNode leader = findLeader();

        // Write initial data through RecoveryStore
        RecoveryStore rs = activateStore(leader);
        Uid uid1 = new Uid();
        OutputObjectState data1 = new OutputObjectState();
        data1.packString("before-split");
        assertTrue(rs.write_committed(uid1, TYPE_NAME, data1));

        // Split 2+2: leader + one follower vs the other two
        List<RaftNode> leaderSide = new ArrayList<>();
        List<RaftNode> otherSide = new ArrayList<>();
        leaderSide.add(leader);
        boolean addedExtra = false;
        for (RaftNode n : nodes) {
            if (n == leader)
                continue;
            if (!addedExtra) {
                leaderSide.add(n);
                addedExtra = true; // leaderSide now has two so don't add more
            } else {
                otherSide.add(n); // otherSide will get 2 because there are 4 nodes
            }
        }

        // Partition: 2+2
        partition(leaderSide, otherSide);

        // Leader side: quorum=3, only 2 nodes → write cannot reach consensus
        Uid uid2 = new Uid();
        OutputObjectState data2 = new OutputObjectState();
        data2.packString("should-fail");
        assertThrows(ObjectStoreException.class,
                () -> rs.write_committed(uid2, TYPE_NAME, data2),
                "Leader-side write should fail: 2 nodes < quorum of 3");

        // Merge
        mergePartitions(leaderSide, otherSide);
        Util.waitUntilAllChannelsHaveSameView(30_000, 1000,
                nodes.stream().map(n -> n.channel).toArray(JChannel[]::new));
        awaitAllLeaders();

        // Cluster recovers: RecoveryStore writes succeed again
        Uid uid3 = new Uid();
        OutputObjectState data3 = new OutputObjectState();
        data3.packString("after-merge");
        assertTrue(rs.write_committed(uid3, TYPE_NAME, data3),
                "Write should succeed after merge restores quorum");

        assertNotNull(rs.read_committed(uid1, TYPE_NAME)); // pre-split record
        assertNotNull(rs.read_committed(uid3, TYPE_NAME)); // post merge record
    }

    /**
     * Packet loss: 30% of outgoing messages are randomly dropped via the
     * {@link DISCARD} protocol.
     * <p>
     *     In the Raft consensus algorithm, the retry mechanism handles
     *     lost network messages, or crashed or slow nodes, or log mismatch,
     *     by having leaders repeat Remote Procedure Calls (RPCs), specifically
     *     AppendEntries and RequestVote, indefinitely until the target
     *     followers respond and catch up ensuring that every committed write
     *     eventually replicates to all nodes.
     * </p>
     * <p>
     * Uses {@link AtomicAction} with {@link CrashInCommitRecord} to create
     * in-doubt transactions under packet loss, then verifies all records
     * replicated to a follower via {@link RecoveryStore}.
     */
    @Test
    void testPacketLoss() throws Throwable {
        String cluster = "raft-packetloss-" + System.currentTimeMillis();
        List<String> members = List.of("L1", "L2", "L3");

        createAndStartNode("L1", cluster, members);
        createAndStartNode("L2", cluster, members);
        createAndStartNode("L3", cluster, members);
        awaitAllLeaders();

        RaftNode leader = findLeader();
        RaftNode follower = nodes.stream()
                .filter(n -> !n.isLeader()).findFirst().orElseThrow();

        // Insert DISCARD protocol on every node: 30% of outgoing messages dropped
        List<DISCARD> discards = new ArrayList<>();
        for (RaftNode n : nodes) {
            DISCARD discard = new DISCARD();
            discard.setDownDiscardRate(0.3);
            n.channel.getProtocolStack().insertProtocol(
                    discard, ProtocolStack.Position.ABOVE,
                    n.channel.getProtocolStack().getTransport().getClass());
            discards.add(discard);
        }

        // Increase RSM timeout to accommodate retries under packet loss
        for (RaftNode n : nodes) {
            n.sm.timeout(10_000);
        }

        // Activate RecoveryStore on the leader
        RecoveryStore rs = activateStore(leader);

        // Create 10 in-doubt transactions under packet loss
        List<Uid> txnUids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Uid uid = createInDoubtTransaction();
            assertTrue(uid.notEquals(Uid.nullUid()),
                    "Transaction " + i + " should have been written to the store under packet loss");
            txnUids.add(uid);
        }

        // Verify all records are in the leader's RecoveryStore
        for (Uid uid : txnUids) {
            AtomicAction probe = new AtomicAction(uid);
            assertTrue(containsAtomicAction(rs, probe),
                    "Leader store should contain txn " + uid);
        }

        // Remove packet loss
        for (DISCARD d : discards) {
            d.setDownDiscardRate(0.0);
        }

        // Switch to a follower's RecoveryStore and verify all records replicated
        RecoveryStore followerRs = activateStore(follower);
        for (Uid uid : txnUids) {
            AtomicAction probe = new AtomicAction(uid);
            assertTrue(containsAtomicAction(followerRs, probe),
                    "Follower store should contain replicated txn " + uid);
        }
    }

    /**
     * Verifies that the SlotStore index is rebuilt when a Raft role change occurs.
     * <p>
     * When data arrives in a follower's RSM via Raft replication (bypassing
     * {@code SlotStore.write()}), the follower's in-memory index is not updated.
     * A subsequent role change (e.g. follower promoted to leader) sets the
     * {@code indexStale} flag, and the next index-dependent query triggers
     * {@code refreshIndex()} to reconcile the index with the backing store.
     */
    @Test
    void testIndexRefreshOnLeaderChange() throws Throwable {
        String cluster = "raft-idx-" + System.currentTimeMillis();
        List<String> members = List.of("R1", "R2", "R3");

        createAndStartNode("R1", cluster, members);
        createAndStartNode("R2", cluster, members);
        createAndStartNode("R3", cluster, members);
        awaitAllLeaders();

        RaftNode leader = findLeader();
        RaftNode follower = nodes.stream()
                .filter(n -> !n.isLeader()).findFirst().orElseThrow();

        // Activate the follower's store - its SlotStore scans all slots (all empty)
        RecoveryStore followerRs = activateStore(follower);

        // Write a record directly to the leader's RSM. Raft replicates the entry
        // to the follower's RSM via apply(), bypassing the follower's SlotStore,
        // so the follower's in-memory index is not updated.
        Uid txnUid = new Uid();
        OutputBuffer record = new OutputBuffer();
        new SlotStoreKey(txnUid, TYPE_NAME, StateStatus.OS_COMMITTED).packInto(record);
        new OutputObjectState().packInto(record);
        leader.sm.put(0, record.buffer());

        AtomicAction probe = new AtomicAction(txnUid);

        // Isolate the leader - the followers elect a new leader, triggering
        // role change notifications that set indexStale = true
        List<RaftNode> followers = nodes.stream().filter(n -> !n.isLeader()).toList();
        partition(followers, List.of(leader));

        // Wait for the follower's backing slots to detect the role change
        waitFor(30_000, "follower to detect stale index after role change",
                follower.slots::isIndexStale);

        // The next index-dependent query triggers refreshIndexIfStale(),
        // which re-scans all slots and discovers the replicated entry
        assertTrue(containsAtomicAction(followerRs, probe),
                "After role change: refreshed index should contain the replicated entry");
    }
}
