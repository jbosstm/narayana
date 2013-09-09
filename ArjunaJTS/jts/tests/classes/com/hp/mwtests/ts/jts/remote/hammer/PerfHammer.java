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
import io.narayana.perf.Result;

public class PerfHammer
{
    public static void main(String[] args) throws Exception
    {
        String gridReference = args[0];
        int defaultNumberOfCalls = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        int defaultThreadCount = args.length > 2 ? Integer.parseInt(args[2]) : 100000;
        int defaultBatchSize = args.length > 3 ? Integer.parseInt(args[3]) : 100;

        int numberOfCalls = Integer.getInteger("testgroup.jtsremote.perftest.numberOfCalls", defaultNumberOfCalls);
        int threadCount = Integer.getInteger("testgroup.jtsremote.perftest.numberOfThreads", defaultThreadCount);
        int batchSize = Integer.getInteger("testgroup.jtsremote.perftest.batchSize", defaultBatchSize);

        ORB myORB = ORB.getInstance("test");
        RootOA myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        GridWorker worker = new GridWorker(myORB, gridReference);
        Result opts = new Result(threadCount, numberOfCalls, batchSize).measure(worker);

        System.out.printf("Test performance (for orb type %s): %d calls/sec (%d invocations using %d threads with %d errors. Total time %d ms)%n",
                ORBInfo.getOrbName(), opts.getThroughput(), opts.getNumberOfCalls(), opts.getThreadCount(),
                opts.getErrorCount(), opts.getTotalMillis());

        System.out.println("Passed");
    }
}

