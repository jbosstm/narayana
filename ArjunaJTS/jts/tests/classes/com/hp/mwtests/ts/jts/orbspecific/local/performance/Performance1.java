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
 * Copyright (C) 2000, 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Performance1.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.local.performance;

import io.narayana.perf.Measurement;
import io.narayana.perf.Worker;
import io.narayana.perf.WorkerLifecycle;
import org.junit.Assert;
import org.junit.Test;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

public class Performance1
{
    @Test
    public void test()
    {
        int numberOfCalls = 1000;
        int warmUpCount = 10;
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
        System.out.printf("Transactions per second = %f%n", measurement.getThroughput());
    }

    Worker<Void> worker = new Worker<Void>() {
        WorkerLifecycle<Void> lifecycle = new PerformanceWorkerLifecycle<>();
        CurrentImple current;
        boolean doCommit = true;

        @Override
        public void init() {
            lifecycle.init();
            current = OTSImpleManager.current();
        }

        @Override
        public void fini() {
            lifecycle.fini();
        }

        @Override
        public Void doWork(Void context, int batchSize, Measurement<Void> measurement) {
            for (int i = 0; i < batchSize; i++) {
                try {
                    current.begin();

                    if (doCommit)
                        current.commit(true);
                    else
                        current.rollback();
                } catch (org.omg.CORBA.UserException e) {
                    if (measurement.getNumberOfErrors() == 0)
                        e.printStackTrace();

                    measurement.incrementErrorCount();
                }
            }

            return context;
        }
    };
}

