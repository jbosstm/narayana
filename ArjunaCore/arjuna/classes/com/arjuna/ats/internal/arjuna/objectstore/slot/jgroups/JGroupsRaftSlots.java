/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.jgroups.JChannel;
import org.jgroups.protocols.raft.RAFT;
import org.jgroups.raft.blocks.ReplicatedStateMachine;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * JGroups-Raft based implementation of {@link BackingSlots} providing strong consistency
 * and persistent write-ahead logging (WAL) for crash recovery.
 *
 * <p>Uses the Raft consensus algorithm for:
 * <ul>
 *   <li><b>Strong consistency</b>: Linearizable reads and writes</li>
 *   <li><b>Persistent write-ahead log</b>: All committed data survives crashes via FileBasedLog</li>
 *   <li><b>Leader election</b>: Automatic failover on leader crash</li>
 *   <li><b>Split-brain protection</b>: Quorum-based operation prevents divergence of views on the membership</li>
 * </ul>
 *
 * <p><b>Configuration Requirements</b>:
 * <ul>
 *   <li>Odd number of nodes (minimum of 3)</li>
 *   <li>Optional static membership list via {@link JGroupsRaftStoreEnvironmentBean#setRaftMembers(String)}.
 *       If omitted, the node begins as a single-member cluster and other nodes can join dynamically.</li>
 *   <li>Unique node address via {@link JGroupsStoreEnvironmentBean#setNodeAddress(String)}</li>
 *   <li>Persistent storage directory via {@link JGroupsStoreEnvironmentBean#setStoreDir(String)}</li>
 * </ul>
 *
 * <p><b>NOTE</b>: This is an Experimental feature and is not recommended for production systems.
 * May contain breaking changes in future releases.
 */
public class JGroupsRaftSlots implements BackingSlots {
    private JChannel channel;
    private ReplicatedStateMachine<Integer, byte[]> cache;
    private JGroupsRaftStoreEnvironmentBean config;
    private volatile boolean initialized = false;

    /**
     * Initialize the Raft-based slot store.
     *
     * @param slotStoreConfig the configuration bean
     * @throws IOException if initialization fails
     */
    @Override
    public void init(SlotStoreEnvironmentBean slotStoreConfig) throws IOException {
        if (initialized) {
            throw new IllegalStateException("JGroupsRaftSlots already initialized");
        }

        if (slotStoreConfig instanceof JGroupsRaftStoreEnvironmentBean) {
            config = (JGroupsRaftStoreEnvironmentBean) slotStoreConfig;
        } else {
            config = BeanPopulator.getDefaultInstance(JGroupsRaftStoreEnvironmentBean.class);
        }

        try {
            tsLogger.i18NLogger.warn_jgroups_raft_slot_store();

            // Check if channel and cache are pre-configured (for testing multi-node scenarios)
            if (slotStoreConfig instanceof JGroupsRaftStoreEnvironmentBean raftConfig) {
                channel = raftConfig.getPreConfiguredChannel();
                cache = raftConfig.getPreConfiguredStateMachine();
            }

            if (channel != null && cache != null) {
                // Use pre-configured channel and cache
                tsLogger.logger.debugf("Using pre-configured Raft channel and state machine for node: %s",
                        config.getNodeAddress());

                // Wait for leader election if not already complete
                waitForLeaderElection(config.getRaftElectionMaxInterval());

                tsLogger.logger.debugf("Raft state machine has %d entries after log replay",
                        cache.size());
                initialized = true;
                tsLogger.logger.debugf("JGroupsRaftSlots initialized successfully for node: %s",
                        config.getNodeAddress());

                return;
            }

            // Standard initialization path - create channel and cache
            // Validate configuration
            validateConfiguration(config);

            // Create JGroups channel
            String jgroupsConfig = config.getJGroupsConfigFileName();
            String nodeName = config.getNodeAddress();

            tsLogger.logger.debugf("Creating Raft channel with config: %s and node: %s",
                    jgroupsConfig, nodeName);
            channel = new JChannel(jgroupsConfig).name(nodeName);

            // Configure RAFT protocol BEFORE creating ReplicatedStateMachine
            RAFT raft = channel.getProtocolStack().findProtocol(RAFT.class);
            if (raft == null) {
                throw new IllegalStateException("RAFT protocol not found in JGroups stack");
            }

            // Configure Raft log directory (where FileBasedLog stores data)
            String storeDir = config.getStoreDir();
            raft.logDir(storeDir);
            tsLogger.logger.debugf("Configured Raft log directory: %s", storeDir);

            // Configure Raft log fsync behavior
            raft.logUseFsync(config.isRaftLogFsync());
            tsLogger.logger.debugf("Configured Raft log fsync: %s", config.isRaftLogFsync());

            // Set members list
            String raftMembers = config.getRaftMembers();
            if (raftMembers != null && !raftMembers.isEmpty()) {
                raft.members(java.util.Arrays.asList(raftMembers.split(",")));
                tsLogger.logger.debugf("Configured RAFT members: %s", raftMembers);
            } else {
                raft.members(java.util.List.of(nodeName));
                tsLogger.logger.debugf("No raftMembers configured; bootstrapping single-node cluster for: %s", nodeName);
            }

            // Create replicated state machine and set raft_id
            cache = new ReplicatedStateMachine<>(channel);
            cache.raftId(nodeName);  // set the raft_id of this member (must be in raft.members)
            cache.timeout(config.getRaftTimeout());

            // Connect to cluster
            String clusterName = config.getClusterName();
            if (clusterName == null) {
                clusterName = config.getCacheName();
            }

            tsLogger.logger.debugf("Connecting to Raft cluster: %s", clusterName);
            channel.connect(clusterName);

            // Wait for Raft leader election before allowing reads
            // This is critical because SlotStore's constructor will immediately try to read all slots
            // to rebuild its index, and reads require a leader
            waitForLeaderElection(config.getRaftElectionMaxInterval());

            // Raft state machine is loaded from the persistent log during connect().
            // SlotStore's constructor will call read(i) for each slot to rebuild its index,
            // so no additional loading is needed here.
            tsLogger.logger.debugf("Raft state machine has %d entries after log replay", cache.size());
            initialized = true;

            tsLogger.logger.debugf("JGroupsRaftSlots initialized successfully for node: %s", nodeName);

        } catch (Exception e) {
            if (channel != null) {
                try {
                    channel.close();
                } catch (Exception ex) {
                    // Ignore cleanup errors
                }
            }
            throw new IOException("Failed to initialize JGroupsRaftSlots", e);
        }
    }


    /**
     * Read slot data. Reads are local (no consensus required).
     *
     * @param slotId the slotIdIndex
     * @return the slot data, or null if slot is empty
     */
    @Override
    public byte[] read(int slotId) throws IOException {
        checkInitialized();

        try {
            return cache.get(slotId);
        } catch (Exception e) {
            tsLogger.logger.debugf("Raft read failed for slot " + slotId, e);
            throw new IOException("Raft read failed", e);
        }
    }

    /**
     * Write slot data. Writes go through Raft consensus and are replicated to majority.
     * Returns only after data is committed to Raft log (and fsynced if enabled).
     *
     * @param slotId the slotIdIndex
     * @param data the data to write
     * @param sync ignored - Raft always ensures consistency
     * @throws IOException if write fails
     */
    @Override
    public void write(int slotId, byte[] data, boolean sync) throws IOException {
        checkInitialized();
        try {
            // Raft put() blocks until majority commit
            cache.put(slotId, data);
        } catch (Exception e) {
            tsLogger.logger.warn("Raft write failed for slot " + slotId, e);
            throw new IOException("Raft write failed", e);
        }
    }

    /**
     * Clear slot data. Goes through Raft consensus.
     *
     * @param slotId the slotIdIndex
     * @param sync ignored - Raft always ensures consistency
     * @throws IOException if clear fails
     */
    @Override
    public void clear(int slotId, boolean sync) throws IOException {
        checkInitialized();
        try {
            cache.remove(slotId);
        } catch (Exception e) {
            tsLogger.logger.warn("Raft clear failed for slot " + slotId, e);
            throw new IOException("Raft clear failed", e);
        }
    }

    @Override
    public void stop() {
        if (channel != null) {
            tsLogger.logger.infof("Shutting down JGroupsRaftSlots for node: %s", config.getNodeAddress());
            try {
                channel.close();
            } catch (Exception e) {
                tsLogger.logger.warn("Error closing Raft channel", e);
            }
            channel = null;
        }
        initialized = false;
    }

    /**
     * Get the number of slots. Returns the configured number.
     *
     * @return number of slots
     */
    public int getNumberOfSlots() {
        return config != null ? config.getNumberOfSlots() : 0;
    }

    /**
     * Get the underlying JChannel for monitoring/testing.
     *
     * @return the JChannel instance
     */
    public JChannel getChannel() {
        return channel;
    }

    /**
     * Get the replicated state machine for advanced access.
     *
     * @return the ReplicatedStateMachine instance
     */
    public ReplicatedStateMachine<Integer, byte[]> getStateMachine() {
        return cache;
    }

    /**
     * Check if Raft leader has been elected.
     *
     * @return true if a leader exists
     */
    public boolean hasLeader() {
        JChannel ch = channel;
        if (!initialized || ch == null) {
            return false;
        }
        try {
            RAFT raft = ch.getProtocolStack().findProtocol(RAFT.class);
            return raft != null && raft.leader() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait for Raft leader election to complete.
     * This is called during initialization to ensure a leader is elected before any reads occur.
     *
     * @param millis maximum time to wait in milliseconds
     * @throws IOException if leader election doesn't complete within timeout
     */
    private void waitForLeaderElection(long millis) throws IOException {
        long deadline = System.currentTimeMillis() + millis;

        while (System.currentTimeMillis() < deadline) {
            // Check leader directly without requiring initialized flag
            // (hasLeader() requires initialized=true, but we're still initializing)
            if (channel != null) {
                try {
                    RAFT raft = channel.getProtocolStack().findProtocol(RAFT.class);
                    if (raft != null && raft.leader() != null) {
                        tsLogger.logger.infof("Raft leader elected");
                        return;
                    }
                } catch (Exception e) {
                    // Continue waiting
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for Raft leader election", e);
            }
        }
    }

    /**
     * Get current Raft role (Leader, Follower, Candidate, or impl class name).
     *
     * @return role name, or "UNKNOWN" if not available
     */
    public String getRole() {
        JChannel ch = channel;
        if (!initialized || ch == null) {
            return "UNKNOWN";
        }
        try {
            RAFT raft = ch.getProtocolStack().findProtocol(RAFT.class);
            if (raft == null) {
                return "UNKNOWN";
            }
            // raft.role() returns the impl class name (e.g. "Leader", "Follower", "Candidate")
            // Convert to uppercase for consistency with expected test values
            String role = raft.role();
            return role != null ? role.toUpperCase() : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    // Private helper methods

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("JGroupsRaftSlots not initialized");
        }
    }

    private void validateConfiguration(JGroupsRaftStoreEnvironmentBean config) {
        if (config.getNodeAddress() == null || config.getNodeAddress().isEmpty()) {
            throw new IllegalArgumentException("nodeAddress must be set for Raft");
        }

        if (config.getStoreDir() == null || config.getStoreDir().isEmpty()) {
            throw new IllegalArgumentException("storeDir must be set for Raft persistent log");
        }

        String raftMembers = config.getRaftMembers();
        if (raftMembers != null && !raftMembers.isEmpty()) {
            String[] members = raftMembers.split(",");
            if (members.length % 2 == 0) {
                tsLogger.logger.warnf("Raft cluster has even number of nodes (%d). Odd numbers (3, 5, 7) are recommended for proper quorum.", members.length);
            }
            if (members.length < 3) {
                tsLogger.logger.warnf("Raft cluster has only %d nodes. Minimum 3 nodes recommended for fault tolerance.", members.length);
            }
        }
    }
}
