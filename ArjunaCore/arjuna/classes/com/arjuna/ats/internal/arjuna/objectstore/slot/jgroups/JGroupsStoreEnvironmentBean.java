/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.common.internal.util.ClassloadingUtility;
import org.jgroups.blocks.ReplCache;

import java.io.File;

/**
 * Configuration properties for an JGroups backed slot store implementation
 * {@link com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots}
 * <p>
 * NOTE: This is an Experimental feature (JGroups Slot Store) and is not recommended for production systems and may
 * contain breaking changes in future releases.
 */
public class JGroupsStoreEnvironmentBean extends SlotStoreEnvironmentBean implements JGroupsStoreEnvironmentBeanMBean {

    private String jGroupsConfigFileName = "jgroups-transport-config.xml"; // "jgroups.xml";
    private volatile ReplCache<ByteArrayKey, byte[]> cache;
    private String clusterName = "defaultJGroupsClusterName";
    private String cacheName = "defaultJGroupsCache";
    private short replicationCount = -1;
    private String nodeAddress;
    private String slotKeyGeneratorClassName;
    private JGroupsSlotKeyGenerator jGroupsSlotKeyGenerator;
    private long cachingTime = 0L;  // L2 cache time in millis (0 = disabled for consistency)
    private long callTimeout = 1500L; // maximum time (in milliseconds) a cache operation—such will wait for responses
    private boolean migrateData = true;

    // WAL (Write-Ahead Log) persistence for crash recovery
    private boolean walEnabled = true; // persist writes to a log before writing to the cache
    private volatile int walFileSize = 1024*1024*2; // 2MB per file
    private volatile int walMinFiles = 2; // Minimum 2 files
    private volatile int walPoolSize = 20; // upper limit for pre-created journal files
    private volatile int walCompactMinFiles = 10; // minimal number of files before we can considering compacting
    private volatile int walCompactPercentage = 30;
    private volatile String walFilePrefix = "slot-journal";
    private volatile String walFileExtension = "log";
    private volatile int walMaxIO = 2;
    private volatile boolean walSyncWrites = true;
    private volatile boolean walSyncDeletes = true;
    private volatile int walBufferFlushesPerSecond = 500;
    private volatile int walBufferSize = 490 * 1024;
    private volatile boolean walAsyncIO = false;

    /**
     * get the filename of the protocol stack config (transport, discovery, failure detection, etc.).
     * <p>
     * @return the name of config file
     */
    public String getJGroupsConfigFileName() {
        return jGroupsConfigFileName;
    }

    public void setJGroupsConfigFileName(String jGroupsConfigFileName) {
        this.jGroupsConfigFileName = jGroupsConfigFileName;
    }

    public ReplCache<ByteArrayKey, byte[]> getCache() throws CoreEnvironmentBeanException {
        if (cache == null) {
            if (jGroupsConfigFileName == null) {
                throw new CoreEnvironmentBeanException(tsLogger.i18NLogger.get_jgroups_config());
            }
            synchronized (this) {
                if (cache == null) { // double-checked locking
                    cache = new ReplCache<>(jGroupsConfigFileName, getCacheName());
                    // TODO are these properties configurable via jGroupsConfigFileName is the callTimeout
                    cache.setCallTimeout(callTimeout);
                    cache.setCachingTime(cachingTime);
                    cache.setMigrateData(migrateData);
                }
            }
        }

        return cache;
    }

    public void setCache(ReplCache<ByteArrayKey, byte[]> cache) {
        this.cache = cache;
    }

    /**
     * Define how many times an element should be available in a cluster.
     * The default is -1 meaning the element is stored on all cluster nodes (full replication).
     * With 1 the element is stored on a single node only, determined through consistent hashing (distribution).
     * Setting it to a number K greater than 1 will store the element K times in the cluster.
     * TODO implement the value internally by monitoring the cluster
     */
    public short getReplicationCount() {
        return replicationCount;
    }

    public void setReplicationCount(short replicationCount) {
        this.replicationCount = replicationCount;
    }

    /**
     * The address of the node within a cluster.
     * Used in clustered and embedded scenarios to identify which member of the JGroups cluster
     * the JVM is currently representing.
     *
     * For single node clusters the {@link CoreEnvironmentBean#getNodeIdentifier()} is a good choice
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
        return cache != null ? cache.getClusterName() : cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * Get the L2 cache time in milliseconds.
     * The L2 cache is a local cache that reduces network calls by caching get() results.
     * Setting to 0 disables L2 caching for immediate consistency (recommended for WAL).
     * Setting to a positive value (e.g., 30000 for 30 seconds) improves performance but
     * may return stale data after remove() operations.
     *
     * @return caching time in milliseconds (0 = disabled, default)
     */
    public long getCachingTime() {
        return cachingTime;
    }

    public void setCachingTime(long cachingTime) {
        this.cachingTime = cachingTime;
    }

    /**
     * Get the maximum time (in milliseconds) a cache operation (a replicated put, for example) will wait for
     * responses from other nodes before aborting
     * @return the number of milliseconds before a cache operation will abort
     */
    public long getCallTimeout() {
        return callTimeout;
    }

    public void setCallTimeout(long callTimeout) {
        this.callTimeout = callTimeout;
    }

    /**
     * Whether to automatically migrate or re-replicate data entries to other nodes in the cluster when nodes join or leave
     * @return whether
     */
    public boolean isMigrateData() {
        return migrateData;
    }

    public void setMigrateData(boolean migrateData) {
        this.migrateData = migrateData;
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
     * {@link JGroupsStoreEnvironmentBean#getJGroupsConfigFileName()} is configured then the cache config
     * will use that file instead
     * @param storeDir the path to the store directory. If storeDir is not an absolute path then it will be
     *                 interpreted as being relative to the User's current working directory
     *                 (as defined by the user.dir system property)
     */
    @Override
    public void setStoreDir(String storeDir) {
        if (storeDir != null) {
            if (!new File(storeDir).isAbsolute()) {
                storeDir = System.getProperty("user.dir") + "/" + storeDir;
                super.setStoreDir(storeDir);
            } else {
                super.setStoreDir(storeDir);
            }
        }
    }

    @Override
    public void setBackingSlots(BackingSlots backingSlots) {
        super.setBackingSlots(backingSlots);
    }

    /**
     * classname of the generator function for internal slot keys. If the classname is unset and
     * {@link JGroupsStoreEnvironmentBean#setSlotKeyGenerator(JGroupsSlotKeyGenerator)} has not been called
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
     * @param jGroupsSlotKeyGenerator the slot key generator
     */
    public void setSlotKeyGenerator(JGroupsSlotKeyGenerator jGroupsSlotKeyGenerator) {
        this.jGroupsSlotKeyGenerator = jGroupsSlotKeyGenerator;
    }

    public JGroupsSlotKeyGenerator getSlotKeyGenerator() {
        if(jGroupsSlotKeyGenerator == null && slotKeyGeneratorClassName != null)
        {
            synchronized (this) {
                // double-checked locking
                if(jGroupsSlotKeyGenerator == null && (slotKeyGeneratorClassName != null && !slotKeyGeneratorClassName.isBlank())) {
                    jGroupsSlotKeyGenerator = ClassloadingUtility.loadAndInstantiateClass(JGroupsSlotKeyGenerator.class, slotKeyGeneratorClassName, null);
                }
            }
        }

        return jGroupsSlotKeyGenerator;
    }

    /**
     * Enable Write-Ahead Log for JGroupsSlots persistence.
     * When enabled, all slot writes are logged to disk for crash recovery.
     *
     * @return true if WAL is enabled
     */
    public boolean isWalEnabled() {
        return walEnabled;
    }

    public void setWalEnabled(boolean walEnabled) {
        this.walEnabled = walEnabled;
    }

    /**
     * Enable fsync after each write to WAL.
     * When enabled, writes are durable (survive crash) but slower (~10-20ms).
     * When disabled, writes are faster (~1-2ms) but may be lost on crash.
     *
     * @return true if fsync is enabled for writes
     */
    public boolean isWalSyncWrites() {
        return walSyncWrites;
    }

    public void setWalSyncWrites(boolean walSyncWrites) {
        this.walSyncWrites = walSyncWrites;
    }

    /**
     * Enable fsync after each delete from WAL.
     * If deletes are lost then recovery will generate warnings but no data loss.
     *
     * @return true if fsync is enabled for deletes
     */
    public boolean isWalSyncDeletes() {
        return walSyncDeletes;
    }

    public void setWalSyncDeletes(boolean walSyncDeletes) {
        this.walSyncDeletes = walSyncDeletes;
    }

    /**
     * Get the WAL buffer size in bytes for Artemis journal batching.
     * Larger buffers allow more writes to batch together before flushing.
     * Default: 490KB (matches HornetqJournalEnvironmentBean default)
     *
     * @return buffer size in bytes
     */
    public int getWalBufferSize() {
        return walBufferSize;
    }

    public void setWalBufferSize(int walBufferSize) {
        this.walBufferSize = walBufferSize;
    }

    /**
     * Get the WAL buffer flush rate (flushes per second).
     * Higher values = more frequent flushes = lower latency but less batching.
     * Lower values = less frequent flushes = higher latency but more batching.
     * Default: 500
     *
     * @return flushes per second
     */
    public int getWalBufferFlushesPerSecond() {
        return walBufferFlushesPerSecond;
    }

    public void setWalBufferFlushesPerSecond(int walBufferFlushesPerSecond) {
        this.walBufferFlushesPerSecond = walBufferFlushesPerSecond;
    }

    /**
     * Get the desired size in bytes of each WAL journal file.
     * Default: 2MB (2097152 bytes)
     *
     * @return the individual log file size, in bytes
     */
    public int getWalFileSize() {
        return walFileSize;
    }

    public void setWalFileSize(int walFileSize) {
        this.walFileSize = walFileSize;
    }

    /**
     * Get the minimum number of WAL journal files to use.
     * Default: 2
     *
     * @return the minimum number of individual log files
     */
    public int getWalMinFiles() {
        return walMinFiles;
    }

    public void setWalMinFiles(int walMinFiles) {
        this.walMinFiles = walMinFiles;
    }

    /**
     * Get how many WAL journal files can be reused.
     * Default: 0 (no pooling)
     *
     * @return the number of files that can be reused
     */
    public int getWalPoolSize() {
        return walPoolSize;
    }

    public void setWalPoolSize(int walPoolSize) {
        this.walPoolSize = walPoolSize;
    }

    /**
     * Get the minimal number of files before WAL compaction can be considered.
     * Default: 0 (automatic compaction disabled)
     *
     * @return the threshold file count
     */
    public int getWalCompactMinFiles() {
        return walCompactMinFiles;
    }

    public void setWalCompactMinFiles(int walCompactMinFiles) {
        this.walCompactMinFiles = walCompactMinFiles;
    }

    /**
     * Get the percentage minimum capacity usage at which to start WAL compaction.
     * Default: 0 (compaction disabled)
     *
     * @return the threshold percentage
     */
    public int getWalCompactPercentage() {
        return walCompactPercentage;
    }

    public void setWalCompactPercentage(int walCompactPercentage) {
        this.walCompactPercentage = walCompactPercentage;
    }

    /**
     * Get the prefix to be used when naming each WAL journal file.
     * Default: "slot-journal"
     *
     * @return the prefix used to construct individual log file names
     */
    public String getWalFilePrefix() {
        return walFilePrefix;
    }

    public void setWalFilePrefix(String walFilePrefix) {
        this.walFilePrefix = walFilePrefix;
    }

    /**
     * Get the suffix to be used when naming each WAL journal file.
     * Default: "log"
     *
     * @return the suffix used to construct individual log file names
     */
    public String getWalFileExtension() {
        return walFileExtension;
    }

    public void setWalFileExtension(String walFileExtension) {
        this.walFileExtension = walFileExtension;
    }

    /**
     * Get the maximum write requests queue depth for WAL.
     * For NIO this property has no effect and will be hard coded to 1.
     * For AIO, the default is 2 but the recommended value is 500.
     * Default: 1
     *
     * @return the max number of outstanding requests
     */
    public int getWalMaxIO() {
        return walMaxIO;
    }

    public void setWalMaxIO(int walMaxIO) {
        this.walMaxIO = walMaxIO;
    }

    /**
     * Get the IO type of WAL Journal.
     * Default: false (NIO)
     *
     * @return true if AsyncIO is enabled, false otherwise which means NIO
     */
    public boolean isWalAsyncIO() {
        return walAsyncIO;
    }

    /**
     * Set the type of WAL Journal.
     * Note that Journal silently falls back to NIO if AIO native libraries are not available.
     *
     * @param walAsyncIO true to enable AsyncIO, false to disable
     */
    public void setWalAsyncIO(boolean walAsyncIO) {
        this.walAsyncIO = walAsyncIO;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
