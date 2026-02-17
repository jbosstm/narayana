/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.infinspan;

import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBeanMBean;
import org.infinispan.Cache;
import org.infinispan.context.Flag;

/**
 * A JMX MBean interface containing configuration for the InfinispanStore
 */
public interface InfinispanStoreEnvironmentBeanMBean extends SlotStoreEnvironmentBeanMBean {

    void setCache(Cache<byte[], byte[]> cache);

    /**
     * If the return value of write operations will be ignored.
     * The default is true to avoid needless remote calls
     *
     * @return true if return values are ignored
     */
    boolean isIgnoreReturnValues();

    void setIgnoreReturnValues(boolean ignoreReturnValues);

    /**
     * The address of the node within a cluster.
     * Used in clustered and embedded scenarios to identify which member of the JGroups cluster
     * the JVM is currently representing.
     *
     * @return the node address
     */
    String getNodeAddress();

    void setNodeAddress(String nodeAddress);

    /**
     * The name of the cache used to store the key/value pairs for this slot store
     *
     * @return the name of the replicated cache
     */
    String getCacheName();

    void setCacheName(String cacheName);

    /**
     * Extra metadata embedded to be within an id
     * @return metadata
     */
    String getFailoverId();

    void setFailoverId(String failoverId);

    /**
     * {@link SlotStoreEnvironmentBeanMBean#getNumberOfSlots()}
     */
    public int getNumberOfSlots();

    /**
     * {@link SlotStoreEnvironmentBeanMBean#setNumberOfSlots(int)}
     */
    public void setNumberOfSlots(int numberOfSlots);

    /**
     * {@link SlotStoreEnvironmentBeanMBean#getBytesPerSlot()}
     */
    public int getBytesPerSlot();

    /**
     * {@link SlotStoreEnvironmentBeanMBean#setBytesPerSlot(int)} }
     */
    public void setBytesPerSlot(int bytesPerSlot);

    /**
     * {@link SlotStoreEnvironmentBeanMBean#setBackingSlots(BackingSlots)} }
     */
    public void setBackingSlots(InfinispanSlots infinispanSlots);
}
