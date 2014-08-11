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
package io.narayana.perf;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class PerformanceTest {
    private BigInteger factorial(int num) {
        BigInteger serialFac = BigInteger.ONE;

        for (int i = 2; i <= num; i++) {
            serialFac = serialFac.multiply(BigInteger.valueOf(i));
        }

        return serialFac;
    }

    /**
     * Calculate a factorial (recall n! = n(n-1)(n-2)...1) in two ways: firstly serially and secondly in parallel
     * (dividing up the computation between a number of threads). Check that the two computations are consistent.
     */
    @Test
    public void testPerformanceTester() {
        FactorialWorker worker = new FactorialWorker();

        int numberOfCalls = 100000;
        int threadCount = 10;
        int batchSize = 10;

        // 1000000!: (total time: 78635 ms versus 1629870 ms) so don't make numberOfCalls to big

        Measurement<BigInteger> measurement = new Measurement<BigInteger>(threadCount, numberOfCalls, batchSize).measure(worker, worker);
        Set<BigInteger> subFactorials = measurement.getContexts();
        BigInteger fac = BigInteger.ONE;

        for (BigInteger bigInteger : subFactorials)
            fac = fac.multiply(bigInteger);

        long start = System.nanoTime();
        BigInteger serialFac = factorial(numberOfCalls);
        long millis = (System.nanoTime() - start) / 1000000L;

        System.out.printf("TestPerformance for %d!: %f calls / second (total time: %d ms versus %d ms)%n",
                measurement.getNumberOfCalls(), measurement.getThroughput(), measurement.getTotalMillis(), millis);

        assertTrue("Factorials not equal", serialFac.equals(fac));

        assertTrue("init method not called", worker.getInitTimemillis() != -1);
        assertTrue("doWork method not called", worker.getWorkTimeMillis() != -1);
        assertTrue("fini method not called", worker.getFiniTimeMillis() != -1);

        assertTrue("init method called after work method", worker.getInitTimemillis() <= worker.getWorkTimeMillis());
        assertTrue("work method called after fini method", worker.getWorkTimeMillis() <= worker.getFiniTimeMillis());
    }

    /**
     * Test that a worker thread is able to cancel an active running test
     */
    @Test
    public void testAbortMeasurement() {
        int numberOfCalls = 1000;
        int threadCount = 10;
        int batchSize = 1;
        WorkerWorkload<String> abortWorker = new WorkerWorkload<String>() {
            private AtomicInteger callCount = new AtomicInteger(0);

            @Override
            public String doWork(String context, int niters, Measurement<String> opts) {
                int sleep = callCount.incrementAndGet();

                if (sleep == 5) {
                    opts.cancel(true);
                    context = "cancelled";
                } else if (sleep > 10) {
                    sleep = 10;
                }

                try {
                    Thread.sleep(sleep * 10);
                } catch (InterruptedException e) {
                }

                return context;
            }

            @Override
            public void finishWork(Measurement<String> measurement) {
            }
        };

        Measurement<String> measurement = new Measurement<>(threadCount, numberOfCalls, batchSize);

        measurement.measure(abortWorker);

        assertTrue("Test should have been aborted", measurement.isCancelled());
        assertEquals("Abort context should have been \"cancelled\"", "cancelled", measurement.getContext());
        assertNotSame("There should have been some workload errors", 0, measurement.getNumberOfErrors());

        System.out.printf("testAbortMeasurement2: %s%n", measurement);
    };

    /**
     * Ensure it is possible to run a single workload
     */
    @Test
    public void testSingleCall() {
        WorkerWorkload<Void> worker = new WorkerWorkload<Void>() {
            @Override
            public Void doWork(Void context, int niters, Measurement<Void> opts) {
                assertEquals("Wrong batch size", 1, niters);
                return context;
            }
            @Override
            public void finishWork(Measurement<Void> measurement) {
            }
        };

        Measurement<Void> config = new Measurement<>(1, 1, 1);

        config.measure(worker);

        assertEquals("Wrong batch size", 1, config.getBatchSize());

        System.out.printf("testSingleCall!: %s%n", config);
    }

    /**
     * Test that a test will be aborted after a fixed timeout
     */
    @Test
    public void testTimeout() {
        final int sleepPeriod = 1;

        int nCalls = 200;
        int nThreads = 2;
        int batchSize = 100;

        int totSleepTime =  sleepPeriod * nCalls;

        int maxTestTime = totSleepTime / nThreads / 2; // set max test time to half the work time

        WorkerWorkload<Void> worker = new WorkerWorkload<Void>() {
            @Override
            public Void doWork(Void context, int niters, Measurement<Void> opts) {
                for (int i = 0; i < niters; i++) {
                    try {
                        Thread.sleep(sleepPeriod);
                    } catch (InterruptedException e) {
                        System.out.printf("%s", e.getMessage());
                    }
                }

                return context;
            }
            @Override
            public void finishWork(Measurement<Void> measurement) {
            }
        };

        Measurement.Builder builder = new Measurement.Builder("testTimeout").
                numberOfCalls(nCalls).
                numberOfThreads(nThreads).
                batchSize(batchSize).
                maxTestTime(maxTestTime);

        Measurement measurement =  builder.build();

        measurement.measure(worker);

        assertTrue(String.format("Test should have timed out after %dms (actual test time was %dms)",
                maxTestTime, measurement.getTotalMillis()), measurement.isTimedOut());
        assertNotEquals("There should have been errors due to test time limit being breached",
                measurement.getNumberOfErrors(), 0);

        System.out.printf("testTimeout: %s%n", measurement);
    }

    /**
     * Sanity check
     */
    @Test
    public void perfTest() {
        int numberOfCalls = 10000;
        int threadCount = 10;
        int batchSize = 100;

        WorkerWorkload<Void> worker = new WorkerWorkload<Void>() {
            @Override
            public Void doWork(Void context, int batchSize, Measurement<Void> config) {
                for (int i = 0; i < batchSize; i++) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        config.incrementErrorCount(1);
                    }
                }

                return context;
            }

            @Override
            public void finishWork(Measurement<Void> measurement) {
            }
        };

        Measurement<Void> measurement = new Measurement<Void>(threadCount, numberOfCalls, batchSize).measure(worker);

        // Each iteration sleeps for 1 ms so the total time <= no of iterations divided by the number of threads
        assertTrue("Test ran too quickly", measurement.getTotalMillis() >= numberOfCalls / threadCount);

        System.out.printf("perfTest!: %s%n", measurement);
    }

    @Test
    public void regressionTest() throws IOException {
        int numberOfCalls = 10000;
        int threadCount = 10;
        int batchSize = 100;
        final AtomicInteger millis = new AtomicInteger(1);

        WorkerWorkload<Void> worker = new WorkerWorkload<Void>() {
            @Override
            public Void doWork(Void context, int batchSize, Measurement<Void> config) {
                for (int i = 0; i < batchSize; i++) {
                    try {
                        Thread.sleep(millis.get());
                    } catch (InterruptedException e) {
                        config.incrementErrorCount(1);
                    }
                }
                return null;
            }

            @Override
            public void finishWork(Measurement<Void> measurement) {
            }
        };

        try {
            // the test should take about 10000/10 msecs. Do an initial run and remember the value
            RegressionChecker config = new RegressionChecker("perf.args", "perf.last", "perf.var");
            String testName = getClass().getName() + "_regressionTest";
            config.setResetMetrics(true);

            Measurement.Builder builder = new Measurement.Builder(testName)
                    .numberOfCalls(numberOfCalls)
                    .numberOfThreads(threadCount)
                    .batchSize(batchSize)
                    .config(config);

            Measurement measurement = builder.build();

            measurement.measure(worker);
            assertFalse("There should not have been a perf regression", measurement.isRegression());

            config.setResetMetrics(false);
            builder.config(config);
            measurement = builder.build();
            millis.incrementAndGet();
            measurement.measure(worker);
            assertTrue("There should have been a perf regression", measurement.isRegression());

            System.out.printf("perfTest!: %s%n", measurement);

            // now do the same test but with a large variance
            PrintWriter out = new PrintWriter(new File("perf.var"));

            out.write(String.format("%s=10.0%n", testName).toString()); // anything within a 1000% variance should pass
            out.close();
            // configure the measurement to use the new variance file:
            RegressionChecker checker = new RegressionChecker("perf.args", "perf.last", "perf.var");
            measurement = builder.config(checker).build();
            measurement.measure(worker);
            assertFalse("There should not have been a perf regression with a large variance", measurement.isRegression());
        } finally {
            new File("perf.args").delete();
            new File("perf.last").delete();
            new File("perf.var").delete();
        }
    }

    @Test
    public void readPerfArgsTest() throws IOException {
        String testName = getClass().getName() + "_readPerfArgsTest";

        File argsFile = new File("perf.args");
        int rc = 1;
        long mt = 100000;
        int wc = 10;
        int nc = 1000000;
        int nt = 50;
        int bs = 100;

        try {
            // write the test arguments to a file
            PrintWriter out = new PrintWriter(argsFile);

            out.write(String.format("%s=%d,%d,%d,%d,%d,%d%n", testName, rc, mt, wc, nc, nt, bs).toString());
            out.close();

            // create a measurement object that will use the file based args to override the defaults
            Measurement<Void> measurement = new Measurement.Builder(testName)
                    .config(new RegressionChecker("perf.args", "perf.last", "perf.var"))
                    .build();

            measurement.measure(new WorkerWorkload<Void>() {
                @Override
                public Void doWork(Void context, int batchSize, Measurement measurement) {
                    return null;
                }
                @Override
                public void finishWork(Measurement<Void> measurement) {
                }
            });

            // assert that the configured file based params were used
            assertEquals("Wrong getNumberOfMeasurements", rc, measurement.getNumberOfMeasurements());
            assertEquals("Wrong getMaxTestTime", mt, measurement.getMaxTestTime());
            assertEquals("Wrong getNumberOfWarmupCalls", wc, measurement.getNumberOfWarmupCalls());
            assertEquals("Wrong getNumberOfCalls", nc, measurement.getNumberOfCalls());
            assertEquals("Wrong getNumberOfThreads", nt, measurement.getNumberOfThreads());
            assertEquals("Wrong getBatchSize", bs, measurement.getBatchSize());

            // assert that the checker also returns the correct configured file based params
            RegressionChecker checker = new RegressionChecker("perf.args", "perf.last", "perf.var");
            String[] args = checker.getTestArgs(testName);
            long mtt = checker.getArg(testName, args, 1, 0L, Long.class);

            assertEquals("Wrong getNumberOfMeasurements", rc, Integer.parseInt(args[0]));
            assertEquals("Wrong getMaxTestTime", mt, mtt);
            assertEquals("Wrong getNumberOfWarmupCalls", wc, Integer.parseInt(args[2]));
            assertEquals("Wrong getNumberOfCalls", nc, Integer.parseInt(args[3]));
            assertEquals("Wrong getNumberOfThreads", nt, Integer.parseInt(args[4]));
            assertEquals("Wrong getBatchSize", bs, Integer.parseInt(args[5]));
        } finally {
            new File("perf.args").delete();
            new File("perf.last").delete();
            new File("perf.var").delete();
        }
    }

    @Test
    public void testAverager() {
        Double[][] tests = {
           { 2.0, 5.0, 6.0, 9.0, 12.0, 26.0 },
        };

        for (Double[] vals : tests) {
            List<Double> values = new ArrayList<>(Arrays.asList(vals));

            double median = Averager.getMedian(values, 0, values.size() - 1);
            double q1 = Averager.getQ1(values);
            double q3 = Averager.getQ3(values);

            int size = values.size();

            Averager.removeOutliers(values);

            assertEquals("wrong first quartile", 5.000000, q1, 0.1);
            assertEquals("wrong third quartile", 12.000000, q3, 0.1);
            assertEquals("wrong median", 7.5, median, 0.1);
            assertEquals("Outlier in data was not detected", size - 1, values.size());
        }
    }

    /**
     * Test that if a worker throws an exception that the test is canceled
     */
    @Test
    public void testCancelOnException() {
        WorkerWorkload<Void> worker = new WorkerWorkload<Void>() {
            AtomicBoolean cancelled = new AtomicBoolean(false);

            @Override
            public Void doWork(Void context, int niters, Measurement<Void> opts) {
                if (!cancelled.getAndSet(true))
                    throw new RuntimeException("Testing throw exception");

                return context;
            }
            @Override
            public void finishWork(Measurement<Void> measurement) {
            }
        };

        Measurement<Void> config = new Measurement<>(2, 100);

        config.measure(worker);

        assertNotNull("Test should have thrown an exception", config.getException());

        System.out.printf("testCancelOnException!: %s%n", config);
    }
}
