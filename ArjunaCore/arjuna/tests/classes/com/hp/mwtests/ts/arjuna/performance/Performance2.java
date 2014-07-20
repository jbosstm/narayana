/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Performance2.java 2342 2006-03-30 13:06:17Z  $
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
    };
}
