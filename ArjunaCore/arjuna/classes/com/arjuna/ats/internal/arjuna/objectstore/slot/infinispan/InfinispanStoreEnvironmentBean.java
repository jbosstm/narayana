/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.common.internal.util.ClassloadingUtility;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;

import java.io.File;
import java.io.IOException;

/**
 * Configuration properties for an Infinispan backed slot store implementation
 * {@link com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots}
 * <p>
 * NOTE: This is an Experimental feature (Infinispan Slot Store) and is not recommended for production systems and may
 * contain breaking changes in future releases.
 */
public class InfinispanStoreEnvironmentBean extends SlotStoreEnvironmentBean implements InfinispanStoreEnvironmentBeanMBean {

    private String infinispanConfigFileName;
    private Cache<byte[], byte[]> cache;
    private String cacheName;
    private boolean ignoreReturnValues = true;
    private String nodeAddress;
    private String groupName = null;
    private String slotKeyGeneratorClassName;
    private InfinispanSlotKeyGenerator infinispanSlotKeyGenerator;

    /**
     * get the infinispan xml based cache configuration
     * <p>
     * Note that any properties set in this infinispan xml config file will override any related config in this bean
     * including:
     * {@link InfinispanStoreEnvironmentBean#getCacheName()}
     * {@link InfinispanStoreEnvironmentBean#getNodeAddress()}
     * {@link InfinispanStoreEnvironmentBean#getStoreDir()}
     * {@link InfinispanStoreEnvironmentBean#getGroupName()}
     * <p>
     * @return the name of config file
     */
    public String getInfinispanConfigFileName() {
        return infinispanConfigFileName;
    }

    public void setInfinispanConfigFileName(String infinispanConfigFileName) {
        this.infinispanConfigFileName = infinispanConfigFileName;
    }

    public Cache<byte[], byte[]> getCache() {
        if (cache == null) {
            // use infinispan config file
            if (infinispanConfigFileName != null) {
                try {
                    DefaultCacheManager cacheManager = new DefaultCacheManager(
                            InfinispanStoreEnvironmentBean.class.getResourceAsStream(infinispanConfigFileName));
                    if (cacheName == null) {
                        cacheName = cacheManager.getName(); // cache name defaults the cache manager name
                    }
                    nodeAddress = cacheManager.getNodeAddress();

                    cache = cacheManager.getCache(cacheName);
                } catch (IOException e) {
                    tsLogger.i18NLogger.warn_infinispan_config(e);
                    throw new RuntimeException(e);
                }
            }
        }

        return cache;
    }

    public void setCache(Cache<byte[], byte[]> cache) {
        this.cache = cache;

        if (isIgnoreReturnValues()) {
            this.cache = cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
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
     * Get the cache backup location.
     * @return when write through caching is configured return the filesystem location used for the storage
     */
    @Override
    public String getStoreDir() {
        return super.getStoreDir();
    }

    /**
     * set the location of the storage for write through caches
     * WARNING this setting only applies for programmatic configuration of the cache, ie if
     * {@link InfinispanStoreEnvironmentBean#getInfinispanConfigFileName()} is configured then the cache config
     * will use that file instead
     * @param storeDir the path to the store directory. If storeDir is not an absolute path then it will be
     *                 interpreted as being relative to the User's current working directory
     *                 (as defined by the user.dir system property)
     */
    @Override
    public void setStoreDir(String storeDir) {
        if (storeDir != null) {
            if (!storeDir.startsWith(String.valueOf(File.separatorChar))) {
                storeDir = System.getProperty("user.dir") + "/" + storeDir;
                super.setStoreDir(storeDir);
            }

            super.setStoreDir(storeDir);
        }
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
     * classname of the generator function for internal slot keys. If the classname is unset and
     * {@link InfinispanStoreEnvironmentBean#setSlotKeyGenerator(InfinispanSlotKeyGenerator)} has not been called
     * then a generator based on {@link com.arjuna.ats.arjuna.common.Uid} will be used
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
                if(infinispanSlotKeyGenerator == null && (slotKeyGeneratorClassName != null && !slotKeyGeneratorClassName.isBlank())) {
                    infinispanSlotKeyGenerator = ClassloadingUtility.loadAndInstantiateClass(InfinispanSlotKeyGenerator.class, slotKeyGeneratorClassName, null);
                }
            }
        }

        return infinispanSlotKeyGenerator;
    }
}
