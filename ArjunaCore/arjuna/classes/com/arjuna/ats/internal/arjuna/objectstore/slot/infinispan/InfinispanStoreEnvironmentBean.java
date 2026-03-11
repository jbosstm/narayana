/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan;

import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.common.internal.util.ClassloadingUtility;
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
    private String groupName = null;
    private String slotKeyGeneratorClassName;
    private InfinispanSlotKeyGenerator infinispanSlotKeyGenerator;

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
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public void setBackingSlots(InfinispanSlots infinispanSlots) {
        super.setBackingSlots(infinispanSlots);
    }

    /**
     * classname of the generator function for internal slot keys if the classname is unset and
     * {@link InfinispanStoreEnvironmentBean#setSlotKeyGenerator(InfinispanSlotKeyGenerator)} has not been called
     * then a {@link com.arjuna.ats.arjuna.common.Uid} will be used
     */
    public void setSlotKeyGeneratorClassName(String slotKeyGeneratorClassName) {
        this.slotKeyGeneratorClassName = slotKeyGeneratorClassName;
    }

    public String getSlotKeyGeneratorClassName() {
        return slotKeyGeneratorClassName;
    }

    /**
     * Define a strategy for initialising slot store keys. If unset then a
     * {@link com.arjuna.ats.arjuna.common.Uid} will be used
     * <p>
     * @param infinispanSlotKeyGenerator the slot key generator
     */
    public void setSlotKeyGenerator(InfinispanSlotKeyGenerator infinispanSlotKeyGenerator) {
        this.infinispanSlotKeyGenerator = infinispanSlotKeyGenerator;
    }

    public InfinispanSlotKeyGenerator getSlotKeyGenerator() {
        if(infinispanSlotKeyGenerator == null && slotKeyGeneratorClassName != null)
        {
            synchronized (this) {
                if(infinispanSlotKeyGenerator == null && slotKeyGeneratorClassName != null) {
                    infinispanSlotKeyGenerator = ClassloadingUtility.loadAndInstantiateClass(InfinispanSlotKeyGenerator.class, slotKeyGeneratorClassName, null);
                }
            }
        }

        return infinispanSlotKeyGenerator;
    }
}
