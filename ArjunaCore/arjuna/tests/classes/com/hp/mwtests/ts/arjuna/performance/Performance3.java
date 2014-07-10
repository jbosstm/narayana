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
 * $Id: Performance3.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.performance;

import io.narayana.perf.PerformanceProfileStore;
import io.narayana.perf.Result;
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

        Result measurement = PerformanceProfileStore.regressionCheck(
                worker, getClass().getName() + "_test1", true, numberOfTransactions, threadCount, batchSize, warmUpCount);

        Assert.assertEquals(0, measurement.getErrorCount());
        Assert.assertFalse(measurement.getInfo(), measurement.isRegression());

        System.out.printf("%s%n", measurement.getInfo());
        System.err.println("TPS: " + measurement.getThroughput());
    }

    WorkerWorkload<Void> worker = new WorkerWorkload<Void>() {
        @Override
        public Void doWork(Void context, int batchSize, Result<Void> config) {
            for (int i = 0; i < batchSize; i++) {
                TwoPhaseCoordinator tx = new TwoPhaseCoordinator();

                tx.start();

                tx.addSynchronization(new SyncRecord());

                tx.end(true);
            }

            return context;
        }
    };
}
