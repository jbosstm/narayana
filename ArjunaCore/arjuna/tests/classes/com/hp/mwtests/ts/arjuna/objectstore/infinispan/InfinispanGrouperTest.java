/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanSlotKeyGenerator;
import org.infinispan.Cache;
import org.infinispan.commons.marshall.WrappedByteArray;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.distribution.group.Grouper;
import org.infinispan.metadata.EmbeddedMetadata;
import org.infinispan.metadata.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfinispanGrouperTest extends InfinispanTestBase {

    @BeforeAll
    static void beforeAll() {
        arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(true);
        Assertions.assertTrue(arjPropertyManager.getCoordinatorEnvironmentBean().isCommitOnePhase());
    }

    @Test
    public void testUserDefinedKeyGenerator() throws IOException {
        Store store1 = new Store(createCacheManager("node1", CacheMode.REPL_SYNC, -1, null, false, false), null, "node1");

        // user defined SlotKeyGenerator instance (Arjuna ClassLoadingUtility doesn't support construct of anonymous classes)
        UserDefinedSlotKeyGenerator slotKeyGenerator = new UserDefinedSlotKeyGenerator();

        store1.config().setSlotKeyGenerator(slotKeyGenerator);
        store1.start();
        Assertions.assertTrue(slotKeyGenerator.initCalled);
        Assertions.assertTrue(slotKeyGenerator.generateCalled);

        RecoveryStore recoveryStore = startRecoveryStore(store1.config());

        Uid uid = new Uid();
        AtomicAction action = new AtomicAction(uid);
        Participant participant = new Participant();

        action.begin();
        action.add(participant);
        int res = action.commit(false);

        Assertions.assertEquals(ActionStatus.COMMITTED, res);

        Assertions.assertEquals(0, store1.manager().getCache(CLUSTER_NAME).size());

        store1.stop();
        recoveryStore.stop();
    }

//TODO    @Test
    public void testDistributedMode() throws IOException, ObjectStoreException {
        class RecoveryGrouper implements Grouper<WrappedByteArray> {
            static final Pattern CB_DELIMITER_REGEX = Pattern.compile("\\{(\\w+)\\}");

            @Override
            public Object computeGroup(WrappedByteArray key, Object group) {
                String k = new String(key.getBytes());

                Matcher matcher = CB_DELIMITER_REGEX.matcher(k);
                if (matcher.find()) {
                    return matcher.group(1);
                }

                return "";
            }
            @Override
            public Class<WrappedByteArray> getKeyType() {
                return WrappedByteArray.class; //byte[].class;
            }
        }
        String minorGroup = "minorGroup";
        String majorGroup = "majorGroup";
        RecoveryGrouper recoveryGrouper = new RecoveryGrouper();

        /*
         * Use Infinispan Distributed Mode which provides better scalability than does Replication Mode:
         * - with replicated caches all nodes in a cluster hold all keys,
         * - with distributed caches a number of copies are maintained to provide redundancy and fault tolerance.
         * Distributed caches provide improved scalability and are able to transparently locate keys across the cluster
         * providing for fast local read access of state that is stored remotely. In this test we verify how keys
         * are distributed. Like replication mode it still provides strong consistency.
         *
         * Note that Infinispan has good support for partition handling in both distributed and replicated cache modes
         * (TODO include some tests).
         * This allows for more fine-grained control of a cache’s behaviour when a split brain occurs.
         * Note that there is a dedicated ConflictManager component so that conflicts on cache entries can be
         * automatically resolved on-demand by users and/or automatically during partition merges.
         */
        Store store1 = new Store(createCacheManager("node1", CacheMode.DIST_SYNC, 1, recoveryGrouper, false, false), minorGroup, "node1");
        Store store2 = new Store(createCacheManager("node2", CacheMode.DIST_SYNC, 1, recoveryGrouper, false, false), majorGroup, "node2");
        Store store3 = new Store(createCacheManager("node3", CacheMode.DIST_SYNC, 1, recoveryGrouper, false, false), majorGroup, "node3");

        store1.config().setSlotKeyGeneratorClassName(ClusterMemberId.class.getName());
        store2.config().setSlotKeyGeneratorClassName(ClusterMemberId.class.getName());
        store3.config().setSlotKeyGeneratorClassName(ClusterMemberId.class.getName());

        store1.start();
        store2.start();
        store3.start();

        RecoveryStore recoveryStore = startRecoveryStore(store1.config());

        Uid uid = new Uid();
        AtomicAction action = new AtomicAction(uid);
        Participant participant = new Participant();

        action.begin();
        action.add(participant);

        // don't delete the log so we can check the cache entries
        int res = action.commit(true);

        Assertions.assertEquals(ActionStatus.H_HAZARD, res);
        Assertions.assertEquals(1, store1.manager().getCache(CLUSTER_NAME).size());

        // verify that the group has just the one key
        Map<Object, Object> group1Keys = store1.manager().getCache(CLUSTER_NAME).getAdvancedCache().getGroup(minorGroup);
        // to recover the keys use new WrappedByteArray((byte[]) g).getBytes()

        Assertions.assertEquals(1, group1Keys.size());
        Assertions.assertEquals(1, store1.manager().getCache(CLUSTER_NAME).size());
        // and that the keys in the group match the keys in the cache
        Assertions.assertTrue(group1Keys.containsKey(store1.manager().getCache(CLUSTER_NAME).keySet().toArray()[0]));

        // clean up the store
        recoveryStore.remove_committed(action.getSavingUid(), action.type());

        Assertions.assertEquals(0, store1.manager().getCache(CLUSTER_NAME).size());

        store1.stop();
        store2.stop();
        store3.stop();
        recoveryStore.stop();
    }

    @Test
    public void testGrouperMultiple() throws IOException, ObjectStoreException {
        class RecoveryGrouper implements Grouper<WrappedByteArray> {
            static final Pattern CB_DELIMITER_REGEX = Pattern.compile("\\{(\\w+)\\}");

            @Override
            public Object computeGroup(WrappedByteArray key, Object group) {
                String k = new String(key.getBytes());

                Matcher matcher = CB_DELIMITER_REGEX.matcher(k);
                if (matcher.find()) {
                    return matcher.group(1);
                }

                return "";
            }
            @Override
            public Class<WrappedByteArray> getKeyType() {
                return WrappedByteArray.class; //byte[].class;
            }
        }
        String minorGroup = "minorGroup";
        String majorGroup = "majorGroup";
        RecoveryGrouper recoveryGrouper = new RecoveryGrouper();

        /*
         * Use Infinispan Distributed Mode which provides better scalability than does Replication Mode:
         * - with replicated caches all nodes in a cluster hold all keys,
         * - with distributed caches a number of copies are maintained to provide redundancy and fault tolerance.
         * Distributed caches provide improved scalability and are able to transparently locate keys across the cluster
         * providing for fast local read access of state that is stored remotely. In this test we verify how keys
         * are distributed. Like replication mode it still provides strong consistency.
         *
         * Note that Infinispan has good support for partition handling in both distributed and replicated cache modes
         * (TODO include some tests).
         * This allows for more fine-grained control of a cache’s behaviour when a split brain occurs.
         * Note that there is a dedicated ConflictManager component so that conflicts on cache entries can be
         * automatically resolved on-demand by users and/or automatically during partition merges.
         */
        Store store1 = new Store(createCacheManager("node1", CacheMode.DIST_SYNC, 3, recoveryGrouper, false, false), minorGroup, "node1");
        Store store2 = new Store(createCacheManager("node2", CacheMode.DIST_SYNC, 3, recoveryGrouper, false, false), majorGroup, "node2");
        Store store3 = new Store(createCacheManager("node3", CacheMode.DIST_SYNC, 3, recoveryGrouper, false, false), majorGroup, "node3");

        store1.start();
        store2.start();
        store3.start();

        RecoveryStore recoveryStore = startRecoveryStore(store1.config());

        Uid uid = new Uid();
        AtomicAction action = new AtomicAction(uid);
        Participant participant = new Participant();

        action.begin();
        action.add(participant);

        // don't delete the log so we can check the cache entries
        int res = action.commit(true);

        Assertions.assertEquals(ActionStatus.H_HAZARD, res);
        Assertions.assertEquals(1, store1.manager().getCache(CLUSTER_NAME).size());

        // verify that the group has just the one key
        Map<Object, Object> group1Keys = store1.manager().getCache(CLUSTER_NAME).getAdvancedCache().getGroup(minorGroup);
        // to recover the keys use new WrappedByteArray((byte[]) g).getBytes()

        Assertions.assertEquals(1, group1Keys.size());
        Assertions.assertEquals(1, store1.manager().getCache(CLUSTER_NAME).size());
        // and that the keys in the group match the keys in the cache
        Assertions.assertTrue(group1Keys.containsKey(store1.manager().getCache(CLUSTER_NAME).keySet().toArray()[0]));

        // clean up the store
        recoveryStore.remove_committed(action.getSavingUid(), action.type());

        Assertions.assertEquals(0, store1.manager().getCache(CLUSTER_NAME).size());

        store1.stop();
        store2.stop();
        store3.stop();
        recoveryStore.stop();
    }

    private void putWithMetadata(Cache<byte[], byte[]> cache, byte[] key, byte[] value) {
        Metadata metadata = new EmbeddedMetadata.Builder().lifespan(1, TimeUnit.DAYS).maxIdle(1, TimeUnit.HOURS).build();

        cache.getAdvancedCache().put(key, value, metadata);
        CacheEntry<byte[], byte[]> entry = cache.getAdvancedCache().getCacheEntry(key);

        Assertions.assertArrayEquals(value, entry.getValue());
        Assertions.assertEquals(TimeUnit.DAYS.toMillis(1), entry.getLifespan());
        Assertions.assertEquals(TimeUnit.HOURS.toMillis(1), entry.getMaxIdle());
    }

    /*
     * test that the tests correctly clean up when multiple cache managers are started
     */
    @Test
    public void testCleanup() throws IOException {
        for (int i =  0; i < 4; i++) {
            String nodeId = "node" + i;
            Store store = new Store(createCacheManager(nodeId, CacheMode.REPL_SYNC, 3, null, false, false), null, nodeId);
            store.start();
            RecoveryStore recoveryStore = startRecoveryStore(store.config());

            AtomicAction action = new AtomicAction(new Uid());
            Participant participant = new Participant();

            action.begin();
            action.add(participant);
            int res = action.commit(false);
            Assertions.assertEquals(ActionStatus.COMMITTED, res);
            recoveryStore.stop();
            store.stop();
        }
    }

    /*
     * test infinispan write through caches, ie verify that when a slot store is backed by a persistent
     * infinispan cache store then the slot store still works when *all* nodes in a cluster are restarted
     */
    @Test
    public void testWithMetadata() throws IOException {

        Store store1 = new Store(createCacheManager("node1", CacheMode.REPL_SYNC, -1, null, true, false), null, "node1");
        Store store2 = new Store(createCacheManager("node2", CacheMode.REPL_SYNC, -1, null, true, false), null, "node2");

        store1.start();
        store2.start();

        // create two key value pairs
        record KVPair(byte[] key, byte[] value) {}
        KVPair kv1 = new KVPair("key1".getBytes(), "value1".getBytes());
        KVPair kv2 = new KVPair("key2".getBytes(), "value2".getBytes());

        // populate the first infinispan cache with them
        putWithMetadata(store1.cache(), kv1.key, kv1.value);
        putWithMetadata(store1.cache(), kv2.key, kv2.value);

        // and verify it replicates to cache2
        Assertions.assertArrayEquals(kv1.value, store1.cache().get(kv1.key));
        Assertions.assertArrayEquals(kv1.value, store2.cache().get(kv1.key));
        Assertions.assertArrayEquals(kv2.value, store1.cache().get(kv2.key));
        Assertions.assertArrayEquals(kv2.value, store2.cache().get(kv2.key));

        // and check that the infinispan filesystem persistence store was created at both nodes
        Assertions.assertTrue(Files.exists(store1.path()), "infinispan persistence store 1 was not created");
        Assertions.assertTrue(Files.exists(store2.path()), "infinispan persistence store 2 was not created");

        // verify that the slot stores at each node have the same values
        byte[] value1 = store1.slots().read(0);
        byte[] value2 = store2.slots().read(0);
        byte[] value3 = store1.slots().read(1);
        byte[] value4 = store2.slots().read(1);

        Assertions.assertArrayEquals(value1, value2);
        Assertions.assertArrayEquals(value3, value4);

        store1.stop();
        store2.stop();
    }

    public static class UserDefinedSlotKeyGenerator implements InfinispanSlotKeyGenerator {
        boolean generateCalled;
        boolean initCalled;

        @Override
        public byte[] generateUniqueKey(int index) {
            generateCalled = true;
            return new Uid().getBytes();
        }

        @Override
        public void init(InfinispanStoreEnvironmentBean config) {
            initCalled = true;
        }
    }
}
