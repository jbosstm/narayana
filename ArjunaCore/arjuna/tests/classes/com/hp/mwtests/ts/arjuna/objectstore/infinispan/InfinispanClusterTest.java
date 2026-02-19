/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinspan.InfinispanSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinspan.InfinispanStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.hp.mwtests.ts.arjuna.resources.CrashRecord;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class InfinispanClusterTest {
    // a name for the cluster
    private final String CLUSTER_NAME = "objectStoreCluster";
    // which will be sharing the same object store
    private final String OBJECT_STORE_NAME = "sharedStore";

    private List<DefaultCacheManager> cacheManagers; // the cluster of infinispan cache managers
    private List<InfinispanStoreEnvironmentBean> storeConfigs; // configs for each store in the cluster

    /**
     * creates a cache manager, the intent is to have one such manager per cluster node
     *
     * @param nodeName Name of the current node. This is a friendly name to make logs, etc. make more sense.
     *                 Defaults to a combination of host name and a random number
     *                 (to differentiate multiple nodes on the same host) although any name can be used
     */
    private DefaultCacheManager createCacheManager(String nodeName) {
        GlobalConfigurationBuilder globalConfig = GlobalConfigurationBuilder.defaultClusteredBuilder();

        globalConfig.transport().nodeName(nodeName).clusterName(CLUSTER_NAME);

        var manager = new DefaultCacheManager(globalConfig.build());

        // Define the replicated cache configuration
        ConfigurationBuilder cacheConfig = new ConfigurationBuilder();
        cacheConfig.clustering()
                .cacheMode(CacheMode.REPL_SYNC)  // write operations block until all nodes confirm
                .remoteTimeout(5, TimeUnit.SECONDS); // abort remote calls if timeout is reached

        manager.defineConfiguration(OBJECT_STORE_NAME, cacheConfig.build());

        return manager;
    }

    @BeforeEach
    public void setup() throws IOException {
        // common config for each slot store
        SlotStoreEnvironmentBean slotStoreConfig = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);

        cacheManagers = new ArrayList<>(); // one per simulated node
        storeConfigs = new ArrayList<>(); // one per simulated node

        /*
         * Create a cluster of 3 nodes (an alternative would be to use docker/testcontainers
         * together with @RegisterExtension public static InfinispanServerExtension SERVERS = ...)
         *
         * CacheManagers are normally per JVM, and this test uses them to represent a node in this cluster simulation.
         * In addition, each CacheManager is associated with a recovery store. Ideally the store shutdown code should be
         * responsible for stopping the CacheManager, in this test we do this in the @AfterEach tearDown method.
         */
        int CACHE_SLOT_COUNT = 3;
        for (int i = 0; i < CACHE_SLOT_COUNT; i++) {
            var manager = createCacheManager("node" + i);
            var config = new InfinispanStoreEnvironmentBean();
            var slots = new InfinispanSlots(); // slot store backed by an infinispan cache

            config.setNumberOfSlots(slotStoreConfig.getNumberOfSlots());
            config.setBytesPerSlot(slotStoreConfig.getBytesPerSlot());
            config.setStoreDir(slotStoreConfig.getStoreDir());
            config.setSyncWrites(true);
            config.setSyncDeletes(true);
            config.setBackingSlots(slots);

            config.setCacheName(OBJECT_STORE_NAME);
            config.setNodeAddress(manager.getNodeAddress());
            config.setIgnoreReturnValues(true);
            config.setCache(manager.getCache(OBJECT_STORE_NAME));
            config.setBackingSlots(slots);

            slots.init(config); // can throw IOException

            cacheManagers.add(manager);
            storeConfigs.add(config);
        }

        // tell the recovery manager that we are using the slot store
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).
                setObjectStoreType(SlotStoreAdaptor.class.getName());

        // wait for the cluster to form
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    public void tearDown() {
        if (cacheManagers != null) {
            // CacheManagers are heavyweight objects so make sure they are stopped.
            // This will ensure all caches within its scope are properly stopped as well.
            cacheManagers.forEach(EmbeddedCacheManager::stop);
        }
    }

    /**
     * start a new recovery manager, shutting down the current one if it is running
     * @param bean config for the recovery manager
     * @return the new store
     */
    private RecoveryStore startRecoveryStore(InfinispanStoreEnvironmentBean bean) {
        StoreManager.shutdown(); // remove any existing store

        try {
            /*
             * The intent is to have one recovery store per JVM, and we want to start each with a different config.
             * However, environment bean instances are global to the JVM and can only be set once so replace the current
             * bean using MethodHandles to update the BeanPopulator bean instances map (an alternative could be
             * to update all the fields of the existing bean instance).
             */
            replaceEnvironmentBean(bean);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return StoreManager.getRecoveryStore();
    }

    // update the slot store environment bean
    private void replaceEnvironmentBean(SlotStoreEnvironmentBean bean) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                BeanPopulator.class,
                MethodHandles.lookup()
        );

        // Get a VarHandle for the private static field
        VarHandle varHandle = lookup.findStaticVarHandle(
                BeanPopulator.class,
                "beanInstances",
                ConcurrentMap.class
        );

        ConcurrentMap<String, Object> beanInstances = (ConcurrentMap<String, Object>) varHandle.get();

        beanInstances.put(SlotStoreEnvironmentBean.class.getName(), bean);
    }

    // verify that a value written to the store can be read back correctly
    @Test
    public void test1 () throws Exception {
        class RecordHolder {
            final String typeName;
            final Uid uid;
            final OutputObjectState outputObjectState;
            final String data;
            final boolean typeFound;
            final boolean uidFound;

            public RecordHolder(String typeName, String data) throws IOException {
                this.typeName = typeName;
                this.uid = new Uid();
                this.outputObjectState = new OutputObjectState();
                this.data = data;
                this.typeFound = false;
                this.uidFound = false;

                this.outputObjectState.packString(data);
            }
        }
        RecordHolder[] records = {
                new RecordHolder("StateManager/junit1", "hello1"),
                new RecordHolder("StateManager/junit2", "hello2")
        };

        RecoveryStore recoveryStore = startRecoveryStore(storeConfigs.get(0));

        // write the records to the store and read them back
        for (RecordHolder record: records) {
            // add the record and read it back again
            Assertions.assertTrue(recoveryStore.write_committed(record.uid, record.typeName, record.outputObjectState));
            InputObjectState inputData = recoveryStore.read_committed(record.uid, record.typeName);

            assertEquals(record.data, inputData.unpackString());
        }

        // and verify that the types are present in the store
        Collection<String> types = getAllTypes(recoveryStore);
        for (RecordHolder record: records) {
            Assertions.assertTrue(types.contains(record.typeName));
        }

        // and finally verify that the record was replicated to the other stores
        for (int i = 1; i < storeConfigs.size(); i++) {
            recoveryStore = startRecoveryStore(storeConfigs.get(i));

            for (RecordHolder record : records) {
                InputObjectState inputData = recoveryStore.read_committed(record.uid, record.typeName);
                String datum = inputData.unpackString();

                assertEquals(record.data, datum);
            }
        }

        // clean up
        for (RecordHolder record : records) {
            Assertions.assertTrue(recoveryStore.remove_committed(record.uid, record.typeName));
        }
    }

    // verify that data written to one cluster node can be read back from other nodes even when the original node is down
    @Test
    public void testCrash() {
        RecoveryStore recoveryStore = startRecoveryStore(storeConfigs.get(0));
        AtomicAction A = new AtomicAction();

        A.begin();

        A.add(new CrashRecord(CrashRecord.CrashLocation.NoCrash, CrashRecord.CrashType.Normal));
        A.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit, CrashRecord.CrashType.HeuristicHazard));

        int outcome = A.commit();

        assertEquals(ActionStatus.H_HAZARD, outcome);

        try {
            boolean exists = containsAtomicAction(recoveryStore, A);

            Assertions.assertTrue(exists);
        } catch (Exception e) {
            fail(e);
        }

        // stop all caches (simulates failure of the cluster) to verify that removal of logs will fail
        for (EmbeddedCacheManager m : cacheManagers) {
            m.stop();
        }

        // should still be able to read the types when the cache is unavailable - see SlotStore.getMatchingKeys();
        boolean exists = containsAtomicAction(recoveryStore, A);
        Assertions.assertTrue(exists);

        try {
            // but should not be able to read and write the cache:
            recoveryStore.remove_committed(A.getSavingUid(), A.type());
            fail("should not be able to access a TERMINATED cache");
        } catch (ObjectStoreException expected) {
            // should be ISPN000323 which indicates that the cache is in the TERMINATED state
        }
    }

    private boolean containsAtomicAction(RecoveryStore recoveryStore, AtomicAction aa) {
        InputObjectState ios = new InputObjectState();

        try {
            if (recoveryStore.allObjUids(aa.type(), ios, StateStatus.OS_UNKNOWN)) {
                Uid id;

                do {
                    try {
                        id = UidHelper.unpackFrom(ios);
                        if (id.equals(aa.get_uid())) {
                            return true;
                        }
                    } catch (Exception ex) {
                        return false;
                    }
                }
                while (id.notEquals(Uid.nullUid()));
            }
        } catch (ObjectStoreException ignore) {
        }

        return false;
    }

    private Collection<String> getAllTypes(RecoveryStore store) throws Exception {
        Collection<String> allTypes = new ArrayList<>();
        InputObjectState types = new InputObjectState();
        boolean hasTypes = store.allTypes(types);

        Assertions.assertTrue(hasTypes);

        while (true) {
            try {
                String typeName = types.unpackString();
                assertNotNull(typeName);
                if (typeName.isEmpty())
                    break;
                allTypes.add(typeName);
            } catch (IOException e1) {
                break;
            }
        }

        return allTypes;
    }

    /*
     * simple sanity check to verify that key/value pairs are replicated correctly
     * and are still available when one node fails and are still available from the
     * failed node on restart
     */
    @Test
    public void testCacheReplicationIsSane() {
        // Get cache from each node
        Cache<byte[], byte[]> cache0 = storeConfigs.get(0).getCache();
        Cache<byte[], byte[]> cache1 = storeConfigs.get(1).getCache();
        Cache<byte[], byte[]> cache2 = storeConfigs.get(2).getCache();

        byte[] k1 = "k1".getBytes();
        byte[] k2 = "k2".getBytes();
        byte[] v1 = "v1".getBytes();
        byte[] v2 = "v2".getBytes();

        // put data in node 0
        cache0.put(k1, v1);

        // Verify it's replicated to nodes 1 and 2
        Assertions.assertArrayEquals(v1, cache1.get(k1));
        Assertions.assertArrayEquals(v1, cache2.get(k1));

        // put data in node 1
        cache1.put(k2, v2);

        // verify it's replicated to nodes 0 and 2
        Assertions.assertArrayEquals(v2, cache0.get(k2));
        Assertions.assertArrayEquals(v2, cache2.get(k2));

        // verify all caches have the same size
        assertEquals(2, cache0.size());
        assertEquals(2, cache1.size());
        assertEquals(2, cache2.size());

        // simulate node 0 failure by stopping the manager
        cacheManagers.get(0).stop();
        // and verify that the value is still replicated at nodes 1 and 2
        Assertions.assertArrayEquals(v2, cache1.get(k2));
        Assertions.assertArrayEquals(v2, cache2.get(k2));

        // restart (ie recreate) the "failed" node 0
        var manager = createCacheManager(cacheManagers.get(0).getNodeAddress());
        cacheManagers.set(0, manager);
        // and restart cache 0
        cache0 = manager.getCache(storeConfigs.get(0).getCacheName());
        cache0.start();

        cache0.clear();
        cache1.clear();
        cache2.clear();
    }
}
