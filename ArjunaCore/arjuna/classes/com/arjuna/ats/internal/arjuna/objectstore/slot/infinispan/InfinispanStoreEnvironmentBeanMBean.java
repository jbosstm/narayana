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

    /**
     * get the infinispan xml based cache configuration
     * <p>
     * Note that any properties set in this infinispan xml config file will override any related config in this bean
     * including:
     * {@link InfinispanStoreEnvironmentBeanMBean#getCacheName()}
     * {@link InfinispanStoreEnvironmentBeanMBean#getNodeAddress()}
     * {@link InfinispanStoreEnvironmentBeanMBean#getStoreDir()}
     * {@link InfinispanStoreEnvironmentBeanMBean#getGroupName()}
     * <p>
     * @return the name of config file
     */
    String getInfinispanConfigFileName();

    void setInfinispanConfigFileName(String infinispanConfigFileName);

    /**
     * @return the location of the storage for write through caches (only applies to programmatic cache configuration)
     */
    String getStoreDir();

    /**
     * set the location of the storage for write through caches
     * WARNING this setting only applies for programmatic configuration of the cache, ie if
     * {@link InfinispanStoreEnvironmentBean#getInfinispanConfigFileName()} is configured then the cache config
     * will use that file instead
     * @param storeDir the path to the store directory. If storeDir is not an absolute path then it will be
     *                 interpreted as being relative to the User's current working directory
     *                 (as defined by the user.dir system property)
     */
    void setStoreDir(String storeDir);

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
     * Cluster Configuration Considerations
     * <p>
     * 1. A single member must run the recovery manager and a new one started if it fails (aka an HA singleton)
     * 2. Top down recovery via the AtomicActionRecoveryModule will recover all transactions in the store unless the
     *    recovery groupName is set in which case the recovery manager will only recover AtomicActions created
     *    with that groupName.
     * 3. Any member can create and commit transactions ({@link com.arjuna.ats.arjuna.recovery.TransactionStatusManager}
     *    will be used during recovery to decide if the creator is still running)
     * <p>
     * The group name may also be used in Distributed Mode which can provide orders of magnitude improvements in
     * scalability than can the Replication Mode. For example if a large cluster is supporting multi-tenancy
     * (multiple transaction and recovery managers) then Distributed Mode together with key grouping can ensure
     * that cache entries for a particular group are co-located on a group of cluster nodes rather than being
     * scattered over the entire cluster.
     *
     * @return the group name or null if not required
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
