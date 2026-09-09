/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.common;

import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;

/**
 * A JMX MBean interface containing configuration for the objectstore and various implementations thereof.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface ObjectStoreEnvironmentBeanMBean
{
    /**
     * Returns the maximum allowed size, in bytes, of the cache store's in-memory cache.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * Default: 10240 bytes
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.objectstore.cacheStore.size
     *
     * @return the memory cache size in bytes.
     */
    int getCacheStoreSize();

    /**
     * Returns true if writes to the objectstore should include a disk sync. Unlikely to be worthwile
     * since the store caches state in memory anyhow.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.objectstore.cacheStore.sync
     *
     * @return true if writes should be synched to disk, false otherwise.
     */
    boolean isCacheStoreSync();

    /**
     * Returns the maximum number of removed items that may be held in the cache before being purged.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * Default: 256
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.objectstore.cacheStore.removedItems
     *
     * @return the maximum number of removed items in the cache.
     */
    int getCacheStoreRemovedItems();

    /**
     * Returns the interval on which the cache will wake and process outstanding work.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * Default: 120000 milliseconds
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.objectstore.cacheStore.scanPeriod
     *
     * @return the work interval of the cache, in milliseconds.
     */
    int getCacheStoreScanPeriod();

    /**
     * Returns the maximum number of outstanding writes that may be held in the cache.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * Default: 100
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.objectstore.cacheStore.workItems
     *
     * @return the maximum number of outstanding writes in the cache.
     */
    int getCacheStoreWorkItems();

    /**
     * Returns the number of hash buckets used for the cache work queue.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * Default: 128
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.objectstore.cacheStore.hash
     *
     * @return the number of hash buckets used to store the cache state.
     */
    int getCacheStoreHash();

    /**
     * Returns the local ObjectStore root directory name. This should be a path element, not a complete path.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore, TwoPhaseVolatileStore, VolatileStore.
     *
     * Default: "defaultStore"
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.localOSRoot
     *
     * @return the local ObjectStore root directory name.
     */
    String getLocalOSRoot();

    /**
     * Returns the ObjectStore directory path.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore, TwoPhaseVolatileStore, VolatileStore.
     *
     * Default: {user.dir}/ObjectStore
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.objectStoreDir
     *
     * @return the ObjectStore directory path.
     */
    String getObjectStoreDir();

    /**
     * Returns true if ObjectStore operations should be synched to disk.
     * Note that this value may be overridden by store implementation specific configuration.
     * See also: isTransactionSync
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.objectStoreSync
     *
     * @return true for synched operations, false otherwise.
     */
    boolean isObjectStoreSync();

    /**
     * Returns the fully qualified class name for the ObjectStore implementation.
     *
     * Default: "com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore"
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.objectStoreType
     *
     * @return the fully qualified class name of the ObjectStore implementation.
     */
    String getObjectStoreType();

    /**
     * Returns the number of directories over which the ObjectStore contents will be distributed.
     * Splitting the contents is important for performance on some file systems, as it reduces
     * chain length (number of items in a directory) and directory lock contention.
     *
     * This property is used by the following object store implementations: CacheStore, HashedActionStore, HashedStore.
     *
     * Default: 255
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.hashedDirectories
     *
     * @return the number of directories over which to distribute the store.
     */
    int getHashedDirectories();

    /**
     * Returns true if transaction log operations should be synched to disk.
     *
     * This property is used by the following object store implementations: ActionStore, HashedActionStore, LogStore.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.transactionSync
     *
     * @return true if operations should be forcedto disk, false otherwise.
     */
    boolean isTransactionSync();

    /**
     * Returns the share mode for the ObjectStore, i.e., is this being shared
     * between VMs?
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore, TwoPhaseVolatileStore, VolatileStore.
     *
     * Default: ObjectStore.OS_UNKNOWN
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.share
     *
     * @return the default share mode.
     */
    int getShare();

    /**
     * Returns the maximum number of attempts which may be made to create a file path in the store.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore.
     *
     * Default: 100
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.hierarchyRetry
     *
     * @return the maximum number of attempts to create a nested directory tree.
     */
    int getHierarchyRetry();

    /**
     * Returns the time in milliseconds to wait between file creation retries.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore.
     *
     * Default: 100 milliseconds.
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.hierarchyTimeout
     *
     * @return the time to wait before retrying a failed file creation, in milliseconds.
     */
    int getHierarchyTimeout();

    /**
     * Returns true if the LogStore should write removal records synchronously.
     * Disabling this may increase performance at the cost of recovery complexity.
     *
     * This property is used by the following object store implementations: LogStore.
     *
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.transactionLog.synchronousRemoval
     *
     * @return true for synchronous removals, false for buffered (asynchronous) operation.
     */
    boolean isSynchronousRemoval();

    /**
     * Returns the default size of the LogStore file, in bytes.
     *
     * This property is used by the following object store implementations: LogStore.
     *
     * Default: 10MB
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.transactionLog.txLogSize
     *
     * @return the default file size for the LogStore, in bytes.
     */
    long getTxLogSize();

    /**
     * Returns the purge interval for the LogStore, in milliseconds.
     *
     * This property is used by the following object store implementations: LogStore.
     *
     * Default: 100000 milliseconds
     * Equivalent deprecated property: com.arjuna.ats.arjuna.coordinator.transactionLog.purgeTime
     *
     * @return the purge interval in milliseconds.
     */
    long getPurgeTime();
    
	/**
	 * Get the JDBCAccess details.
	 */
	public String getJdbcAccess();

	/**
	 * Sets the instance of JDBCAccess
	 * 
	 * @param connectionDetails
	 *            an Object that provides JDBCAccess, or null.
	 */
	public void setJdbcAccess(String connectionDetails);

	/**
	 * Get the table prefix
	 * 
	 * @return The prefix to apply to the table
	 */
	public String getTablePrefix();

	/**
	 * Set the table prefix
	 * 
	 * @param tablePrefix
	 *            A prefix to use on the tables
	 */
	public void setTablePrefix(String tablePrefix);

	/**
	 * Should the store drop the table
	 * 
	 * @return Whether to drop the table
	 */
	public boolean getDropTable();

	/**
	 * Set whether to drop the table.
	 * 
	 * @param dropTable
	 *            Drop the table
	 */
	public void setDropTable(boolean dropTable);

}