/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.xa.performance;

import io.narayana.perf.Measurement;
import io.narayana.perf.Worker;
import org.junit.Assert;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.hp.mwtests.ts.jta.common.SampleOnePhaseResource;
import com.hp.mwtests.ts.jta.common.SampleOnePhaseResource.ErrorType;

public class OnePhase2PCPerformanceDefaultUnitTest
{   
    public static void main (String[] args)
    {
        OnePhase2PCPerformanceDefaultUnitTest obj = new OnePhase2PCPerformanceDefaultUnitTest();

        obj.test();
    }

    @Test
    public void test()
    {
        int warmUpCount = 0;
        int numberOfThreads = 10;
        int batchSize = 1000;
        int numberOfTransactions = numberOfThreads * batchSize;

        Measurement measurement = new Measurement.Builder(getClass().getName() + "_test1")
                .maxTestTime(0L).numberOfCalls(numberOfTransactions)
                .numberOfThreads(numberOfThreads).batchSize(batchSize)
                .numberOfWarmupCalls(warmUpCount).build().measure(worker, worker);

        System.out.printf("%s%n", measurement.getInfo());
        Assert.assertEquals(0, measurement.getNumberOfErrors());
        Assert.assertFalse(measurement.getInfo(), measurement.shouldFail());

        long timeTaken = measurement.getTotalMillis();

        System.out.println("ObjectStore used: "+arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreType());
        System.out.println("time for " + numberOfTransactions + " write transactions is " + timeTaken);
        System.out.println("number of transactions: " + numberOfTransactions);
        System.out.println("throughput: " + (float) (numberOfTransactions / (timeTaken / 1000.0)));
    }

    Worker<Void> worker = new Worker<Void>() {
        jakarta.transaction.TransactionManager tm;

        @Override
        public void init() {
            arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(false);
            tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        }

        @Override
        public void fini() {
        }

        @Override
        public Void doWork(Void context, int batchSize, Measurement<Void> measurement) {
            for (int i = 0; i < batchSize; i++)
            {
                try
                {
                    tm.begin();

                    tm.getTransaction().enlistResource(new SampleOnePhaseResource(ErrorType.none, false));

                    tm.commit();
                }
                catch (Exception e)
                {
                    if (measurement.getNumberOfErrors() == 0)
                        e.printStackTrace();

                    measurement.incrementErrorCount();
                }
            }

            return context;
        }

        @Override
        public void finishWork(Measurement<Void> measurement) {
        }
    };
}