/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.ByteArrayKey;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsSlotKeyGenerator;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsStoreEnvironmentBean;

import java.nio.charset.StandardCharsets;

/**
 * A deterministic key generator for testing where all cluster nodes share the same slot keys.
 * <p>
 * This allows writes from any node to be visible to all other nodes for the same slot index.
 */
public class SharedSlotKeyGenerator implements JGroupsSlotKeyGenerator {
    private String groupId;

    @Override
    public void init(JGroupsStoreEnvironmentBean config) {
        this.groupId = config.getClusterName();
        if (this.groupId == null || this.groupId.isEmpty()) {
            this.groupId = "default-group";
        }
    }

    @Override
    public ByteArrayKey generateUniqueKey(int index) {
        // Create deterministic key: {clusterName};slot;{index}
        try {
            String key = String.format("{%s};slot;%d", groupId, index);
            return new ByteArrayKey(key.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
