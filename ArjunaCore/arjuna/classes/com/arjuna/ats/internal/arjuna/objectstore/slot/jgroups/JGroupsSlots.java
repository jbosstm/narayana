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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStore} implementation backed by a jGroups cache.
 * It is an in-memory datastore running as a jGroups cluster to maintain data availability
 * (provided the caches are suitably configured to manage replication of data across the cluster).
 *
 * <p><b>NOTE</b>: This is an Experimental feature and is not recommended for production systems.
 * May contain breaking changes in future releases.
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

        if (slotStoreConfig instanceof JGroupsStoreEnvironmentBean) {
            config = (JGroupsStoreEnvironmentBean) slotStoreConfig;
        } else {
            config = BeanPopulator.getDefaultInstance(JGroupsStoreEnvironmentBean.class);
        }

        if (!config.isExperimentalEnabled()) {
            throw new IOException(
                    "JGroupsSlotStore is experimental and disabled by default. " +
                    "Call JGroupsStoreEnvironmentBean.setExperimentalEnabled(true) to enable it.");
        }

        tsLogger.i18NLogger.warn_jgroups_slot_store_is_experimental();

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

        int recoveredCount = 0;
        int skippedCount = 0;
        boolean warned = false;

        Set<ByteArrayKey> cacheKeys = cache.getL2Cache().getInternalMap().keySet();
        int nextFree = 0;

        // Journal mutations are deferred to a second phase so that the complete
        // slot mapping is known before any record is moved. Without deferral,
        // two records that swap positions (A at slot 2 → 5, B at slot 5 → 2)
        // would interfere: deleting slot 2 before processing slot 5 loses A's
        // data, and the recovery path could overwrite a cache-hit record's
        // journal entry before the cache-hit iteration reads it.
        Set<Integer> journalDeletes = new LinkedHashSet<>();
        Map<Integer, Object[]> journalWrites = new LinkedHashMap<>();

        Map<ByteArrayKey, Integer> slotIndex = new HashMap<>();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                slotIndex.put(slots[i], i);
            }
        }

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
                slotIndex.put(originalKey, nextFree);
                cache.put(originalKey, data, replicationCount, 0);
                if (nextFree != slotId) {
                    journalDeletes.add(slotId);
                    journalWrites.put(nextFree, new Object[]{originalKey, data});
                }
                nextFree++;
                recoveredCount++;
            } else {
                byte[] cacheData = cache.get(originalKey);
                if (cacheData == null) {
                    journalDeletes.add(slotId);
                    skippedCount++;
                    continue;
                }
                Integer indexedSlot = slotIndex.get(originalKey);
                int currentSlot = indexedSlot != null ? indexedSlot : -1;
                if (currentSlot < 0) {
                    while (nextFree < slots.length && cacheKeys.contains(slots[nextFree])) {
                        nextFree++;
                    }
                    if (nextFree >= slots.length) {
                        throw new IOException(tsLogger.i18NLogger.get_jgroups_too_few_slots(slots.length));
                    }
                    currentSlot = nextFree;
                    slots[currentSlot] = originalKey;
                    slotIndex.put(originalKey, currentSlot);
                    nextFree++;
                }
                if (currentSlot != slotId) {
                    journalDeletes.add(slotId);
                }
                journalWrites.put(currentSlot, new Object[]{originalKey, cacheData});
                skippedCount++;
            }
        }

        // Phase 2: apply all journal mutations. Deleting every source before
        // writing any destination prevents data loss when records swap slots.
        for (Integer slot : journalDeletes) {
            journal.delete(slot);
        }
        for (Map.Entry<Integer, Object[]> entry : journalWrites.entrySet()) {
            journal.write(entry.getKey(), (ByteArrayKey) entry.getValue()[0], (byte[]) entry.getValue()[1]);
        }

        tsLogger.logger.debugf("JGroupsSlots: Recovered %d slots from write-ahead log to cache%s",
            recoveredCount, skippedCount > 0 ? " (skipped " + skippedCount + " already in cache)" : "");
    }

    /*
     * == Slot-reassignment problem and WAL recovery ==
     *
     * load() populates slots[] from the cache's ConcurrentHashMap key set, whose iteration order is
     * non-deterministic. After a restart the same ByteArrayKey may occupy a different slot index than
     * the one recorded in the WAL at write time. Two things must be correct for recovery/restart to work:
     *
     *   1. Lookup by the right key (SHA ec341880c1). Each WAL record persists the original ByteArrayKey
     *      alongside the data. loadFromWAL() uses that persisted key for putIfAbsent - not
     *      slots[slotId], which may now point to an unrelated key. Without this, putIfAbsent could
     *      match against the wrong cache entry, silently skipping unreplicated data.
     *
     *   2. Rebase the journal index. When putIfAbsent returns false (a surviving node already has the
     *      data), the journal record is still indexed under the pre-crash slot position. If the key
     *      now lives at a different slot, clear(currentSlot) would call journal.delete(currentSlot) -
     *      missing the record at the old position - and a later restart would resurrect cleared data.
     *      To avoid this scenario the fix deletes the stale entry and rewrites it at the current slot.
     *      If the key is not yet in slots[] (it arrived via replication from a faster node after load()
     *      ran), a free slot is claimed so the data is reachable through the slot interface.
     *
     *      putIfAbsent on the internal local JGroups map is atomic against the JGroups message receiver
     *      thread, so if replicated data arrives concurrently, putIfAbsent keeps the replicated value
     *      and discards the stale WAL value.
     *
     * Scenarios:
     *     Single-node crash with surviving nodes: survivors already have the data via replication,
     *     so putIfAbsent returns false. The journal is rebased to the current slot mapping.
     *
     *     All-nodes crash: each node recovers its own WAL locally (putIfAbsent returns true), then
     *     replicates via cache.put() to restore the full replicated state.
     *
     *     Partial crash with unreplicated data: the WAL entry's original key is absent from the cache.
     *     putIfAbsent succeeds, the data is assigned to a free slot and replicated.
     *
     *     All-nodes simultaneous restart: a race is possible - a faster node may replicate recovered
     *     data before a slower node's loadFromWAL() runs, causing putIfAbsent to return false for
     *     keys that load() never saw. The free-slot claim in the rebase path handles this.
     *
     *     Graceful restart: behaves the same as a crash  the WAL may contain entries for transactions
     *     that were active at shutdown, and the same putIfAbsent-then-rebase logic applies.
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
