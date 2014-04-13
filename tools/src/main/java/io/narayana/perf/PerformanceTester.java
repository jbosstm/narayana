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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:mmusgrov@redhat.com">M Musgrove</a>
 *
 * Workload runner for measuring the maximum throughput of an instance of @see Worker
 */
public class PerformanceTester<T> {
    private static int DEF_WORK_BATCH_SZ = 32;
    private static int DEF_THREAD_POOL_SZ = 100;

    private int BATCH_SIZE;
    private int POOL_SIZE;
    private ExecutorService executor;

    public PerformanceTester() {
        this(DEF_THREAD_POOL_SZ, DEF_WORK_BATCH_SZ);
    }

    /**
     *
     * @param maxThreads the number of threads to use to complete the workload
     * @param batchSize the size of each batch of work to pass to @see Worker tasks
     */
    public PerformanceTester(int maxThreads, int batchSize) {
        POOL_SIZE = maxThreads > 0 ? maxThreads : DEF_THREAD_POOL_SZ; // must be >=  jacorb.poa.thread_pool_max
        BATCH_SIZE = batchSize > 0 ? batchSize : DEF_WORK_BATCH_SZ;

        executor = Executors.newFixedThreadPool(POOL_SIZE);
    }

    /**
     * shutdown the executor
     */
    public void fini() {
        executor.shutdownNow();
    }

    /**
     * Run a workload using the passed in config and worker
     *
     * @param worker a multithreaded worker for running batches of work
     * @param config config parameters for the run
     * @return
     */
    public Result<T> measureThroughput(Worker<T> worker, Result<T> config) {
        return doWork(worker, config);
    }

    private Result<T> doWork(final Worker<T> worker, final Result<T> opts)  {
        int threadCount = opts.getThreadCount();
        int callCount = opts.getNumberOfCalls();

        if (threadCount > POOL_SIZE) {
            System.err.println("Updating thread count (request size exceeds thread pool size)");
            threadCount = POOL_SIZE;
            opts.setThreadCount(POOL_SIZE);
        }

        if (callCount < BATCH_SIZE) {
            System.err.println("Updating call count (request size less than batch size)");
            callCount = BATCH_SIZE;
            opts.setNumberOfCalls(callCount);
        }

        int batchCount =  callCount/BATCH_SIZE;

        if (batchCount < threadCount) {
            System.err.println("Reducing thread count (request number greater than the number of batches)");
            threadCount = batchCount;
            opts.setThreadCount(threadCount);
        }

        final AtomicInteger count = new AtomicInteger(callCount/BATCH_SIZE);

        Collection<Future<Result<T>>> tasks = new ArrayList<Future<Result<T>>>();
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount + 1); // workers + self

        worker.init();

        for (int i = 0; i < opts.getThreadCount(); i++)
            tasks.add(executor.submit(new Callable<Result<T>>() {
                public Result<T> call() throws Exception {
                    Result<T> res = new Result<T>(opts);
                    int errorCount = 0;

                    cyclicBarrier.await();
                    long start = System.nanoTime();

                    // all threads are ready - this thread gets more work in batch size chunks until there isn't anymore
                    while(count.decrementAndGet() >= 0) {
                        res.setNumberOfCalls(BATCH_SIZE);
                        // ask the worker to do BATCH_SIZE units or work
                        res.setContext(worker.doWork(res.getContext(), BATCH_SIZE, res));
                        errorCount += res.getErrorCount();
                    }

                    cyclicBarrier.await();

                    res.setTotalMillis((System.nanoTime() - start) / 1000000L);
                    res.setErrorCount(errorCount);

                    return res;
                };
            }));

        long start = System.nanoTime();

        try {
            cyclicBarrier.await(); // wait for each thread to arrive at the barrier
            cyclicBarrier.await(); // wait for each thread to finish

            worker.fini();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }

        long end = System.nanoTime();

        opts.setTotalMillis(0L);
        opts.setErrorCount(0);

        for (Future<Result<T>> t : tasks) {
            try {
                Result<T> outcome = t.get();
                T context = outcome.getContext();

                if (context != null)
                    opts.addContext(context);

                opts.setErrorCount(opts.getErrorCount() + outcome.getErrorCount());
            } catch (Exception e) {
                opts.setErrorCount(opts.getErrorCount() + BATCH_SIZE);
            }
        }

        opts.setTotalMillis((end - start) / 1000000L);

        return opts;
    }
}

