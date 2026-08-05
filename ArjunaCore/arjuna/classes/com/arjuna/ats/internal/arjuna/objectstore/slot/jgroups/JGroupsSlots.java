/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.jgroups.blocks.Cache;
import org.jgroups.blocks.ReplCache;

import java.io.IOException;
import java.util.Set;

/**
 * A {@link com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStore} implementation backed by a jGroups cache.
 * It is an in-memory datastore running as a jGroups cluster to maintain data availability
 * (provided the caches are suitably configured to manage replication of data across the cluster).
 */
public class JGroupsSlots implements BackingSlots {
    private ByteArrayKey[] slots = null;
    private ReplCache<ByteArrayKey, byte[]> cache;
    private JGroupsSlotKeyGenerator jGroupsSlotKeyGenerator;
    private short replicationCount = -1;
    private SlotJournal journal = null;  // Optional WAL for persistence

    /**
     * Overrides {@link BackingSlots#init(SlotStoreEnvironmentBean)} and has the same meaning
     * @param slotStoreConfig the config to use for the initialisation
     * @throws IOException if the cache operation threw an exception
     */
    @Override
    public void init(SlotStoreEnvironmentBean slotStoreConfig) throws IOException {
        JGroupsStoreEnvironmentBean config;

        tsLogger.i18NLogger.warn_jgroups_slot_store_is_experimental();

        if (slotStoreConfig instanceof JGroupsStoreEnvironmentBean) {
            config = (JGroupsStoreEnvironmentBean) slotStoreConfig;
        } else {
            config = BeanPopulator.getDefaultInstance(JGroupsStoreEnvironmentBean.class);
        }

        slots = new ByteArrayKey[slotStoreConfig.getNumberOfSlots()];
        jGroupsSlotKeyGenerator = config.getSlotKeyGenerator();

        if (jGroupsSlotKeyGenerator == null) {
            jGroupsSlotKeyGenerator = new JGroupsSlotKeyGenerator() {
                @Override
                public ByteArrayKey generateUniqueKey(int index) {
                    return new ByteArrayKey(new Uid().getBytes());
                }

                @Override
                public void init(JGroupsStoreEnvironmentBean ignore) {
                }
            };
        }
        jGroupsSlotKeyGenerator.init(config);

        try {
            // Initialize write-ahead log if enabled
            if (config.isWalEnabled()) {
                tsLogger.logger.infof("JGroupsSlots: Enabling write-ahead log with " +
                                "storeDir=%s, syncWrites=%s, syncDeletes=%s, fileSize=%d, minFiles=%d, asyncIO=%s",
                    config.getStoreDir(), config.isWalSyncWrites(), config.isWalSyncDeletes(),
                    config.getWalFileSize(), config.getWalMinFiles(), config.isWalAsyncIO());

                journal = new SlotJournal(config);
                journal.start();

                tsLogger.logger.debugf("JGroupsSlots: write-ahead log loaded %d slots from disk", journal.size());
            }

            cache = config.getCache();
            replicationCount = config.getReplicationCount();
            cache.start();

            // load existing keys from cache (start will have synchronised state with other cluster members)
            Set<ByteArrayKey> existingKeys = cache.getL2Cache().getInternalMap().keySet();
            load(existingKeys);

            // and then load from WAL (slots[] is fully initialized) making sure not to overwrite cache entries
            if (journal != null) {
                loadFromWAL();
            }
        } catch (Exception e) {
            // release anything already started before failing init
            try {
                if (cache != null) {
                    cache.stop();
                }
            } catch (Exception ignore) {
            }
            try {
                if (journal != null) {
                    journal.stop();
                }
            } catch (Exception ignore) {
            }

            throw new IOException(e);
        }
    }

    /**
     * Load slots from the write-ahead log, when enabled, into cache.
     * Only loads data if not already present in cache (avoids overwriting
     * newer replicated data with stale log entries).
     */
    private void loadFromWAL() throws Exception {
        if (journal == null) {
            return;
        }

        int recoveredCount = 0; // incremented when an entry is recovered from the log
        int skippedCount = 0; // incremented if the cache already contains the log entry
        boolean warned = false;

        Set<ByteArrayKey> cacheKeys = cache.getL2Cache().getInternalMap().keySet();
        int nextFree = 0; // next slot to populate

        for (Integer slotId : journal.getSlotIds()) {
            if (slotId < 0 || slotId >= slots.length) {
                if (!warned) {
                    tsLogger.i18NLogger.warn_slot_store_too_few_slots(journal.size(), slots.length);
                    warned = true;
                }
                tsLogger.logger.debugf("JGroupsSlots: WAL contains out-of-range slot ID %d (valid range 0..%d)",
                        slotId.intValue(), slots.length - 1);
                continue;
            }

            ByteArrayKey originalKey = journal.getKey(slotId);
            byte[] data = journal.read(slotId);

            if (putIfAbsent(cache, originalKey, data)) {
                while (nextFree < slots.length && cacheKeys.contains(slots[nextFree])) {
                    nextFree++;
                }

                if (nextFree >= slots.length) {
                    throw new IOException(tsLogger.i18NLogger.get_jgroups_too_few_slots(slots.length));
                }

                slots[nextFree] = originalKey;
                cache.put(originalKey, data, replicationCount, 0);
                if (nextFree != slotId) {
                    journal.delete(slotId);
                    journal.write(nextFree, originalKey, data);
                }
                nextFree++;
                recoveredCount++;
            } else {
                skippedCount++;
            }
        }

        tsLogger.logger.debugf("JGroupsSlots: Recovered %d slots from write-ahead log to cache%s",
            recoveredCount, skippedCount > 0 ? " (skipped " + skippedCount + " already in cache)" : "");
    }

    /*
     * The slots initialisation algorithm is:
     * 1.  Initialise the slots array using the cache (load method). Cache key iteration order is
     *     non-deterministic, so slots[i] may map to a different ByteArrayKey than before a crash.
     * 2a. Read keys and data from the write-ahead log (loadFromWAL method).
     * 2b. For each WAL entry, check putIfAbsent against the original ByteArrayKey persisted with the
     *     entry - not slots[slotId], which may have been reassigned by load(). This prevents data
     *     loss when load() shuffles key-to-slot assignments. If the original key is not in the cache,
     *     assign it to a free slot position and replicate via cache.put().
     *
     *     putIfAbsent on the internal JGroups map is atomic against the receiver thread, so if
     *     replicated data arrives concurrently, putIfAbsent keeps the replicated value and discards
     *     the stale WAL value.
     *
     * Scenarios:
     *     Single-node crash or restart with surviving nodes: the surviving nodes already have the data
     *     via replication, so putIfAbsent returns false and WAL entries are skipped.
     *
     *     All-nodes crash or restart: each node recovers its own WAL locally via putIfAbsent, then
     *     replicates the recovered data to the cluster via cache.put() to restore the full replicated
     *     state.
     *
     *     Partial-crash with unreplicated data: the WAL entries original key is not in the cache.
     *     putIfAbsent succeeds, and the data is assigned to a free slot and replicated.
     *
     *     A graceful restart behaves the same as a crash from this algorithm's perspective - the WAL
     *     may contain entries for transactions that were active at shutdown time, and the same
     *     putIfAbsent-then-replicate logic applies.
     */
    private static <K, V> boolean putIfAbsent(ReplCache<K, V> cache, K key, V val) {
        // remark the ReplCache.Value constructor uses an arbitrary value for the second parameter (replication_count)
        // because put is to the local internal map which bypasses replication (see ReplCache.mcastPut)
        ReplCache.Value<V> replValue = new ReplCache.Value<>(val, (short) -1);
        Cache.Value<ReplCache.Value<V>> cacheValue = new Cache.Value<>(replValue, 0L);

        return cache.getL2Cache().getInternalMap().putIfAbsent(key, cacheValue) == null;
    }

    /**
     * Overrides {@link BackingSlots#write(int, byte[], boolean)}
     * The write semantics depend on how the cache was configured {@link JGroupsStoreEnvironmentBean#setCache(ReplCache)}
     *
     * Overrides @link {BackingSlots} and has the same meaning
     *
     * @param slot the index, from 0 to config numberOfSlots-1
     * @param data the content.
     * @param sync not used (use {@link JGroupsStoreEnvironmentBean#setReplicationCount} to control how write operations
     *             behave)
     *
     * @throws IOException if the cache operation threw an exception
     */
    @Override
    public void write(int slot, byte[] data, boolean sync) throws IOException {
        try {
            // Write to WAL first (if enabled) for durability
            if (journal != null) {
                journal.write(slot, slots[slot], data);
            }

            /*
             * cache the value until explicitly removed (timeout 0) by the transaction manager.
             * The replicationCount controls how many nodes will see the write operation,
             * -1 means don't cache at all in the L1 cache (L1 is the local cache L2 is the distributed one).
             *
             * A non-zero timeout value is the number of milliseconds to keep an idle (unaccessed) element in the cache
             * - we never want to timeout entries instead relying on the TM to explicitly remove the item when it
             * is no longer in doubt.
             */
            cache.put(slots[slot], data, replicationCount, 0);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Overrides {@link BackingSlots#read(int)}
     * The read semantics depend on how the cache ({@link JGroupsStoreEnvironmentBean#setCache(ReplCache)} setCache(Cache)})
     * was configured
     *
     * @param slot the index, from 0 to config numberOfSlots-1
     *
     * @throws IOException if the cache operation threw an exception
     */
    @Override
    public byte[] read(int slot) throws IOException {
        try {
            byte[] data = cache.get(slots[slot]);

            // If not in cache but WAL enabled, try WAL (shouldn't happen normally)
            if (data == null && journal != null) {
                data = journal.read(slot);
            }

            return data;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Overrides {@link BackingSlots#clear(int, boolean)} and has the same meaning
     * @param slot the index, from 0 to config numberOfSlots-1
     * @param sync not used because the sync behaviour depends on the cache configuration
     * @throws IOException if the cache operation threw an exception
     */
    @Override
    public void clear(int slot, boolean sync) throws IOException {
        try {
            ByteArrayKey key = slots[slot];

            // Delete from WAL first (if enabled)
            if (journal != null) {
                journal.delete(slot);
            }

            // Remove from cache - both the replicated cache and local L2 cache
            // Note: ReplCache.remove() removes from distributed cache but not always from L2
            cache.remove(key);
            cache.getL2Cache().remove(key);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void stop() {
        if (journal != null) {
            try {
                journal.stop();
                tsLogger.logger.debugf("JGroupsSlots: write-ahead log stopped");
            } catch (Exception e) {
                tsLogger.logger.infof("JGroupsSlots: Error stopping write-ahead log: %s", e.getMessage());
            }
        }
        if (cache != null) {
            cache.stop();
        }
    }

    private void load(Set<ByteArrayKey> keys) throws IOException {
        int i = 0;

        for (ByteArrayKey key : keys) {
            if (i < slots.length) {
                slots[i] = key;
                i += 1;
            } else {
                /*
                 * The number of slots should equal the maximum number of unresolved transactions expected at any given
                 * time, including those in-flight and awaiting recovery.
                 */
                String errorMsg = tsLogger.i18NLogger.get_jgroups_too_few_slots(keys.size(), slots.length);

                throw new IOException(errorMsg);
            }
        }

        // initialise the remaining slots
        while (i < slots.length) {
            try {
                slots[i] = jGroupsSlotKeyGenerator.generateUniqueKey(i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            i += 1;
        }
    }
}
