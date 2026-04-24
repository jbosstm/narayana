/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

import org.infinispan.configuration.cache.CacheMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InfinispanReplicatedAndPersistentTest extends InfinispanTestBase {

    /*
     * test infinispan write through caches, ie verify that when a slot store is backed by a persistent
     * infinispan cache store then the slot store still works when *all* nodes in a cluster are restarted
     */
    @Test
    public void testWriteThroughCache() throws IOException {
        String storeDir1 = "infinispan-caches/write-through1";
        String storeDir2 = "infinispan-caches/write-through2";
        Store store1 = new Store(createCacheManager("node1", CacheMode.REPL_SYNC, -1, null, storeDir1, false), null, "node1", storeDir1);
        Store store2 = new Store(createCacheManager("node2", CacheMode.REPL_SYNC, -1, null, storeDir2, false), null, "node2", storeDir2);

        store1.manager().getCache(CLUSTER_NAME).clear(); // start clean
        store2.manager().getCache(CLUSTER_NAME).clear(); // start clean

        store1.config().setSlotKeyGeneratorClassName(ClusterMemberId.class.getName());
        store2.config().setSlotKeyGeneratorClassName(ClusterMemberId.class.getName());
        Assertions.assertTrue(store1.config().getStoreDir().endsWith("write-through1/node1"));
        Assertions.assertTrue(store2.config().getStoreDir().endsWith("write-through2/node2"));

        // create two key value pairs
        record KVPair(byte[] key, byte[] value) {}
        KVPair kv1 = new KVPair("key1".getBytes(), "value1".getBytes());
        KVPair kv2 = new KVPair("key2".getBytes(), "value2".getBytes());

        // populate the first infinispan cache with them
        store1.cache().put(kv1.key, kv1.value);
        store1.cache().put(kv2.key, kv2.value);

        // make sure to start the store after updating the infinispan caches, then starting the store will
        // load the keys just were added.
        store1.start();
        store2.start();

        // and verify it replicates to cache2
        Assertions.assertArrayEquals(kv1.value, store1.cache().get(kv1.key));
        Assertions.assertArrayEquals(kv1.value, store2.cache().get(kv1.key));
        Assertions.assertArrayEquals(kv2.value, store1.cache().get(kv2.key));
        Assertions.assertArrayEquals(kv2.value, store2.cache().get(kv2.key));

        // and check that the infinispan filesystem persistence store was created at both nodes
        Assertions.assertTrue(Files.exists(Paths.get(store1.config().getStoreDir())), "infinispan persistence store 1 was not created");
        Assertions.assertTrue(Files.exists(Paths.get(store2.config().getStoreDir())), "infinispan persistence store 2 was not created");

        // verify that the slot stores at each node have the same values
        // note that the slots backend is internal, but it's still useful to test it directly
        byte[] value1 = store1.slots().read(0);
        byte[] value2 = store2.slots().read(0);
        byte[] value3 = store1.slots().read(1);
        byte[] value4 = store2.slots().read(1);

        Assertions.assertArrayEquals(value1, value2);
        Assertions.assertArrayEquals(value3, value4);

        // stop both nodes
        store1.stop();
        store2.stop();

        /*
         * All nodes are now down so we know none of the data can be in-memory.
         * Restart them and verify that they repopulated their caches
         * from the filesystem backing stores on their respective nodes
         */
        store1 = new Store(createCacheManager("node1", CacheMode.REPL_SYNC, -1, null, storeDir1, false), null, "node1", storeDir1);
        store2 = new Store(createCacheManager("node2", CacheMode.REPL_SYNC, -1, null, storeDir2, false), null, "node2", storeDir2);

        store1.config().setSlotKeyGeneratorClassName(ClusterMemberId.class.getName());
        store2.config().setSlotKeyGeneratorClassName(ClusterMemberId.class.getName());
        store1.start();
        store2.start();

        Assertions.assertArrayEquals(kv1.value, store1.cache().get(kv1.key));
        Assertions.assertArrayEquals(kv1.value, store2.cache().get(kv1.key));
        Assertions.assertArrayEquals(kv2.value, store1.cache().get(kv2.key));
        Assertions.assertArrayEquals(kv2.value, store2.cache().get(kv2.key));

        // and similarly check that the slot stores are repopulated correctly
        Assertions.assertArrayEquals(store1.slots().read(0), store2.slots().read(0));
        Assertions.assertArrayEquals(store1.slots().read(1), store2.slots().read(1));

        store1.slots().clear(0, true);
        store1.slots().clear(1, true);

        store1.stop();
        store2.stop();
    }
}
