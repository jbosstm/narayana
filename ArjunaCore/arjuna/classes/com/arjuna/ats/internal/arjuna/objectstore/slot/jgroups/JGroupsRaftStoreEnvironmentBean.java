/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

import org.jgroups.JChannel;
import org.jgroups.raft.blocks.ReplicatedStateMachine;

/**
 * Extended environment bean for JGroups Raft-based stores.
 * Allows pre-configuration of JChannel and ReplicatedStateMachine for testing scenarios
 * where multiple Raft nodes need to be started in parallel before RecoveryStore creation.
 */
public class JGroupsRaftStoreEnvironmentBean extends JGroupsStoreEnvironmentBean {
    private JChannel preConfiguredChannel;
    private ReplicatedStateMachine<Integer, byte[]> preConfiguredStateMachine;

    private boolean raftLogFsync = true;
    private String raftMembers = null;
    private int raftTimeout = 5000; // milliseconds
    private int raftElectionMaxInterval = 500; // milliseconds
    private boolean allowDirtyReads = false;

    public JGroupsRaftStoreEnvironmentBean() {
        setJGroupsConfigFileName("jgroups-raft-config.xml");
    }

    /**
     * Enable fsync for Raft log writes. When true, all writes are forced to disk
     * before returning (provides crash recovery). When false, writes are buffered
     * (faster but not crash-safe).
     *
     * @return true if fsync is enabled
     */
    public boolean isRaftLogFsync() {
        return raftLogFsync;
    }

    public void setRaftLogFsync(boolean raftLogFsync) {
        this.raftLogFsync = raftLogFsync;
    }

    /**
     * Optional static membership list for Raft cluster (e.g., "node1,node2,node3").
     * When null or empty, the node auto-discovers an existing cluster and joins it
     * via the REDIRECT protocol. If no existing cluster is found, the node bootstraps
     * as a single-member cluster. On restart, membership is restored from the persistent
     * Raft log regardless of this setting.
     *
     * @return comma-separated list of member names, or null if not configured
     */
    public String getRaftMembers() {
        return raftMembers;
    }

    public void setRaftMembers(String raftMembers) {
        this.raftMembers = raftMembers;
    }

    /**
     * Timeout in milliseconds for Raft operations (default: 5000).
     * Used as the timeout to wait for a majority of group members to ack a write operation
     *
     * @return timeout in milliseconds
     */
    public int getRaftTimeout() {
        return raftTimeout;
    }

    public void setRaftTimeout(int raftTimeout) {
        this.raftTimeout = raftTimeout;
    }

    /**
     * Maximum election timeout interval in milliseconds (default: 500).
     * This value can also be defined in jgroups-raft-config.xml config file
     * @see <a href="https://belaban.github.io/jgroups-raft/manual/index.html#_configuration">jgroups raft config file</a>
     *
     * @return max election interval in milliseconds
     */
    public int getRaftElectionMaxInterval() {
        return raftElectionMaxInterval;
    }

    public void setRaftElectionMaxInterval(int raftElectionMaxInterval) {
        this.raftElectionMaxInterval = raftElectionMaxInterval;
    }

    /**
     * When true, reads are served from the node's local in-memory state machine
     * without going through Raft consensus. This is faster but may return stale
     * data on follower nodes that have not yet received the latest log entries.
     * When false, reads go through the Raft leader for consistent results at the
     * cost of higher latency.
     * <p>
     * When the store is used as a transaction log use the value false (the default)
     * so that recovery reads for in-doubt transactions are forwarded to the Raft
     * leader and a complete, up-to-date view is returned.
     *
     * @return true if dirty reads are allowed
     */
    public boolean isAllowDirtyReads() {
        return allowDirtyReads;
    }

    public void setAllowDirtyReads(boolean allowDirtyReads) {
        this.allowDirtyReads = allowDirtyReads;
    }

    /**
     * Get the pre-configured JChannel, if any.
     * <p>
     * This is useful for testing because Raft store initialisation must wait for leader election before allowing reads
     * but the RecoveryStore is a singleton so a second store could never be started. By pre-configuring both the channel
     * and the state machine, cluster formation can happen before the RecoveryStore is started.
     * See {@code JGroupsRaftClusterTest} for an example.
     *
     * @return the pre-configured channel, or null if not set
     */
    public JChannel getPreConfiguredChannel() {
        return preConfiguredChannel;
    }

    /**
     * Set a pre-configured JChannel. If set, JGroupsRaftSlots will use this channel
     * instead of creating a new one during init().
     *
     * @param channel the pre-configured channel
     */
    public void setPreConfiguredChannel(JChannel channel) {
        this.preConfiguredChannel = channel;
    }

    /**
     * Get the pre-configured ReplicatedStateMachine, if any.
     * <p>
     * This is useful for testing because Raft store initialisation must wait for leader election before allowing reads
     * but the RecoveryStore is a singleton so a second store could never be started. By pre-configuring both the channel
     * and the state machine, cluster formation can happen before the RecoveryStore is started.
     *
     * @return the pre-configured state machine, or null if not set
     */
    public ReplicatedStateMachine<Integer, byte[]> getPreConfiguredStateMachine() {
        return preConfiguredStateMachine;
    }

    /**
     * Set a pre-configured ReplicatedStateMachine. If set, JGroupsRaftSlots will use
     * this state machine instead of creating a new one during init().
     *
     * @param stateMachine the pre-configured state machine
     */
    public void setPreConfiguredStateMachine(ReplicatedStateMachine<Integer, byte[]> stateMachine) {
        this.preConfiguredStateMachine = stateMachine;
    }
}
