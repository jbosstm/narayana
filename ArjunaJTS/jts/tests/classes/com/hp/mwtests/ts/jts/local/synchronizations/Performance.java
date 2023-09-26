/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.synchronizations;

import static org.junit.Assert.fail;

import io.narayana.perf.Measurement;
import io.narayana.perf.Worker;
import org.junit.Assert;
import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CosTransactions.*;
import org.omg.CORBA.ORBPackage.InvalidName;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jts.orbspecific.resources.demosync;

public class Performance
{
    @Test
    public void test() throws Exception
    {
        int maxTestTime = 0;
        int numberOfCalls = 1000;
        int warmUpCount = 0;
        int numberOfThreads = 1;
        int batchSize = numberOfCalls;

        Measurement measurement = new Measurement.Builder(getClass().getName() + "_test1")
                .maxTestTime(0L).numberOfCalls(numberOfCalls)
                .numberOfThreads(numberOfThreads).batchSize(batchSize)
                .numberOfWarmupCalls(warmUpCount).build().measure(worker, worker);

        Assert.assertEquals(0, measurement.getNumberOfErrors());
        Assert.assertFalse(measurement.getInfo(), measurement.shouldFail());

        System.out.printf("%s%n", measurement.getInfo());
        System.out.println("Average time for empty transaction = " + measurement.getTotalMillis() / (float) numberOfCalls);
        System.out.printf("TPS: %f%n", measurement.getThroughput());
    }

    Worker<Void> worker = new Worker<Void>() {
        ORB myORB = null;
        RootOA myOA = null;
        org.omg.CosTransactions.Current current;
        demosync sync = null;

        private void initCorba() {
            myORB = ORB.getInstance("test");

            myOA = OA.getRootOA(myORB);

            myORB.initORB(new String[] {}, null);

            try {
                myOA.initOA();
            } catch (InvalidName invalidName) {
                fail(invalidName.getMessage());
            }

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);
        }

        @Override
        public void init() {
            initCorba();
            current = OTSManager.get_current();
            sync = new demosync(false);
        }

        @Override
        public void fini() {
            myOA.shutdownObject(sync);

            myOA.destroy();
            myORB.shutdown();
        }

        @Override
        public Void doWork(Void context, int batchSize, Measurement<Void> measurement) {
            for (int i = 0; i < batchSize; i++)
            {
                try {
                    current.begin();

                    Control myControl = current.get_control();
                    Coordinator coord = myControl.get_coordinator();

                    coord.register_synchronization(sync.getReference());

                    current.commit(true);

                } catch (UserException e) {
                    if (measurement.getNumberOfErrors() == 0)
                        e.printStackTrace();

                    measurement.incrementErrorCount();
                    fail("Caught UserException: "+e);
                } catch (SystemException e) {
                    if (measurement.getNumberOfErrors() == 0)
                        e.printStackTrace();

                    measurement.incrementErrorCount();
                    fail("Caught SystemException: " + e);
                }
            }

            return context;
        }

        @Override
        public void finishWork(Measurement<Void> measurement) {
        }
    };
}