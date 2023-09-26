/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.performance;

import io.narayana.perf.Measurement;
import io.narayana.perf.WorkerWorkload;
import org.junit.Assert;
import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator;
import com.hp.mwtests.ts.arjuna.resources.SyncRecord;

public class Performance3
{
    @Test
    public void test()
    {
        int warmUpCount = 10;
        int numberOfTransactions = 1000000;
        int threadCount =  1;
        int batchSize = 100;

        Measurement measurement = new Measurement.Builder(getClass().getName() + "_test1")
                .maxTestTime(0L).numberOfCalls(numberOfTransactions)
                .numberOfThreads(threadCount).batchSize(batchSize)
                .numberOfWarmupCalls(warmUpCount).build().measure(worker);

        Assert.assertEquals(0, measurement.getNumberOfErrors());
        Assert.assertFalse(measurement.getInfo(), measurement.shouldFail());

        System.out.printf("%s%n", measurement.getInfo());
        System.err.println("TPS: " + measurement.getThroughput());
    }

    WorkerWorkload<Void> worker = new WorkerWorkload<Void>() {
        @Override
        public Void doWork(Void context, int batchSize, Measurement<Void> config) {
            for (int i = 0; i < batchSize; i++) {
                TwoPhaseCoordinator tx = new TwoPhaseCoordinator();

                tx.start();

                tx.addSynchronization(new SyncRecord());

                tx.end(true);
            }

            return context;
        }

        @Override
        public void finishWork(Measurement<Void> measurement) {
        }
    };
}