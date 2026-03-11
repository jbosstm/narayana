package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.infinispan.Cache;
import org.infinispan.commons.marshall.WrappedByteArray;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.conflict.MergePolicy;
import org.infinispan.distribution.group.Grouper;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.partitionhandling.PartitionHandling;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class InfinispanTestBase {
    // the name of the cluster and the shared cache used for the object store
    static final String CLUSTER_NAME = "objectStoreCluster";
    // location of the file system store (with surefire it will be the build directory)
    static final String STORE_DIR = System.getProperty("java.io.tmpdir") + "/infinispan-caches";

    // record bringing together various data related to a slot store instance
    record Store(DefaultCacheManager manager, // the infinispan cache manager
                 String groupName,
                 String nodeName, // name of a cluster node
                 Cache<byte[], byte[]> cache, // the cache for this cluster node
                 InfinispanStoreEnvironmentBean config, // config for the slot store on this node
                 InfinispanSlots slots, // slot store
                 Path path) { // filesystem path where the persistent cache is located
        public Store(DefaultCacheManager manager, String groupName, String nodeName) {
            this(
                    manager,
                    groupName,
                    nodeName,
                    manager.getCache(CLUSTER_NAME),
                    // manager.administration().getOrCreateCache(CLUSTER_NAME, manager.getDefaultCacheConfiguration()),
                    new InfinispanStoreEnvironmentBean(),
                    new InfinispanSlots(),
                    Paths.get(STORE_DIR + "/" + nodeName)
            );
            config.setNodeAddress(manager.getNodeAddress());
            config.setGroupName(groupName);
            config.setCacheName(cache.getName());
            config.setCache(cache);
            config.setBackingSlots(slots);
            config.setSlotKeyGeneratorClassName(ClusterMemberId.class.getName()); // default key generator
        }

        /*
         * Stop the cache manager otherwise the network endpoints won't be closed correctly.
         * This is particularly important with jgroups.
         */
        public void stop() {
            cache().stop();
            manager.stop();
        }

        public void start() throws IOException {
            slots().init(config());
        }
    }

    static DefaultCacheManager createCacheManager(String nodeName,
                                                   CacheMode cacheMode,
                                                   int numOwners, // used with CacheMode.CacheMode
                                                   Grouper<WrappedByteArray> grouper,
                                                   boolean persistence,
                                                   boolean partitionResilience) {
        GlobalConfigurationBuilder globalConfig = GlobalConfigurationBuilder.defaultClusteredBuilder();

        globalConfig.transport().nodeName(nodeName).machineId(nodeName).clusterName(CLUSTER_NAME);

        var manager = new DefaultCacheManager(globalConfig.build());
        var storeDir = String.format("%s/%s", STORE_DIR, nodeName);

        // Define the replicated cache configuration
        ConfigurationBuilder cacheConfig = new ConfigurationBuilder();
        cacheConfig
                .clustering()
                .cacheMode(cacheMode)
                .remoteTimeout(5, TimeUnit.SECONDS);

        if (numOwners > 0) {
            cacheConfig
                    .clustering()
                    .hash()
                    .numOwners(numOwners); // number of cluster-wide replicas for each cache entry
        }
        if (grouper != null) {
            cacheConfig
                    .clustering()
                    .hash().groups().enabled().addGrouper(grouper);
        }
        if (persistence) {
            cacheConfig
                    .clustering()
                    .persistence()
                    .passivation(false)
                    .addSoftIndexFileStore()
                    .dataLocation(storeDir + "/data")
                    .indexLocation(storeDir + "/index")
                    .shared(false);
        }
        if (partitionResilience) {
            cacheConfig
                    .clustering()
                    .partitionHandling()
                    .whenSplit(PartitionHandling.DENY_READ_WRITES)
                    .mergePolicy(MergePolicy.PREFERRED_ALWAYS);
        }

        manager.defineConfiguration(CLUSTER_NAME, cacheConfig.build());

        return manager;
    }

    /**
     * start a new recovery manager, shutting down the current one if it is running
     * @param bean config for the recovery manager
     * @return the new store
     */
    static RecoveryStore startRecoveryStore(InfinispanStoreEnvironmentBean bean) {
        StoreManager.shutdown(); // remove any existing store

        // tell the recovery manager that we are using the slot store
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).
                setObjectStoreType(SlotStoreAdaptor.class.getName());

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

    /*
     * update the slot store environment bean which normally gets set once per VM, but we need different
     * values of the SlotStoreEnvironmentBean for various tests
     */
    static private void replaceEnvironmentBean(SlotStoreEnvironmentBean bean) throws Throwable {
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

    /*
     * reset the AtomicActionRecoveryModule
     * this is useful when restarting recovery because the AARM initialises a private static variable
     * RecoveryStore _recoveryStore which needs to be reset when testing different recovery configurations
     */
    static void resetAtomicActionRecoveryModule() throws Throwable {//RecoveryStore
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                AtomicActionRecoveryModule.class,
                MethodHandles.lookup()
        );

        // Get a VarHandle for the private static field
        VarHandle varHandle = lookup.findStaticVarHandle(
                AtomicActionRecoveryModule.class,
                "_recoveryStore",
                RecoveryStore.class
        );

        varHandle.set((Object) null);
    }

    boolean containsAtomicAction(RecoveryStore recoveryStore, AtomicAction aa) {
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

    static class Participant extends AbstractRecord {
        @Override
        public int typeIs() {
            return 0;
        }

        @Override
        public Object value() {
            return null;
        }

        @Override
        public void setValue(Object o) {
        }

        @Override
        public int nestedAbort() {
            return 0;
        }

        @Override
        public int nestedCommit() {
            return 0;
        }

        @Override
        public int nestedPrepare() {
            return 0;
        }

        @Override
        public int topLevelAbort() {
            return 0;
        }

        @Override
        public int topLevelCommit() {
            return 0;
        }

        @Override
        public int topLevelPrepare() {
            return 0;
        }

        @Override
        public void merge(AbstractRecord a) {
        }

        @Override
        public void alter(AbstractRecord a) {
        }

        @Override
        public boolean shouldAdd(AbstractRecord a) {
            return true; // record should be added to the intentions list
        }

        @Override
        public boolean shouldAlter(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldMerge(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldReplace(AbstractRecord a) {
            return false;
        }
    }
}
