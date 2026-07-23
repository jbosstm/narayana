/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

import org.jgroups.JChannel;
import org.jgroups.raft.blocks.ReplicatedStateMachine;

/**
 * MBean interface for JGroupsRaftStoreEnvironmentBean.
 * Extends the base JGroups store MBean with Raft-specific pre-configuration methods.
 *
 * <p>The pre-configured channel and state machine properties are primarily intended
 * for testing scenarios where multiple Raft nodes need to be started in parallel
 * before RecoveryStore creation. See {@link com.hp.mwtests.ts.arjuna.objectstore.jgroups.JGroupsRaftClusterTest}
 * for usage examples.
 *
 * @since 5.13.2
 */
public interface JGroupsRaftStoreEnvironmentBeanMBean extends JGroupsStoreEnvironmentBeanMBean {

    /**
     * Get the pre-configured JChannel, if any.
     * <p>
     * This is useful for testing because Raft store initialisation must wait for leader election before allowing reads
     * but the RecoveryStore is a singleton so a second store could never be started. By pre-configuring the channel
     * "cluster formation" can happen before "RecoveryStore creation"
     *
     * @return the pre-configured channel, or null if not set
     */
    JChannel getPreConfiguredChannel();

    /**
     * Set a pre-configured JChannel. If set, JGroupsRaftSlots will use this channel
     * instead of creating a new one during init().
     *
     * @param channel the pre-configured channel
     */
    void setPreConfiguredChannel(JChannel channel);

    /**
     * Get the pre-configured ReplicatedStateMachine, if any.
     * <p>
     * This is useful for testing because Raft store initialisation must wait for leader election before allowing reads
     * but the RecoveryStore is a singleton so a second store could never be started. By pre-configuring the channel
     * "cluster formation" can happen before "RecoveryStore creation"
     *
     * @return the pre-configured state machine, or null if not set
     */
    ReplicatedStateMachine<Integer, byte[]> getPreConfiguredStateMachine();

    /**
     * Set a pre-configured ReplicatedStateMachine. If set, JGroupsRaftSlots will use
     * this state machine instead of creating a new one during init().
     *
     * @param stateMachine the pre-configured state machine
     */
    void setPreConfiguredStateMachine(ReplicatedStateMachine<Integer, byte[]> stateMachine);

    boolean isAllowDirtyReads();

    void setAllowDirtyReads(boolean allowDirtyReads);
}
