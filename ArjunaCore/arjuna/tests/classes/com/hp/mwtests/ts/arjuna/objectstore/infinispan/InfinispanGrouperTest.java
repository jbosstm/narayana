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
import org.infinispan.CacheSet;
import org.infinispan.commons.marshall.WrappedByteArray;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.distribution.DistributionInfo;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.distribution.LocalizedCacheTopology;
import org.infinispan.distribution.group.Grouper;
import org.infinispan.metadata.EmbeddedMetadata;
import org.infinispan.metadata.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.fail;

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

        Assertions.assertEquals(ActionStatus.COMMITTED, action.commit(false));

        Assertions.assertEquals(0, store1.manager().getCache(CLUSTER_NAME).size());

        store1.stop();
        recoveryStore.stop();
    }

    /*
     * Test Infinispan Distributed Mode which provides better scalability than does Replication Mode:
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
    @Test
    public void testDistributedMode() throws IOException, ObjectStoreException {
        // define strategy for grouping keys
        class RecoveryGrouper implements Grouper<WrappedByteArray> {
            static final Pattern CB_DELIMITER_REGEX = Pattern.compile("\\{(\\w+)\\}");

            @Override
            public Object computeGroup(WrappedByteArray key, Object group) {
                // group holds the group as currently computed, or null if no group has been determined yet
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

        RecoveryGrouper recoveryGrouper = new RecoveryGrouper();

        List<Store> stores = new ArrayList<>();
        int numStores = 10;
        int numOwners = 3;
        for (int i = 0; i < numStores; i++) {
            String nodeId = "node" + i;
            String groupId = "group" + i % 2;
            Store store = new Store(createCacheManager(nodeId, CacheMode.DIST_SYNC, numOwners, recoveryGrouper, false, false),
                    groupId, nodeId);
            store.config().setSlotKeyGeneratorClassName(ClusterMemberId.class.getName());
            store.start();
            stores.add(store);
        }

        List<AtomicAction> actions = new ArrayList<>();
        RecoveryStore recoveryStore;
        int NUMBER_OF_ACTIONS_PER_NODE = 2;

        for (Store store : stores) {
            // start NUMBER_OF_ACTIONS_PER_NODE actions from node store.nodeName()
            recoveryStore = startRecoveryStore(store.config());

            for (int i = 0; i < NUMBER_OF_ACTIONS_PER_NODE; i++) {
                Uid uid = new Uid();
                AtomicAction aa = new AtomicAction(uid);
                Participant participant = new Participant();

                aa.begin();
                aa.add(participant);

                // don't delete the log yet because we want to verify that the corresponding cache entries exist
                int res = aa.commit(true);

                Assertions.assertEquals(ActionStatus.H_HAZARD, res);
                actions.add(aa);
                // make sure the log is in the store
                try {
                    recoveryStore.read_committed(aa.getSavingUid(), aa.type());
                } catch (ObjectStoreException e) {
                    fail(e); // record should be available in the recovery store
                }
            }
        }

        // all actions should be readable from any node
        for (Store store : stores) {
            recoveryStore = startRecoveryStore(store.config());

            for (AtomicAction aa : actions) {
                try {
                    recoveryStore.read_committed(aa.getSavingUid(), aa.type());
                } catch (ObjectStoreException e) {
                    fail(e); // record should be available in the recovery store
                }
            }
        }

        // and all caches should contain the same entries
        int totalNumberOfActions = NUMBER_OF_ACTIONS_PER_NODE * stores.size();
        CacheSet<Object> keySet0 = stores.get(0).manager().getCache(CLUSTER_NAME).keySet();
        for (Store store : stores) {
            Assertions.assertEquals(totalNumberOfActions, store.manager().getCache(CLUSTER_NAME).size());
            Assertions.assertTrue(keySet0.containsAll(store.manager().getCache(CLUSTER_NAME).keySet()));
        }

        /*
         * check the key grouping logic
         */

        // we only defined two groups above when creating the stores (new Store(...))
        String group0 = "group0";
        String group1 = "group1";
        Map<Object, Object> g0 = stores.get(0).manager().getCache(CLUSTER_NAME).getAdvancedCache().getGroup(group0);
        Map<Object, Object> g1 = stores.get(1).manager().getCache(CLUSTER_NAME).getAdvancedCache().getGroup(group1);

        // check that the grouping operation returned the expected keys
        g0.forEach((k, v) -> {
            // the key generator was store.config().setSlotKeyGeneratorClassName(ClusterMemberId.class.getName());
            // - use it to decode the key:
            ClusterMemberId id = ClusterMemberId.fromUniqueKey((byte[]) k);
            // verify that the key generator used group0
            Assertions.assertEquals(group0, id.groupId);
            // the value v is the transaction logs' OutputObjectState which is opaque

            // verify that group1 does not contain this key from the group 0 query
            Assertions.assertFalse(g1.containsKey(k));
        });

        /*
         * verify that the entries were distributed across the cluster (we used CacheMode.DIST_SYNC)
         */

        DistributionManager dm = stores.get(0).cache().getAdvancedCache().getDistributionManager();
        // sanity check that the local address of the DistributionManager is node 0
        Assertions.assertEquals(stores.get(0).nodeName(), dm.getCacheTopology().getLocalAddress().getMachineId());
        // look up the DistributionInfo for a particular key to verify that the keys are distributed correctly
        Object aKey = stores.get(0).cache().keySet().toArray()[0];
        DistributionInfo info = dm.getCacheTopology().getDistribution(aKey);

        // Each store was configured with distribution mode (CacheMode.DIST_SYNC) which means data is partitioned
        // across the cluster, with each key stored on a specific number of nodes (we set numOwners to 3 above).
        // So with numOwners = 3 there should be 3 writers (a primary and two backups):
        Assertions.assertEquals(3, info.writeOwners().size());
        Assertions.assertEquals(2, info.writeBackups().size());

        // locate the primary store
        String primary = info.primary().getMachineId(); // we set the machineId equal to the nodeId
        Store primaryStore = stores.stream()
                .filter(s -> s.nodeName().equals(primary))
                .findFirst()
                .orElse(null);

        // and verify it exists
        Assertions.assertNotNull(primaryStore);

        /*
         * clean up the store (any recovery manager in the cluster can be used clean up)
         */

        recoveryStore = startRecoveryStore(stores.get(new Random().nextInt(stores.size())).config());
        for (AtomicAction aa : actions) {
            recoveryStore.remove_committed(aa.getSavingUid(), aa.type());
        }
        // use any store to verify the actions were removed
        recoveryStore = startRecoveryStore(stores.get(new Random().nextInt(stores.size())).config());
        for (AtomicAction aa : actions) {
            try {
                recoveryStore.read_committed(aa.getSavingUid(), aa.type());
                fail("record should not be in the store");
            } catch (ObjectStoreException ignore) {
            }
        }

        /*
         * verify that a new primary is selected if the current one is down
         */

        // stop the primary
        primaryStore.stop();

        String newPrimary = dm.getCacheTopology().getDistribution(aKey).primary().getMachineId();
        // a new primary store should have been chosen since the old one was removed from the cluster when we stopped it:
        Assertions.assertNotEquals(primary, newPrimary);

        for (Store store : stores) {
            // make sure we don't try accessing the cache we just stopped
            if (store.manager().isRunning(store.cache().getName())) {
                Assertions.assertEquals(0, store.manager().getCache(CLUSTER_NAME).size());
                store.stop();
            }
        }

        recoveryStore.stop();
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

    /*
     * test that it's possible to set the lifespan of keys (we don't test actual expiry)
     */
    private void putWithMetadata(Cache<byte[], byte[]> cache, byte[] key, byte[] value) {
        Metadata metadata = new EmbeddedMetadata.Builder().lifespan(1, TimeUnit.DAYS).maxIdle(1, TimeUnit.HOURS).build();

        cache.getAdvancedCache().put(key, value, metadata);
        CacheEntry<byte[], byte[]> entry = cache.getAdvancedCache().getCacheEntry(key);

        Assertions.assertArrayEquals(value, entry.getValue());
        Assertions.assertEquals(TimeUnit.DAYS.toMillis(1), entry.getLifespan());
        Assertions.assertEquals(TimeUnit.HOURS.toMillis(1), entry.getMaxIdle());
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
