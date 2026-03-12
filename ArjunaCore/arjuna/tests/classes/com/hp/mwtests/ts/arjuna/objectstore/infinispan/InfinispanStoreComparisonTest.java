/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore;
import com.arjuna.ats.internal.arjuna.objectstore.TwoPhaseVolatileStore;
import com.arjuna.ats.internal.arjuna.objectstore.slot.DiskSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.MappedDiskSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanSlots;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;
import io.narayana.perf.Measurement;
import io.narayana.perf.WorkerWorkload;
import org.infinispan.commons.marshall.WrappedByteArray;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.distribution.group.Grouper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class InfinispanStoreComparisonTest extends InfinispanTestBase {

    RecoveryStore recoveryStore = null;
    Store store = null;
    record MeasurementConfig(String storeName, int threadCount, int batchSize, int warmUpCount) {}

    MeasurementConfig setup(String storeType) throws CoreEnvironmentBeanException {
        int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
        int BATCH_SIZE = 4000; // each thread will run this number of transactions
        int WARMUP_COUNT = 2;
        BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class).setNodeIdentifier("1");
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreType(storeType);

        return new MeasurementConfig(storeType, THREAD_COUNT, BATCH_SIZE, WARMUP_COUNT);
    }

    private Store getStore(String nodeName, CacheMode mode, int numOwners, Grouper<WrappedByteArray> grouper, boolean persistence, boolean partitionResilience, String groupName) {
        store = new Store(createCacheManager(nodeName, mode, numOwners, grouper, persistence, partitionResilience), groupName, nodeName);
        recoveryStore = startRecoveryStore(store.config());
        return store;
    }

    @Test
    public void testVolatile() throws CoreEnvironmentBeanException {
        arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(false);
        MeasurementConfig runConfig = setup(TwoPhaseVolatileStore.class.getName());

        measure(runConfig);
    }

    @Test
    public void testFS() throws CoreEnvironmentBeanException {
        MeasurementConfig runConfig = setup(ShadowNoFileLockStore.class.getName());

        measure(runConfig);
    }

    @Test
    public void testDiskSlots() throws CoreEnvironmentBeanException {
        MeasurementConfig runConfig = setup(SlotStoreAdaptor.class.getName());
        SlotStoreEnvironmentBean configBean = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);

        configBean.setBackingSlotsClassName(DiskSlots.class.getName());

        measure(runConfig, DiskSlots.class.getName());
    }

    @Test
    public void testInfinispan() throws IOException, CoreEnvironmentBeanException {
        MeasurementConfig runConfig = setup(InfinispanSlots.class.getName());
        String nodeId = "node1";
        Store store = getStore(nodeId, CacheMode.REPL_SYNC, 3, null, true, false, null);

        store.start();

        recoveryStore = startRecoveryStore(store.config());

        measure(runConfig);
    }

    @Test
    public void testMappedDiskSlots() throws CoreEnvironmentBeanException {
        MeasurementConfig runConfig = setup(SlotStoreAdaptor.class.getName());
        SlotStoreEnvironmentBean configBean = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);

        configBean.setBackingSlotsClassName(MappedDiskSlots.class.getName());

        measure(runConfig, MappedDiskSlots.class.getName());
    }

    void measure(MeasurementConfig config) {
        measure(config, config.storeName);
    }

    void measure(MeasurementConfig config, String storeName) {
        int numberOfTransactions = config.threadCount * config.batchSize;
        WorkerWorkload<Void> workerWorkload = getWorker();
        long start = System.currentTimeMillis();
        Measurement measurement = new Measurement.Builder(getClass().getName())
                .maxTestTime(0L)
                .numberOfCalls(numberOfTransactions)
                .numberOfThreads(config.threadCount)
                .batchSize(config.batchSize)
                .numberOfWarmupCalls(config.warmUpCount)
                .build()
                .measure(workerWorkload);

        Assert.assertEquals(0, measurement.getNumberOfErrors());
        Assert.assertFalse(measurement.getInfo(), measurement.shouldFail());

        System.out.printf("== %s: throughput: %f (%d ms %d threads %d actions)%n", storeName,
                (float) (numberOfTransactions / (measurement.getTotalMillis() / 1000.0)),
                measurement.getTotalMillis(),
                config.threadCount,
                numberOfTransactions);
//        System.out.println("\ttime for " + numberOfTransactions + " write transactions is " + measurement.getTotalMillis());
//        System.out.println("\tnumber of transactions: " + numberOfTransactions);
    }

    WorkerWorkload<Void> getWorker() {
        return new WorkerWorkload<Void>() {
            @Override
            public Void doWork(Void context, int batchSize, Measurement<Void> config) {
                for (int i = 0; i < batchSize; i++) {
                    try {
                        AtomicAction A = new AtomicAction();

                        A.begin();

                        A.add(new BasicRecord());
                        A.add(new BasicRecord());

                        A.commit();
                    } catch (Exception e) {
                        if (config.getNumberOfErrors() == 0)
                            e.printStackTrace();

                        config.incrementErrorCount();
                    }
                }

                return context;
            }

            @Override
            public void finishWork(Measurement<Void> measurement) {
            }
        };
    }
}