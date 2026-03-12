/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.infinispan.Cache;

import java.io.IOException;
import java.util.Set;

/**
 * A {@link com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStore} implementation backed by an infinispan cache.
 * It is an in-memory datastore and can be backed by a cluster of infinispan nodes to maintain data
 * availability provided the caches are suitably configured to manage replication of data across the cluster.
 * If the store is to be used with a recovery manager it is important that the environment is configured such that the
 * usual caveats are maintained:
 * - jgroups-raft, or equivalent, is used to ensure strict consistency as required by a CP system
 * - only one recovery manager acts as the leader
 * - if the leader fails automatic failover chooses a new leader while avoiding split brain scenarios
 * - etc.
 * Maintaining these requirements is non-trivial and requires extra support from the environment.
 * <p>
 * The interface is internal and is used by the {@link com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor}
 * and should not be called independently of the transaction and recovery systems.
 */
/*
 * Implementation notes:
 * A record is written to the store using
 * SlotStoreAdaptor#write_committed(Uid uid, String typeName, OutputObjectState outputObjectState).
 * From this data a SlotStoreKey key is created (new SlotStoreKey(uid, typeName, StateStatus.OS_COMMITTED);) and the Uid
 * and the type of the record serve as a key for the SlotStore and the data is the object state (outputObjectState).
 * The key is then packed into the object state (so now the outputObjectState contains both the key and the object store
 * record). The key is used as a key into SlotStore ConcurrentHashMap<SlotStoreKey, Integer> slotIdIndex and this
 * slotIdIndex "tracks the key to slot mapping for all in-use slots".
 *
 * The new data is placed in a free slot (Integer slotId = freeList.poll(); and slotIdIndex.put(key, slotId);), and the
 * data is then written to the actual backing slots implementation using slots.write(slotId, data).
 * The backend manages its own mapping of the slotId to the data. In the InfinispanSlots backend the data is in byte[][]
 * slots and is an array of keys (of type byte[]). When the backend initialises, it gets the current cache.keySet() and
 * places each cache entry key into one of the slots and initialises the remaining slots with a unique key (it uses an
 * instance of InfinispanSlotKeyGenerator defined in the InfinispanStoreEnvironmentBean config to get the unique key but
 * the generator can be anything that produces a unique entry for the slots table - it has to be unique because it is
 * used for the cache entry keys which are distributed to other nodes in cluster setups so minimally the Uid class
 * would be sufficient, in fact this is the default if no key generator is defined.
 *
 * With replicated or distributed caches, writes to the cache update other cluster nodes and when another node reads
 * the entry it uses the key to populate an entry in its own slot table.
 *
 * So with all that, now it's possible to go from a Uid and typeName (which all arjuna records contain)
 * to the SlotStoreKey to the slot index (via slotIdIndex) to the actual data returned from the backing slots
 * read(byte[] read(int slot) method which does the actual infinispan cache lookup to get the data).
 */
public class InfinispanSlots implements BackingSlots {
    private byte[][] slots = null;
    private Cache<byte[], byte[]> cache;
    private InfinispanSlotKeyGenerator infinispanSlotKeyGenerator;

    /**
     * Overrides {@link BackingSlots#init(SlotStoreEnvironmentBean)} and has the same meaning
     * @param slotStoreConfig the config to use for the initialisation
     * @throws IOException if the cache operation threw an exception
     */
    @Override
    public void init(SlotStoreEnvironmentBean slotStoreConfig) throws IOException {
        InfinispanStoreEnvironmentBean config;

        if (slotStoreConfig instanceof InfinispanStoreEnvironmentBean) {
            config = (InfinispanStoreEnvironmentBean) slotStoreConfig;
        } else {
            config = BeanPopulator.getDefaultInstance(InfinispanStoreEnvironmentBean.class);
        }

        slots = new byte[slotStoreConfig.getNumberOfSlots()][];
        infinispanSlotKeyGenerator = config.getSlotKeyGenerator();

        if (infinispanSlotKeyGenerator == null) {
            infinispanSlotKeyGenerator = new InfinispanSlotKeyGenerator() {
                @Override
                public byte[] generateUniqueKey(int index) {
                    return new Uid().getBytes();
                }

                @Override
                public void init(InfinispanStoreEnvironmentBean ignore) {
                }
            };
        }
        infinispanSlotKeyGenerator.init(config);

        try {
            // set up the slot keys
            String group = config.getGroupName();

            cache = config.getCache();

            if (group != null)
                load(cache.getAdvancedCache().getGroup(group).keySet());
            else
                load(cache.keySet());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Overrides {@link BackingSlots#write(int, byte[], boolean)}
     * The write semantics depend on how the cache was configured {@link InfinispanStoreEnvironmentBean#setCache(Cache)}
     *
     * Overrides @link {BackingSlots} and has the same meaning
     *
     * @param slot the index, from 0 to config numberOfSlots-1
     * @param data the content.
     * @param sync not used because the sync behaviour depends on the cache configuration
     *
     * @throws IOException if the cache operation threw an exception
     */
    @Override
    public void write(int slot, byte[] data, boolean sync) throws IOException {
        try {
            cache.put(slots[slot], data);
            // With replicated or distributed caches, writes to the cache update other cluster nodes and when
            // another node reads the entry it uses the key to populate an entry in its own slot table.
            // cache.get(slots[slot]) will cause SlotStore to add the entry to its SlotStoreIndex
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Overrides {@link BackingSlots#read(int)}
     * The read semantics depend on how the cache ({@link InfinispanStoreEnvironmentBean#setCache(Cache)}) was configured
     *
     * @param slot the index, from 0 to config numberOfSlots-1
     *
     * @throws IOException if the cache operation threw an exception
     */
    @Override
    public byte[] read(int slot) throws IOException {
        try {
            return cache.get(slots[slot]);
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
            // remove an entry from the entire cache system (it's important to use this method instead of evict)
            cache.remove(slots[slot]);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void load(Set<byte[]> keys) throws IOException {
        int i = 0;

        for (byte[] key : keys) {
            if (i < slots.length) {
                slots[i] = key;
                i += 1;
            } else {
                /*
                 * The number of slots should equal the maximum number of unresolved transactions expected at any given
                 * time, including those in-flight and awaiting recovery.
                 */
                String errorMsg = tsLogger.i18NLogger.get_infinispan_too_few_slots(keys.size(), slots.length);

                throw new IOException(errorMsg);
            }
        }

        // initialise the remaining slots
        while (i < slots.length) {
            slots[i] = infinispanSlotKeyGenerator.generateUniqueKey(i);
            i += 1;
        }
    }
}
