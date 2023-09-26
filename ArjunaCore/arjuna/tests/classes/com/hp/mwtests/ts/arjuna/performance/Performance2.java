/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.performance;

import io.narayana.perf.Measurement;
import io.narayana.perf.WorkerWorkload;
import org.junit.Assert;
import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;

public class Performance2
{
    @Test
    public void test()
    {
        int numberOfTransactions = 1000;
        int threads = 10;
        int work = 100;
        int warmUpCount = 0;

        arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(false);

        Measurement measurement = new Measurement.Builder(getClass().getName() + "_test1")
                .maxTestTime(0L).numberOfCalls(numberOfTransactions)
                .numberOfThreads(threads).batchSize(work)
                .numberOfWarmupCalls(warmUpCount).build().measure(worker);

        Assert.assertEquals(0, measurement.getNumberOfErrors());
        Assert.assertFalse(measurement.getInfo(), measurement.shouldFail());

        System.out.printf("%s%n", measurement.getInfo());


        System.out.println("time for " + numberOfTransactions + " write transactions is " + measurement.getTotalMillis());
        System.out.println("number of transactions: " + numberOfTransactions);
        System.out.println("throughput: " + measurement.getThroughput());
    }

    WorkerWorkload<Void> worker = new WorkerWorkload<Void>() {
        @Override
        public Void doWork(Void context, int batchSize, Measurement<Void> config) {
            for (int i = 0; i < batchSize; i++) {
                try {
                    AtomicAction A = new AtomicAction();

                    A.begin();

                    A.add(new BasicRecord());

                    A.commit();
                }
                catch (Exception e) {
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