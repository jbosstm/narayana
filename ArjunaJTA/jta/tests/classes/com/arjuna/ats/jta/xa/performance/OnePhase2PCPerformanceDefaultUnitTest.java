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
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
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
        javax.transaction.TransactionManager tm;

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
