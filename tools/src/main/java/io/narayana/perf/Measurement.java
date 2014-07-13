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

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:mmusgrov@redhat.com">M Musgrove</a>
 *
 * Config and result data for running a work load (@link{Measurement#measure})
 */
public class Measurement<T> implements Serializable {
    boolean regression;
    int numberOfCalls;
    int threadCount = 1;
    int batchSize = 1;
    int numberOfBatches;

    int errorCount;
    long totalMillis;
    int one; // time in msecs to do one call
    int throughput; // calls per second
    private T context;

    private final Set<T> contexts = new HashSet<T>();

    private String info;
    private boolean cancelled;
    private boolean mayInterruptIfRunning;
    private long maxTestTime = 0; // terminate measurement if test takes longer than this value
    private int warmUpCallCount = 0;

    public Measurement(int threadCount, int numberOfCalls) {
        this(threadCount, numberOfCalls, 10);
    }

    public Measurement(int threadCount, int numberOfCalls, int batchSize) {
        this(0L, threadCount, numberOfCalls, batchSize);
    }

    public Measurement(long maxTestTime, int threadCount, int numberOfCalls, int batchSize) {
        this.maxTestTime = maxTestTime;
        this.numberOfCalls = numberOfCalls;
        this.threadCount = threadCount;
        this.batchSize = batchSize;

        this.totalMillis = this.throughput = 0;
        this.errorCount = 0;

        if (numberOfCalls < batchSize) {
            System.err.println("Updating call count (request size less than batch size)");
            this.numberOfCalls = batchSize;
        }

        numberOfBatches = this.numberOfCalls/batchSize;

        if (numberOfBatches < this.threadCount) {
            System.err.printf("Too few batches - reducing thread count (%d %d %d)%n",
                    this.threadCount, numberOfBatches, this.numberOfCalls);
            this.threadCount = numberOfBatches;
        }
    }

    public boolean isRegression() {
        return regression;
    }

    public void setRegression(boolean regression) {
        this.regression = regression;
    }


    public void setContext(T value) {
        context = value;
    }

    public T getContext() {
        return context;
    }

    public Set<T> getContexts() {
        return contexts;
    }

    void addContext(T t) {
        contexts.add(t);
    }

    public Measurement(Measurement result) {
        this(result.threadCount, result.numberOfCalls);

        this.totalMillis = result.totalMillis;
        this.throughput = result.throughput;
        this.errorCount = 0;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getNumberOfCalls() {
        return numberOfCalls;
    }

    public void setNumberOfCalls(int numberOfCalls) {
        this.numberOfCalls = numberOfCalls;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getNumberOfBatches() {
        return numberOfBatches;
    }

    public long getTotalMillis() {
        return totalMillis;
    }

    public long getOne() {
        return one;
    }

    public void setTotalMillis(long totalMillis) {
        this.totalMillis = totalMillis;
        if (totalMillis != 0) {
            this.one = totalMillis > 0 ? (int) (totalMillis / numberOfCalls) : 0;
            this.throughput = (int) ((1000 * numberOfCalls) / totalMillis);
        }
    }

    public int getThroughput() {
        return throughput;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public void incrementErrorCount() {
        this.errorCount += 1;
    }

    public void incrementErrorCount(int delta) {
        this.errorCount += delta;
    }

    public String toString() {
        return String.format("%d calls / second (%d calls in %d ms using %d threads. %d errors)",
                getThroughput(), getNumberOfCalls(),
                getTotalMillis(), getThreadCount(),
                getErrorCount());
    }

    public void setInfo(String info) {
        this.info = info;
    }


    public String getInfo() {
        return info;
    }

    /**
     * Cancel the measurement.
     *
     * A worker may cancel a measurement by invoking this method on the Measurement object it was
     * passed in its @see Worker#doWork(T, int, Measurement) method
     * @param mayInterruptIfRunning if false then any running calls to @see Worker#doWork will be allowed to finish
     *                              before the the measurement is cancelled.
     */
    public void cancel(boolean mayInterruptIfRunning) {
        this.cancelled = true;
        this.mayInterruptIfRunning = mayInterruptIfRunning;
    }

    void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isMayInterruptIfRunning() {
        return mayInterruptIfRunning;
    }

    /**
     * @return max test time in milliseconds
     */
    public long getMaxTestTime() {
        return maxTestTime;
    }

    public void setMaxTestTime(long maxTestTime) {
        this.maxTestTime = maxTestTime;
    }

    public int getWarmUpCallCount() {
        return warmUpCallCount;
    }

    /**
     *
     * @return true if the measurement took longer than the maximum test time {@link io.narayana.perf.Measurement#getMaxTestTime()}
     */
    public boolean isTimedOut() {
        return isCancelled() || getTotalMillis() > maxTestTime;
    }

    public Measurement<T> measure(WorkerWorkload<T> workload) {
        return measure(null, workload);
    }

    public Measurement<T> measure(WorkerLifecycle<T> lifecycle, WorkerWorkload<T> workload) {
        return measure(lifecycle, workload, 0);
    }

    public Measurement<T> measure(final WorkerLifecycle<T> lifecycle, final WorkerWorkload<T> workload, int warmUpCallCount) {
        this.warmUpCallCount = warmUpCallCount;
        if (workload == null)
            throw new IllegalArgumentException("workload must not be null");

        if (lifecycle != null)
            lifecycle.init();

        if (warmUpCallCount > 0)
            doWork(workload, new Measurement<T>(maxTestTime, threadCount, warmUpCallCount, batchSize));

        Measurement<T> res = doWork(workload, this);

        if (lifecycle != null)
            lifecycle.fini();

        return res;
    }

    private Measurement<T> doWork(final WorkerWorkload<T> workload, final Measurement<T> opts) {
        ExecutorService executor = Executors.newFixedThreadPool(opts.getThreadCount());
        final AtomicInteger count = new AtomicInteger(opts.getNumberOfBatches());

        final Collection<Future<Measurement<T>>> tasks = new ArrayList<Future<Measurement<T>>>();
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(opts.getThreadCount() + 1); // workers + self

        for (int i = 0; i < opts.getThreadCount(); i++)
            tasks.add(executor.submit(new Callable<Measurement<T>>() {
                public Measurement<T> call() throws Exception {
                    Measurement<T> res = new Measurement<T>(opts);
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
                            for (Future<Measurement<T>> task : tasks) {
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

        opts.setErrorCount(0);

        long start = System.nanoTime();

        try {
            cyclicBarrier.await(); // wait for each thread to arrive at the barrier

            // wait for each thread to finish
            if (opts.getMaxTestTime() > 0)
                cyclicBarrier.await(opts.getMaxTestTime(), TimeUnit.MILLISECONDS);
            else
                cyclicBarrier.await();

            long tot = System.nanoTime() - start;

            if (tot < 0) // nanoTime is reckoned from an arbitrary origin which may be in the future
                tot = -tot;

            opts.setTotalMillis(tot / 1000000L);

        } catch (InterruptedException e) {
            opts.incrementErrorCount(); // ? exactly how many errors were there?
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            opts.incrementErrorCount(); // ? exactly how many errors were there?
            opts.setCancelled(true);
        } catch (TimeoutException e) {
            opts.incrementErrorCount(); // ? exactly how many errors were there?
            opts.setCancelled(true);
        }

        for (Future<Measurement<T>> t : tasks) {
            try {
                Measurement<T> outcome = t.get();
                T context = outcome.getContext();

                if (context != null)
                    opts.addContext(context);

                opts.incrementErrorCount(outcome.getErrorCount());
            } catch (CancellationException e) {
                opts.incrementErrorCount(opts.getBatchSize());
                opts.setCancelled(true);
            } catch (ExecutionException e) { // should be a BrokenBarrierException due to a timeout
                System.out.printf("ExecutionException exception: %s%n", e.getMessage());
                opts.incrementErrorCount(opts.getBatchSize());
                opts.setCancelled(true);
            } catch (Exception e) {
                System.err.printf("Performance test exception: %s%n", e.getMessage());
                opts.incrementErrorCount(opts.getBatchSize());
            }
        }

        executor.shutdownNow();

        return opts;
    }
}
