/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.ats.arjuna.common;

import java.io.File;
import java.lang.reflect.Method;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.internal.arjuna.objectstore.HashedStore;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

/**
 * A JavaBean containing configuration properties for the objectstore and various implementations thereof.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.objectstore.")
public class ObjectStoreEnvironmentBean implements ObjectStoreEnvironmentBeanMBean
{
    private volatile String localOSRoot = "defaultStore";
    private volatile String objectStoreDir = System.getProperty("user.dir") + File.separator + "ObjectStore";
    private volatile boolean objectStoreSync = true;
    private volatile String objectStoreType = ShadowNoFileLockStore.class.getName();
    private volatile int hashedDirectories = HashedStore.DEFAULT_NUMBER_DIRECTORIES;
    private volatile boolean transactionSync = true;

    private volatile boolean scanZeroLengthFiles = false;

    private volatile int share = StateType.OS_UNSHARED;
    private volatile int hierarchyRetry = 100;
    private volatile int hierarchyTimeout = 100;

    private volatile boolean volatileStoreSupportAllObjUids;

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.size")
    private volatile int cacheStoreSize = 10240;  // size in bytes
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.sync")
    private volatile boolean cacheStoreSync = false;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.removedItems")
    private volatile int cacheStoreRemovedItems = 256;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.scanPeriod")
    private volatile int cacheStoreScanPeriod = 120000;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.workItems")
    private volatile int cacheStoreWorkItems = 100;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.objectstore.cacheStore.hash")
    private volatile int cacheStoreHash = 128;

    @FullPropertyName(name = "com.arjuna.ats.arjuna.coordinator.transactionLog.synchronousRemoval")
    private volatile boolean synchronousRemoval = true;
    @FullPropertyName(name = "com.arjuna.ats.arjuna.coordinator.transactionLog.size")
    private volatile long txLogSize = 10 * 1024 * 1024;  // default maximum log txLogSize in bytes;
    @FullPropertyName(name = "com.arjuna.ats.arjuna.coordinator.transactionLog.purgeTime")
    private volatile long purgeTime = 100000; // in milliseconds

    private volatile boolean androidDirCheck = false;
    
	private volatile String jdbcAccess;

	private volatile String tablePrefix;

	private volatile boolean dropTable;
	
	private volatile boolean createTable = true;
    
    private volatile boolean exposeAllLogRecordsAsMBeans = false;

    private volatile boolean ignoreMBeanHeuristics = true;

    private volatile String jmxToolingMBeanName = "jboss.jta:type=ObjectStore";

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
    public int getCacheStoreSize()
    {
        if (cacheStoreSize < 0)
        {
            return 0;
        }

        return cacheStoreSize;
    }

    /**
     * Sets the maximum size, in bytes, of the in-memory object state cache.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * @param cacheStoreSize the maximum cache size in bytes.
     */
    public void setCacheStoreSize(int cacheStoreSize)
    {
        this.cacheStoreSize = cacheStoreSize;
    }

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
    public boolean isCacheStoreSync()
    {
        return cacheStoreSync;
    }

    /**
     * Sets if writes to the store should be synched to disk or not.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * @param cacheStoreSync true to enable syncing, false to disable.
     */
    public void setCacheStoreSync(boolean cacheStoreSync)
    {
        this.cacheStoreSync = cacheStoreSync;
    }

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
    public int getCacheStoreRemovedItems()
    {
        if (cacheStoreRemovedItems < 0)
        {
            return 0;
        }

        return cacheStoreRemovedItems;
    }

    /**
     * Sets the maximum number of removed items that may be held in the cache before being purged.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * @param cacheStoreRemovedItems teh maximun number of items.
     */
    public void setCacheStoreRemovedItems(int cacheStoreRemovedItems)
    {
        this.cacheStoreRemovedItems = cacheStoreRemovedItems;
    }

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
    public int getCacheStoreScanPeriod()
    {
        return cacheStoreScanPeriod;
    }

    /**
     * Sets the interval on which the cache will process outstanding work, in milliseconds.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * @param cacheStoreScanPeriod the sleep duration, in milliseconds.
     */
    public void setCacheStoreScanPeriod(int cacheStoreScanPeriod)
    {
        this.cacheStoreScanPeriod = cacheStoreScanPeriod;
    }

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
    public int getCacheStoreWorkItems()
    {
        if (cacheStoreWorkItems < 0)
        {
            return 0;
        }

        return cacheStoreWorkItems;
    }

    /**
     * Sets the maximum number of outstanding writes that may be held in the cache.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * @param cacheStoreWorkItems the maximum number of outstnading writes.
     */
    public void setCacheStoreWorkItems(int cacheStoreWorkItems)
    {
        this.cacheStoreWorkItems = cacheStoreWorkItems;
    }

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
    public int getCacheStoreHash()
    {
        if (cacheStoreHash <= 0)
        {
            return 128;
        }

        return cacheStoreHash;
    }

    /**
     * Sets the number of hash buskets used to store the cache work queue.
     *
     * This property is used by the following object store implementations: CacheStore.
     *
     * @param cacheStoreHash the number of hash buckets.
     */
    public void setCacheStoreHash(int cacheStoreHash)
    {
        this.cacheStoreHash = cacheStoreHash;
    }


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
    public String getLocalOSRoot()
    {
        return localOSRoot;
    }

    /**
     * Sets the local ObjectStore root directory name. This should be a path element, not a complete path.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore, TwoPhaseVolatileStore, VolatileStore.
     *
     * @param localOSRoot the directory name.
     */
    public void setLocalOSRoot(String localOSRoot)
    {
        this.localOSRoot = localOSRoot;
    }

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
    public String getObjectStoreDir()
    {
        if (Utility.isAndroid() && !androidDirCheck)
        {
            try
            {
                /*
                 * Use reflection so we can build this in an environment that does
                 * not have the various Android libraries available.
                 */
                     
                Class<?> instance = Class.forName("android.os.Environment");
                
                Method[] mthds = instance.getDeclaredMethods();
                Method m = null;
                
                for (int i = 0; (i < mthds.length) && (m == null); i++)
                {
                    if ("getExternalStorageDirectory".equals(mthds[i].getName()))
                        m = mthds[i];
                }
                
                objectStoreDir = ((File) m.invoke(null)).toString() + File.separator + "ObjectStore";
                
                androidDirCheck = true;
            }
            catch (final Throwable ex)
            {
                ex.printStackTrace();
            }
        }
        
        return objectStoreDir;
    }

    /**
     * Sets the ObjectStore directory path.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore, TwoPhaseVolatileStore, VolatileStore.
     *
     * @param objectStoreDir the directory path.
     */
    public void setObjectStoreDir(String objectStoreDir)
    {
        this.objectStoreDir = objectStoreDir;
    }

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
    public boolean isObjectStoreSync()
    {
        return objectStoreSync;
    }

    /**
     * Sets if ObjectStore operations should be synched to disk or not.
     * Caution: Disabling this may be lead to non-ACID transaction behaviour.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore.
     *
     * @param objectStoreSync true to sunc to disk, false to skip synching.
     */
    public void setObjectStoreSync(boolean objectStoreSync)
    {
        this.objectStoreSync = objectStoreSync;
    }

    /**
     * Returns the fully qualified class name for the ObjectStore implementation.
     *
     * Default: "com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore"
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.objectStoreType
     *
     * @return the fully qualified class name of the ObjectStore implementation.
     */
    public String getObjectStoreType()
    {
        return objectStoreType;
    }

    /**
     * Sets the symbolic name of the ObjectStore implementation.
     *
     * @param objectStoreType the symbolic name of the implementation.
     */
    public void setObjectStoreType(String objectStoreType)
    {
        this.objectStoreType = objectStoreType;
    }

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
    public int getHashedDirectories()
    {
        if (hashedDirectories <= 0) {
            tsLogger.i18NLogger.warn_objectstore_HashedStore_2(Integer.toString(hashedDirectories));
            return HashedStore.DEFAULT_NUMBER_DIRECTORIES;
        }
        return hashedDirectories;
    }

    /**
     * Sets the number of directories over which the ObjectStore will be split.
     *
     * This property is used by the following object store implementations: CacheStore, HashedActionStore, HashedStore.
     *
     * @param hashedDirectories the number of directories.
     */
    public void setHashedDirectories(int hashedDirectories)
    {
        this.hashedDirectories = hashedDirectories;
    }

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
    public boolean isTransactionSync()
    {
        return transactionSync;
    }

    /**
     * Sets if transaction log operations should be synched to disk or not.
     * Caution: Disabling this may be lead to non-ACID transaction behaviour.
     *
     * This property is used by the following object store implementations: ActionStore, HashedActionStore, LogStore.
     *
     * @param transactionSync true to enable synching, false to disable.
     */
    public void setTransactionSync(boolean transactionSync)
    {
        this.transactionSync = transactionSync;
    }

    /**
     * Returns true if zero length files should be returned by a recovery scan.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore.
     *
     * Default: false
     *
     * @return true if scan results should include zero length files, false if they should be excluded.
     */
    public boolean isScanZeroLengthFiles()
    {
        return scanZeroLengthFiles;
    }

    /**
     * Sets if zero length files should be returned by a recovery scan.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore.
     *
     * @param scanZeroLengthFiles true to include zero length files in scan results, false to exclude them.
     */
    public void setScanZeroLengthFiles(boolean scanZeroLengthFiles)
    {
        this.scanZeroLengthFiles = scanZeroLengthFiles;
    }

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
    public int getShare()
    {
        return share;
    }

    /**
     * Sets the share mode of the ObjectStore
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore, TwoPhaseVolatileStore, VolatileStore.
     *
     * @param share a valid share mode.
     */
    public void setShare(int share)
    {
        this.share = share;
    }

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
    public int getHierarchyRetry()
    {
        if (hierarchyRetry < 0)
        {
            return 100;
        }

        return hierarchyRetry;
    }

    /**
     * Sets the maximum number of attempts which may be made to create a direcory tree in the store.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore.
     *
     * @param hierarchyRetry the maximum number of file creation attempts.
     */
    public void setHierarchyRetry(int hierarchyRetry)
    {
        this.hierarchyRetry = hierarchyRetry;
    }

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
    public int getHierarchyTimeout()
    {
        if (hierarchyTimeout < 0)
        {
            return 100;
        }

        return hierarchyTimeout;
    }

    /**
     * Sets the time in milliseconds to wait between file creation retries.
     *
     * This property is used by the following object store implementations: ActionStore, CacheStore, HashedActionStore,
     * HashedStore, LogStore, NullActionStore, ShadowingStore, ShadowNoFileLockStore.
     *
     * @param hierarchyTimeout the wait time in milliseconds.
     */
    public void setHierarchyTimeout(int hierarchyTimeout)
    {
        this.hierarchyTimeout = hierarchyTimeout;
    }

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
    public boolean isSynchronousRemoval()
    {
        return synchronousRemoval;
    }

    /**
     * Sets if the LogStore should write removal records synchronously or not.
     *
     * This property is used by the following object store implementations: LogStore.
     *
     * @param synchronousRemoval true for synchronous operation, false for asynchronous.
     */
    public void setSynchronousRemoval(boolean synchronousRemoval)
    {
        this.synchronousRemoval = synchronousRemoval;
    }

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
    public long getTxLogSize()
    {
        return txLogSize;
    }

    /**
     * Sets the default size of the LogStore, in bytes.
     *
     * This property is used by the following object store implementations: LogStore.
     *
     * @param txLogSize the default file size, in bytes.
     */
    public void setTxLogSize(long txLogSize)
    {
        this.txLogSize = txLogSize;
    }

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
    public long getPurgeTime()
    {
        return purgeTime;
    }

    /**
     * Sets the purge interval for the LogStore, in milliseconds.
     *
     * This property is used by the following object store implementations: LogStore.
     *
     * @param purgeTime the purge interval in milliseconds.
     */
    public void setPurgeTime(long purgeTime)
    {
        this.purgeTime = purgeTime;
    }

	/**
	 * Returns an instance of a class implementing JDBCAccess.
	 * 
	 * @return a JDBCAccess implementation instance, or null.
	 */
	public String getJdbcAccess() {
		return jdbcAccess;
	}

	/**
	 * Sets the instance of JDBCAccess
	 * 
	 * @param connectionDetails
	 *            an Object that provides JDBCAccess, or null.
	 */
	public void setJdbcAccess(String connectionDetails) {
		jdbcAccess = connectionDetails;
	}

	/**
	 * Get the table prefix
	 * 
	 * @return The prefix to apply to the table
	 */
	public String getTablePrefix() {
		return tablePrefix;
	}

	/**
	 * Set the table prefix
	 * 
	 * @param tablePrefix
	 *            A prefix to use on the tables
	 */
	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	/**
	 * Should the store drop the table
	 * 
	 * @return Whether to drop the table
	 */
	public boolean getDropTable() {
		return dropTable;
	}

	/**
	 * Set whether to drop the table.
	 * 
	 * @param dropTable
	 *            Drop the table
	 */
	public void setDropTable(boolean dropTable) {
		this.dropTable = dropTable;
	}

	/**
	 * Should the store create the table
	 * 
	 * @return Whether to create the table
	 */
	public boolean getCreateTable() {
		return createTable;
	}

	/**
	 * Set whether to create the table.
	 * 
	 * @param createTable
	 *            Create the table
	 */
	public void setCreateTable(boolean createTable) {
		this.createTable = createTable;
	}

    /**
     * @return Whether basic information about all log reccords are exposed
     */
    public boolean getExposeAllLogRecordsAsMBeans() {
        return exposeAllLogRecordsAsMBeans;
    }

    /**
     * Determine whether the ObjStoreBrowser should report basic information about all logs.
     * Because exposing log records has the side effect of activating the default is to
     * only activate ones for which we know the side effects are harmless
     * {@link com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser ObjectStoreBrowser}
     *
     * Use this method to explicitly set the desired behaviour.
     *
     * You can also set this behaviour via JMX using
     * {@link com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowserMBean#setExposeAllRecordsAsMBeans JMX}
     * 
     * @param exposeAllLogRecords
     *            Set to true to expose basic information about all log records
     */
    public void setExposeAllLogRecordsAsMBeans(boolean exposeAllLogRecords) {
        this.exposeAllLogRecordsAsMBeans = exposeAllLogRecords;
    }

    /**
     * Determine whether or not MBean operations that delete a transaction will delete participants that
     * are still in a heuristic state
     *
     * @param ignoreMBeanHeuristics if false heuristic participants may only be deleted after the heuristic
     *                                         has been cleared
     */
    public void setIgnoreMBeanHeuristics(boolean ignoreMBeanHeuristics) {
        this.ignoreMBeanHeuristics = ignoreMBeanHeuristics;
    }

    /**
     *
     * @return whether or not MBean operations that delete a transaction will delete participants that
     *         are still in a heuristic state
     */
    public boolean isIgnoreMBeanHeuristics() {
        return ignoreMBeanHeuristics;
    }

    /**
     * @return whether the volatile store types {@link com.arjuna.ats.internal.arjuna.objectstore.VolatileStore}
     * and {@link com.arjuna.ats.internal.arjuna.objectstore.TwoPhaseVolatileStore} should support the
     * {@link com.arjuna.ats.arjuna.objectstore.RecoveryStore#allObjUids} and
     * {@link com.arjuna.ats.arjuna.objectstore.RecoveryStore#allTypes(com.arjuna.ats.arjuna.state.InputObjectState)}
     * API methods
     */
    public boolean isVolatileStoreSupportAllObjUids() {
        return volatileStoreSupportAllObjUids;
    }

    /**
     * Indicate whether or not the volatile store types {@link com.arjuna.ats.internal.arjuna.objectstore.VolatileStore}
     * and {@link com.arjuna.ats.internal.arjuna.objectstore.TwoPhaseVolatileStore} should support the
     * {@link com.arjuna.ats.arjuna.objectstore.RecoveryStore#allObjUids} and
     * {@link com.arjuna.ats.arjuna.objectstore.RecoveryStore#allTypes(com.arjuna.ats.arjuna.state.InputObjectState)}
     * API methods
     * @param volatileStoreSupportAllObjUids if true then add support for finding Uids by type in the volatile stores
     */
    public void setVolatileStoreSupportAllObjUids(boolean volatileStoreSupportAllObjUids) {
        this.volatileStoreSupportAllObjUids = volatileStoreSupportAllObjUids;
    }

    /**
     * Set JMX name where the Narayana object store tooling MBean will be registered at.
     *
     * @param jmxToolingMBeanName A name of the JMX MBean
     */
    public void setJmxToolingMBeanName(String jmxToolingMBeanName) {
        this.jmxToolingMBeanName = jmxToolingMBeanName;
    }

    /**
     * JMX name that the Narayana object store tooling MBean will be registered at.
     *
     * The default value is {@code jboss.jta:type=ObjectStore}
     * which is considered deprecated anw will be changed for {@code narayana.logStore:type=ObjectStore}
     * in some next Narayana releases.
     *
     * @return name of the MBean where tooling objects will be available at
     */
    public String getJmxToolingMBeanName() {
        return jmxToolingMBeanName;
    }
}
