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

import java.math.BigInteger;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

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

        Result<BigInteger> measurement = new Result<BigInteger>(threadCount, numberOfCalls, batchSize).measure(worker, worker);
        Set<BigInteger> subFactorials = measurement.getContexts();
        BigInteger fac = BigInteger.ONE;

        for (BigInteger bigInteger : subFactorials)
            fac = fac.multiply(bigInteger);

        long start = System.nanoTime();
        BigInteger serialFac = factorial(numberOfCalls);
        long millis = (System.nanoTime() - start) / 1000000L;

        System.out.printf("TestPerformance for %d!: %d calls / second (total time: %d ms versus %d ms)%n",
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
            public String doWork(String context, int niters, Result<String> opts) {
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
        };

        Result<String> measurement = new Result<>(threadCount, numberOfCalls, batchSize);

        measurement = measurement.measure(abortWorker);

        assertTrue("Test should have been aborted", measurement.isCancelled());
        assertEquals("Abort context should have been \"cancelled\"", "cancelled", measurement.getContext());
        assertNotSame("There should have been some workload errors", 0, measurement.getErrorCount());

        System.out.printf("testAbortMeasurement2: %s%n", measurement);
    };

    /**
     * Ensure it is possible to run a single workload
     */
    @Test
    public void testSingleCall() {
        WorkerWorkload<String> worker = new WorkerWorkload<String>() {
            @Override
            public String doWork(String context, int niters, Result<String> opts) {
                assertEquals("Wrong batch size", 1, niters);
                return context;
            }
        };

        Result<String> config = new Result<>(1, 1, 1);

        config.measure(worker);

        assertEquals("Wrong batch size", 1, config.getBatchSize());

        System.out.printf("testSingleCall!: %s%n", config);
    }

    /**
     * Sanity check
     */
    @Test
    public void perfTest() {
        int numberOfCalls = 10000;
        int threadCount = 10;
        int batchSize = 100;

        WorkerWorkload<Object> worker = new WorkerWorkload<Object>() {
            @Override
            public Object doWork(Object context, int batchSize, Result<Object> config) {
                for (int i = 0; i < batchSize; i++) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        config.incrementErrorCount(1);
                    }
                }
                return null;
            }
        };

        Result measurement = new Result(threadCount, numberOfCalls, batchSize).measure(worker);

        // Each iteration sleeps for 1 ms so the total time <= no of iterations divided by the number of threads
        assertTrue("Test ran too quickly", measurement.getTotalMillis() >= numberOfCalls / threadCount);

        System.out.printf("perfTest!: %s%n", measurement);
    }
}
