/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan;

import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBeanMBean;
import org.infinispan.Cache;

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
    String getGroupName();

    void setGroupName(String groupName);

    /**
     * {@link SlotStoreEnvironmentBeanMBean#getNumberOfSlots()}
     */
    int getNumberOfSlots();

    /**
     * {@link SlotStoreEnvironmentBeanMBean#setNumberOfSlots(int)}
     */
    void setNumberOfSlots(int numberOfSlots);

    /**
     * {@link SlotStoreEnvironmentBeanMBean#getBytesPerSlot()}
     */
    int getBytesPerSlot();

    /**
     * {@link SlotStoreEnvironmentBeanMBean#setBytesPerSlot(int)} }
     */
    void setBytesPerSlot(int bytesPerSlot);

    /**
     * {@link SlotStoreEnvironmentBeanMBean#setBackingSlots(BackingSlots)} }
     */
    void setBackingSlots(InfinispanSlots infinispanSlots);

    /**
     * The classname of the {@link InfinispanSlotKeyGenerator}
     * @param slotKeyGeneratorClassName the classname of the class that should be used to generate unique slot keys
     */
    void setSlotKeyGeneratorClassName(String slotKeyGeneratorClassName);

    /**
     *
     * @return the classname of the class that should be used to generate unique slot keys
     */
    String getSlotKeyGeneratorClassName();

    /**
     * set the instance that should be used to generate unique slot keys
     * @param infinispanSlotKeyGenerator the slot key generator
     */
    void setSlotKeyGenerator(InfinispanSlotKeyGenerator infinispanSlotKeyGenerator);

    /**
     * @return the slot key generator instance
     */
    InfinispanSlotKeyGenerator getSlotKeyGenerator();
}
