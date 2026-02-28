/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanSlotKeyGenerator;

import java.nio.charset.StandardCharsets;

public class ClusterMemberId implements InfinispanSlotKeyGenerator {
    String nodeId;
    // grouping is the concern of the users and can define the groupId according to their own specific needs
    // eg a redis based store might apply the semantic that keys in the same "failover" group support migrate
    // semantics within the group
    String groupId;
    Uid uid;
    String id;
    String keyPattern;

    public ClusterMemberId() {
        this.uid = new Uid();
        this.groupId = "";
        this.nodeId = "";
    }

    @Override
    public void init(InfinispanStoreEnvironmentBean config) {
        this.groupId = config.getGroupName();
        this.nodeId = config.getNodeAddress();
        this.uid = new Uid();
        this.id = String.format("%s:%s:%s", groupId, nodeId, uid.stringForm());
        this.keyPattern = String.format("{%s}:%s:*", groupId, nodeId); // matches all keys in this group
    }

    /**
     * @return a pattern that matches all keys that share the same failoverGroupId and nodeId
     */
    public String allKeysPattern() {
        return keyPattern;
    }

    public byte[] generateUniqueKey(int index) {
        return String.format("{%s}:%s:%s:%d", groupId, nodeId, uid.stringForm(), index)
                .getBytes(StandardCharsets.UTF_8);
    }
}
