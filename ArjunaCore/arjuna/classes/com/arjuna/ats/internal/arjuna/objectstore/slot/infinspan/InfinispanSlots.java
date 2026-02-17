/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.infinspan;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.infinispan.CacheSet;
import org.infinispan.Cache;

import java.io.IOException;
import java.util.Arrays;

/**
 * A {@link com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStore} implementation backed by an infinispan cache.
 * This store is an in-memory datastore and can be backed be a cluster of infinispan nodes to maintain data
 * availability provided the caches are suitably configured to manage replication of data across the cluster.
 * If the store is to be used with a recovery manager it is important that the environment is configured such that the
 * usual caveats are maintained:
 * - jgroups-raft, or equivalent, is used to ensure strict consistency as required by a CP system
 * - only one recovery manager acts as the leader
 * - if the leader fails automatic failover chooses a new leader while avoiding split brain scenarios
 * - etc.
 * Maintaining these requirements is non-trivial and requires extra support from the environment.
 */
public class InfinispanSlots implements BackingSlots {
    private ClusterMemberId clusterMemberId;
    private byte[][] slots = null;
    private Cache<byte[], byte[]> cache;

    @Override
    public void init(SlotStoreEnvironmentBean slotStoreConfig) throws IOException {
        InfinispanStoreEnvironmentBean config;

        if (slotStoreConfig instanceof InfinispanStoreEnvironmentBean) {
            config = (InfinispanStoreEnvironmentBean) slotStoreConfig;
        } else {
            config = BeanPopulator.getDefaultInstance(InfinispanStoreEnvironmentBean.class);
        }

        slots = new byte[slotStoreConfig.getNumberOfSlots()][];
        clusterMemberId = new ClusterMemberId(config.getNodeAddress(), config.getFailoverId());
        try {
            cache = config.getCache();

            load(cache.keySet()); // set up the slot keys
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(int slot, byte[] data, boolean sync) throws IOException {
        try {
            cache.put(slots[slot], data);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public byte[] read(int slot) throws IOException {
        try {
            return cache.get(slots[slot]);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void clear(int slot, boolean sync) throws IOException {
        try {
            // TODO evicting cache entries has some caveats
            // see https://infinispan.org/docs/stable/apidocs/org/infinispan/Cache.html#evict(K)
            cache.evict(slots[slot]);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void load(CacheSet<byte[]> keys) {
        int i = 0;

        for (byte[] key : keys) {
            if (i < slots.length) {
                slots[i] = key;
                i += 1;
            } else {
                tsLogger.logger.infof("Too many infinispan keys: ignoring remaining keys from slot %d (key=%s)",
                        i, key);
                break;
            }
        }

        // initialise the remaining slots
        while (i < slots.length) {
            slots[i] = clusterMemberId.generateUniqueKey(i);
            i += 1;
        }
    }
}
