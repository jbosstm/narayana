/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CurrentTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.hammer;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.ORBInfo;
import com.arjuna.orbportability.RootOA;
import io.narayana.perf.PerformanceProfileStore;
import io.narayana.perf.Result;

public class PerfHammer
{
    public static final String PERF_HAMMER_CALL_CNT_PROP = "testgroup.jtsremote.perftest.numberOfCalls";
    public static final String PERF_HAMMER_THR_CNT_PROP = "testgroup.jtsremote.perftest.numberOfThreads";
    public static final String PERF_HAMMER_BATCH_SIZE_PROP = "testgroup.jtsremote.perftest.batchSize";
    public static final String PERF_HAMMER_VARIANCE_PROP = "testgroup.jtsremote.perftest.variance";

    private static final int DEFAULT_CALL_CNT = 1000;
    private static final int DEFAULT_THR_CNT = 10;
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final float DEFAULT_VARIANCE = PerformanceProfileStore.getVariance();

    private static Integer getDefaultCallCnt() {
        return PerformanceProfileStore.isFailOnRegression() ?
                DEFAULT_CALL_CNT * 30 : DEFAULT_CALL_CNT;
    }

    private static Integer getDefaultThreadCount() {
        return PerformanceProfileStore.isFailOnRegression() ?
                DEFAULT_THR_CNT * 5 : DEFAULT_THR_CNT;
    }

    public static Integer getCallCount() {
        return Integer.getInteger(PERF_HAMMER_CALL_CNT_PROP, getDefaultCallCnt());
    }

    public static Integer getThreadCount() {
        return Integer.getInteger(PERF_HAMMER_THR_CNT_PROP, getDefaultThreadCount());
    }

    public static Integer getBatchSize() {
        return Integer.getInteger(PERF_HAMMER_BATCH_SIZE_PROP, DEFAULT_BATCH_SIZE);
    }

    public static float getVariance() {
        String varianceFromProperty =  System.getProperty(PERF_HAMMER_VARIANCE_PROP);

        return varianceFromProperty == null ? DEFAULT_VARIANCE : Float.parseFloat(varianceFromProperty);
    }

    public static void main(String[] args) throws Exception
    {
        String gridReference = args[0];

        int numberOfCalls =  getCallCount();
        int threadCount =  getThreadCount();
        int batchSize = getBatchSize();
        float variance =  getVariance();

        ORB myORB = ORB.getInstance("test");
        RootOA myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        GridWorker worker = new GridWorker(myORB, gridReference);
        Result opts = new Result(threadCount, numberOfCalls, batchSize).measure(worker);

        boolean correct = PerformanceProfileStore.checkPerformance("JTSRemote_PerfTest_PerfHammer_" + myORB.orb().getClass().getName(), variance, opts.getThroughput(), true);

        System.out.printf("Test performance (for orb type %s): %d calls/sec (%d invocations using %d threads with %d errors. Total time %d ms)%n",
                ORBInfo.getOrbName(), opts.getThroughput(), opts.getNumberOfCalls(), opts.getThreadCount(),
                opts.getErrorCount(), opts.getTotalMillis());

        System.out.printf("%s%n", (correct ? "Passed" : "Failed"));
    }
}

