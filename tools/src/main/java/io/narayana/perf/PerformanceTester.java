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
 * @deprecated replaced by {@link Result#measure(WorkerLifecycle, WorkerWorkload)}}
 */
@Deprecated
public class PerformanceTester<T> {
    private static int DEF_WORK_BATCH_SZ = 32;
    private static int DEF_THREAD_POOL_SZ = 100;

    private int BATCH_SIZE;
    private int THREAD_POOL_SIZE;
//    private ExecutorService executor;

    public PerformanceTester() {
        this(DEF_THREAD_POOL_SZ, DEF_WORK_BATCH_SZ);
    }

    /**
     *
     * @param maxThreads the number of threads to use to complete the workload
     * @param batchSize the size of each batch of work to pass to @see Worker tasks
     */
    public PerformanceTester(int maxThreads, int batchSize) {
        THREAD_POOL_SIZE = maxThreads > 0 ? maxThreads : DEF_THREAD_POOL_SZ; // must be >=  jacorb.poa.thread_pool_max
        BATCH_SIZE = batchSize > 0 ? batchSize : DEF_WORK_BATCH_SZ;

//        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * shutdown the executor
     */
    public void fini() {
//        executor.shutdownNow();
    }

    /**
     * Run a workload using the passed in config and worker
     *
     * @param worker a multithreaded worker for running batches of work
     * @param config config parameters for the run
     * @return
     */
    public Result<T> measureThroughput(Worker<T> worker, Result<T> config) {
        return measureThroughput(worker, worker, config.getNumberOfCalls(), config.getThreadCount(), BATCH_SIZE, 0);
    }

    public Result<T> measureThroughput(WorkerWorkload<T> workload, int callCount, int threadCount, int batchSize) {
        return measureThroughput(null, workload, callCount, threadCount, batchSize, 0);
    }

    public Result<T> measureThroughput(WorkerLifecycle<T> lifecycle, WorkerWorkload<T> workload, int callCount, int threadCount, int batchSize) {
        return measureThroughput(lifecycle, workload, callCount, threadCount, batchSize, 0);
    }

    public Result<T> measureThroughput(final WorkerLifecycle<T> lifecycle, final WorkerWorkload<T> workload,
                                       int callCount, int threadCount, final int batchSize, int warmUpCallCount) {

        final Result<T> opts = new Result<>(threadCount, callCount, batchSize, THREAD_POOL_SIZE);

        if (workload == null)
            throw new IllegalArgumentException("workload must not be null");

        if (lifecycle != null)
            lifecycle.init();

        if (warmUpCallCount > 0)
            doWork(workload, new Result<T>(threadCount, warmUpCallCount, batchSize, THREAD_POOL_SIZE));

        Result<T> res = doWork(workload, opts);

        if (lifecycle != null)
            lifecycle.fini();

        return res;
    }

    private Result<T> doWork(final WorkerWorkload<T> workload, final Result<T> opts) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        final AtomicInteger count = new AtomicInteger(opts.getNumberOfBatches());

        final Collection<Future<Result<T>>> tasks = new ArrayList<Future<Result<T>>>();
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(opts.getThreadCount() + 1); // workers + self

        for (int i = 0; i < opts.getThreadCount(); i++)
            tasks.add(executor.submit(new Callable<Result<T>>() {
                public Result<T> call() throws Exception {
                    Result<T> res = new Result<T>(opts);
                    int errorCount = 0;

                    cyclicBarrier.await();
                    long start = System.nanoTime();

                    // all threads are ready - this thread gets more work in batch size chunks until there isn't anymore
                    while(count.decrementAndGet() >= 0) {
                        res.setNumberOfCalls(opts.getBatchSize());
                        // ask the worker to do batchSize units or work
                        res.setContext(workload.doWork(res.getContext(), opts.getBatchSize(), res));
                        errorCount += res.getErrorCount();

                        if (res.isCancelled()) {
                            for (Future<Result<T>> task : tasks) {
                                if (!task.equals(this))
                                    task.cancel(res.isMayInterruptIfRunning());
                            }

                            opts.setContext(res.getContext());

                            break;
                        }
                    }

                    cyclicBarrier.await();

                    res.setTotalMillis((System.nanoTime() - start) / 1000000L);
                    if (res.getTotalMillis() < 0)
                        res.setTotalMillis(-res.getTotalMillis());

                    res.setErrorCount(errorCount);

                    return res;
                };
            }));

        long start = System.nanoTime();

        try {
            cyclicBarrier.await(); // wait for each thread to arrive at the barrier
            cyclicBarrier.await(); // wait for each thread to finish

            long tot = System.nanoTime() - start;

            if (tot < 0) // nanoTime is reckoned from an arbitrary origin which may be in the future
                tot = -tot;

            opts.setTotalMillis(tot / 1000000L);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            opts.setCancelled(true);
        }

        opts.setErrorCount(0);

        for (Future<Result<T>> t : tasks) {
            try {
                Result<T> outcome = t.get();
                T context = outcome.getContext();

                if (context != null)
                    opts.addContext(context);

                opts.setErrorCount(opts.getErrorCount() + outcome.getErrorCount());
            } catch (CancellationException e) {
                opts.setErrorCount(opts.getErrorCount() + opts.getBatchSize());
            } catch (Exception e) {
                opts.setErrorCount(opts.getErrorCount() + opts.getBatchSize());
            }
        }

        executor.shutdownNow();

        return opts;
    }
}

