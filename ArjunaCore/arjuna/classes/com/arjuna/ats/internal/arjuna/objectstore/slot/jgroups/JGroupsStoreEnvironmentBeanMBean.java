/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBeanMBean;
import org.jgroups.blocks.ReplCache;

/**
 * A JMX MBean interface containing configuration for the JGroupsStore
 */
public interface JGroupsStoreEnvironmentBeanMBean extends SlotStoreEnvironmentBeanMBean {

    /**
     * get the jGroups xml based cache configuration
     * <p>
     * Note that any properties set in this jGroups xml config file will override any related config in this bean
     * including:
     * {@link JGroupsStoreEnvironmentBeanMBean#getCacheName()}
     * {@link JGroupsStoreEnvironmentBeanMBean#getNodeAddress()}
     * {@link JGroupsStoreEnvironmentBeanMBean#getStoreDir()}
     * <p>
     * @return the name of config file
     */
    String getJGroupsConfigFileName();

    void setJGroupsConfigFileName(String jGroupsConfigFileName);

    /**
     * @return the location of the storage for write through caches (only applies to programmatic cache configuration)
     */
    String getStoreDir();

    /**
     * set the location of the storage for write through caches
     * WARNING this setting only applies for programmatic configuration of the cache, ie if
     * {@link JGroupsStoreEnvironmentBean#getJGroupsConfigFileName()} is configured then the cache config
     * will use that file instead
     * @param storeDir the path to the store directory. If storeDir is not an absolute path then it will be
     *                 interpreted as being relative to the User's current working directory
     *                 (as defined by the user.dir system property)
     */
    void setStoreDir(String storeDir);

    void setCache(ReplCache<ByteArrayKey, byte[]> cache);

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
     * The name of the JGroups cluster to connect to.
     * Falls back to {@link #getCacheName()} if not set.
     *
     * @return the cluster name, or null if not explicitly configured
     */
    String getClusterName();

    void setClusterName(String clusterName);

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
    void setBackingSlots(BackingSlots backingSlots);

    /**
     * The classname of the {@link JGroupsSlotKeyGenerator}
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
     * @param jGroupsSlotKeyGenerator the slot key generator
     */
    void setSlotKeyGenerator(JGroupsSlotKeyGenerator jGroupsSlotKeyGenerator);

    /**
     * @return the slot key generator instance
     */
    JGroupsSlotKeyGenerator getSlotKeyGenerator();

    // ===== WAL Configuration Methods =====

    boolean isWalEnabled();
    void setWalEnabled(boolean walEnabled);

    boolean isWalSyncWrites();
    void setWalSyncWrites(boolean walSyncWrites);

    boolean isWalSyncDeletes();
    void setWalSyncDeletes(boolean walSyncDeletes);

    int getWalBufferSize();
    void setWalBufferSize(int walBufferSize);

    int getWalBufferFlushesPerSecond();
    void setWalBufferFlushesPerSecond(int walBufferFlushesPerSecond);

    int getWalFileSize();
    void setWalFileSize(int walFileSize);

    int getWalMinFiles();
    void setWalMinFiles(int walMinFiles);

    int getWalPoolSize();
    void setWalPoolSize(int walPoolSize);

    int getWalCompactMinFiles();
    void setWalCompactMinFiles(int walCompactMinFiles);

    int getWalCompactPercentage();
    void setWalCompactPercentage(int walCompactPercentage);

    String getWalFilePrefix();
    void setWalFilePrefix(String walFilePrefix);

    String getWalFileExtension();
    void setWalFileExtension(String walFileExtension);

    int getWalMaxIO();
    void setWalMaxIO(int walMaxIO);

    boolean isWalAsyncIO();
    void setWalAsyncIO(boolean walAsyncIO);
}
