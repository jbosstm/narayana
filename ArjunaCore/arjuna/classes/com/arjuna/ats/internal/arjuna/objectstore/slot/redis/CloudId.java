/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.redis;

import com.arjuna.ats.arjuna.common.Uid;

import java.nio.charset.StandardCharsets;

/*
 * a member of a recovery manager fail-over group
 * the pair <failoverGroupId>:<nodeId> are managed by one Recovery Manager
 */
public class CloudId {
    String nodeId;
    // keys in the same failover group support migrate semantics within the group
    String failoverGroupId;
    Uid uid; // to avoid clashes when moving keys
    String description;
    String id; // The pair <failoverGroupId>:<nodeId>:<generation> must be unique in a give Redis cluster
    String keyPattern;

    public CloudId(String nodeId) {
        this(nodeId, "0");
    }

    public CloudId(String nodeId, String failoverGroupId) {
        this(nodeId, failoverGroupId, null);
    }

    public CloudId(String nodeId, String failoverGroupId, String description) {
        this.failoverGroupId = failoverGroupId;
        this.nodeId = nodeId;
        this.uid = new Uid();
        this.id = String.format("%s:%s:%s", failoverGroupId, nodeId, uid.stringForm());
        this.description = description;
        this.keyPattern = String.format("{%s}:%s:*", failoverGroupId, nodeId); // matches all keys in this failover group
    }

    /**
     * @return a pattern that matches all keys that share the same failoverGroupId and nodeId
     */
    public String allKeysPattern() {
        return keyPattern;
    }

    public byte[] generateUniqueKey(int index) {
        return String.format("{%s}:%s:%s:%d", failoverGroupId, nodeId, new Uid().stringForm(), index)
                .getBytes(StandardCharsets.UTF_8);
    }
}
