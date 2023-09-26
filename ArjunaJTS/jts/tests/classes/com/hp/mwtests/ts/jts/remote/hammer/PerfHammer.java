/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.hammer;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import io.narayana.perf.Measurement;

public class PerfHammer
{
    private static int getArg(String[] args, int index, int defaultValue) {
        try {
            if (index >= 0 && index < args.length)
                return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            throw new NullPointerException(new PerfHammer().getClass().getName() + "test arguments in the PerformanceProfileStore invalid: " + e.getMessage());
        }

        return defaultValue;

    }

    public static void main(String[] args) throws Exception
    {
        String gridReference = args[0];
        int numberOfCalls = 1000;
        int threadCount = 10;
        int batchSize = 100;
        int warmUpCount = 0;

        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String metricName = "JTSRemote_PerfTest_PerfHammer_" + System.getProperty("org.omg.CORBA.ORBClass",
                myORB.orb().getClass().getName());

        GridWorker worker = new GridWorker(myORB, gridReference);

        Measurement measurement = new Measurement.Builder(metricName)
                .maxTestTime(0L).numberOfCalls(numberOfCalls)
                .numberOfThreads(threadCount).batchSize(batchSize)
                .numberOfWarmupCalls(warmUpCount).build().measure(worker, worker);

        System.out.printf("%s: %d iterations using %d threads with a batch size of %d%n",
                metricName, numberOfCalls, threadCount, batchSize);

        System.out.printf("%s%n", measurement.getInfo());

        System.out.printf("%s%n%s%n", measurement.getInfo(),
                (measurement.shouldFail() || measurement.getNumberOfErrors() != 0 ? "Failed" : "Passed"));
    }
}