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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Performance.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.synchronizations;

import static org.junit.Assert.fail;

import io.narayana.perf.Measurement;
import io.narayana.perf.PerformanceProfileStore;
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

        Measurement measurement = PerformanceProfileStore.regressionCheck(
                worker, worker, getClass().getName() + "_test1", true, maxTestTime, warmUpCount, numberOfCalls, numberOfThreads, batchSize);

        Assert.assertEquals(0, measurement.getErrorCount());
        Assert.assertFalse(measurement.getInfo(), measurement.isRegression());

        System.out.printf("%s%n", measurement.getInfo());
        System.out.println("Average time for empty transaction = " + measurement.getTotalMillis() / (float) numberOfCalls);
        System.out.printf("TPS: %d%n", measurement.getThroughput());

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
                    if (measurement.getErrorCount() == 0)
                        e.printStackTrace();

                    measurement.incrementErrorCount();
                    fail("Caught UserException: "+e);
                } catch (SystemException e) {
                    if (measurement.getErrorCount() == 0)
                        e.printStackTrace();

                    measurement.incrementErrorCount();
                    fail("Caught SystemException: " + e);
                }
            }

            return context;
        }
    };
}
