/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jbossts.star.test;

import io.narayana.perf.Measurement;
import io.narayana.perf.WorkerWorkload;
import org.jboss.jbossts.star.util.TxSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PerformanceTest extends BaseTest {
    private boolean doRealWork = false;

    @BeforeClass
    public static void beforeClass() throws Exception {
        startContainer(TXN_MGR_URL);
    }

    // 2PC commit
    @Test
    public void measureThroughput() throws Exception {
        String info = USE_SPDY ? "SPDY" : USE_SSL ? "SSL" : USE_UNDERTOW ? "UTOW" : "none";
        String metricName = getClass().getName() + "_measureThroughput_" + info;

        int callCount = 1000;
        int warmUpCount = 10;
        int threadCount = 10;
        int batchSize = 50;

        Measurement measurement = new Measurement.Builder(metricName)
                .maxTestTime(0L).numberOfCalls(callCount)
                .numberOfThreads(threadCount).batchSize(batchSize)
                .numberOfWarmupCalls(warmUpCount).build().measure(new RTSWorker());

        Assert.assertEquals(0, measurement.getNumberOfErrors());
        Assert.assertFalse(measurement.getInfo(), measurement.shouldFail());
    }

    private class RTSWorker implements WorkerWorkload<String> {
        private String run2PC(String context, TxSupport txn) throws Exception {

            String pUrl = PURL;
            String[] pid = new String[2];
            String[] pVal = new String[2];

            for (int i = 0; i < pid.length; i++) {
                pid[i] = modifyResource(txn, pUrl, null, "p1", "v1");
                pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");

                Assert.assertEquals(pVal[i], "v1");
            }

            txn.startTx();

            for (int i = 0; i < pid.length; i++) { txn.enlistTestResource(pUrl, false);
                enlistResource(txn, pUrl + "?pId=" + pid[i]);

                modifyResource(txn, pUrl, pid[i], "p1", "v2");
                pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");

                Assert.assertEquals(pVal[i], "v2");
            }

            txn.commitTx();

            for (int i = 0; i < pid.length; i++) {
                pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");
                Assert.assertEquals(pVal[i], "v2");
            }

            return context;
        }

        private void runEmptyTxn(TxSupport txn) throws Exception {
            txn.startTx();
            txn.commitTx();
        }

        @Override
        public String doWork(String context, int niters, Measurement<String> opts) {
            TxSupport txn = new TxSupport();

            for (int i = 0; i < niters; i++) {
                try {
                    if (doRealWork)
                        run2PC(context, txn);
                    else
                        runEmptyTxn(txn);
                } catch (Exception e) {
                    System.out.printf("workload %d failed with %s%n", i, e.getMessage());
                    opts.incrementErrorCount();
                }
            }

            return context;
        }
    }
}
