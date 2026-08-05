/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBeanMBean;
import org.jgroups.JChannel;
import org.jgroups.raft.blocks.ReplicatedStateMachine;

/**
 * MBean interface for JGroupsRaftStoreEnvironmentBean.
 * Declares Raft-specific configuration and the subset of JGroups settings
 * that the Raft store actually uses.
 */
public interface JGroupsRaftStoreEnvironmentBeanMBean extends SlotStoreEnvironmentBeanMBean {

    String getJGroupsConfigFileName();
    void setJGroupsConfigFileName(String jGroupsConfigFileName);

    String getNodeAddress();
    void setNodeAddress(String nodeAddress);

    String getClusterName();
    void setClusterName(String clusterName);

    String getCacheName();
    void setCacheName(String cacheName);

    boolean isRaftLogFsync();
    void setRaftLogFsync(boolean raftLogFsync);

    String getRaftMembers();
    void setRaftMembers(String raftMembers);

    int getRaftTimeout();
    void setRaftTimeout(int raftTimeout);

    int getRaftElectionMaxInterval();
    void setRaftElectionMaxInterval(int raftElectionMaxInterval);

    boolean isAllowDirtyReads();
    void setAllowDirtyReads(boolean allowDirtyReads);

    JChannel getPreConfiguredChannel();
    void setPreConfiguredChannel(JChannel channel);

    ReplicatedStateMachine<Integer, byte[]> getPreConfiguredStateMachine();
    void setPreConfiguredStateMachine(ReplicatedStateMachine<Integer, byte[]> stateMachine);
}
