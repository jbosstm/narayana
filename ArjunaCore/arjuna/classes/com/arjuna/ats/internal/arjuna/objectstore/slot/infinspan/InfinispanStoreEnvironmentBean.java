/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.infinspan;

import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import org.infinispan.Cache;
import org.infinispan.context.Flag;

/**
 * Basic set of config properties for tuning the store.
 * Properties that relate to  administrative requirements and performance tuning of the store will follow.
 */
public class InfinispanStoreEnvironmentBean extends SlotStoreEnvironmentBean implements InfinispanStoreEnvironmentBeanMBean {

    private Cache<byte[], byte[]> cache;
    private String cacheName;
    private boolean ignoreReturnValues = true;
    private String nodeAddress;
    private String failoverId = "0";

    public Cache<byte[], byte[]> getCache() {
        return cache;
    }

    public void setCache(Cache<byte[], byte[]> cache) {
        this.cache = cache;

        if (isIgnoreReturnValues()) {
            cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
        }
    }

    /**
     * If the return value of write operations will be ignored.
     * The default is true to avoid needless remote calls
     *
     * @return true if return values are ignored
     */
    public boolean isIgnoreReturnValues() {
        return ignoreReturnValues;
    }

    public void setIgnoreReturnValues(boolean ignoreReturnValues) {
        this.ignoreReturnValues = ignoreReturnValues;
    }

    /**
     * The address of the node within a cluster.
     * Used in clustered and embedded scenarios to identify which member of the JGroups cluster
     * the JVM is currently representing.
     *
     * @return the node address
     */
    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    /**
     * the name of the cache used to store the key/value pairs for this slot store
     *
     * @return the name of the replicated cache
     */
    public String getCacheName() {
        return cache != null ? cache.getName() : cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * extra metadata embedded to be within an id
     *
     * @return metadata
     */
    public String getFailoverId() {
        return failoverId;
    }

    public void setFailoverId(String failoverId) {
        this.failoverId = failoverId;
    }

    @Override
    public void setBackingSlots(InfinispanSlots infinispanSlots) {
        super.setBackingSlots(infinispanSlots);
    }
}
