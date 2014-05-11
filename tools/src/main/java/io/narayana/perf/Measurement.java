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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:mmusgrov@redhat.com">M Musgrove</a>
 *
 * Config data for running a work load (@see PerformanceTester and @see Worker)
 */
public class Measurement<T> implements Serializable {
    private ExecutorService executor;
    private int threadCount;
    private int numberOfCalls;
    private int batchSize;
    private Worker<T> worker;


    private boolean cancelled;
    private int errorCount;
    private long totalMillis;
    private int throughput; // calls per second
    private boolean mayInterruptIfRunning;
    private T context;

    private final Set<T> contexts = new HashSet<T>();
    private String info;

    /**
     * Configuration options for performing numberOfCalls units of work using multiple
     * threads. The work is shared equally amongst each thread. For better
     * processor utilization of you should configure the batch size using the other
     * constructor (the default batch size is 1).
     *
     * @param worker Interface implementation for running a batch of work
     * @param numberOfCalls the number of workloads that will be executed
     * @param numberOfThreads the number of threads to use to complete the workload
     */
    public Measurement(Worker<T> worker, int numberOfCalls, int numberOfThreads) {
        this(worker, numberOfCalls, numberOfThreads, numberOfCalls / numberOfThreads);
    }

    /**
     * Configuration options for performing numberOfCalls units of work using multiple
     * threads. The worker will asked to do work in batchSize chunks until the workload
     * is complete. The work is shared equally amongst each thread.
     *
     * @param worker Interface implementation for running a batch of work
     * @param numberOfCalls the number of workloads that will be executed
     * @param numberOfThreads the number of threads to use to complete the workload
     * @param batchSize the size of each batch of work to pass to @see Worker tasks
     */
    public Measurement(Worker<T> worker, int numberOfCalls, int numberOfThreads, int batchSize) {
        this(numberOfCalls, numberOfThreads, batchSize);

        if (worker == null)
            throw new IllegalArgumentException("Worker must not be null");

        this.worker = worker;
    }

    private Measurement(int numberOfCalls, int numberOfThreads, int batchSize) {
        if (numberOfCalls < 1)
            throw new IllegalArgumentException("numberOfCalls must be greater than zero");

        if (numberOfThreads < 1)
            throw new IllegalArgumentException("threadCount must be greater than zero");

        if (numberOfCalls < threadCount)
            throw new IllegalArgumentException("numberOfCalls must be greater than or equal to the threadCount");

        if (batchSize < 1)
            throw new IllegalArgumentException("batchSize must be greater than zero");

        this.threadCount = numberOfThreads;
        this.numberOfCalls = numberOfCalls;
        this.batchSize = batchSize;
        this.worker = null;

        this.totalMillis = 0;
        this.throughput = 0;
        this.errorCount = 0;
        this.info = "";
    }

    /**
     * Time how long it takes to complete a workload using the configured number of threads and batch size
     */
    public synchronized void measure() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(threadCount);

            doMeasure();

            executor.shutdownNow();
        }
    }

    private void doMeasure()  {
        final AtomicInteger count = new AtomicInteger(numberOfCalls/batchSize);
        final AtomicBoolean aborted = new AtomicBoolean(false);
        final AtomicInteger runCount = new AtomicInteger(0);

        final Collection<Future<Measurement<T>>> tasks = new ArrayList<Future<Measurement<T>>>();
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount + 1); // workers + self

        worker.init();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(executor.submit(new Callable<Measurement<T>>() {
                public Measurement<T> call() throws Exception {
                    Measurement<T> res = new Measurement<T>(numberOfCalls, threadCount, batchSize);
                    int runs = 0;

                    cyclicBarrier.await(); // the first barrier ensures all threads are ready to go

                    long start = System.nanoTime();

                    try {
                        // all threads are ready - this thread gets more work in batchSize chunks until there isn't anymore
                        while(count.decrementAndGet() >= 0) {
                            res.setNumberOfCalls(batchSize);
                            res.setErrorCount(0);

                            // ask the worker to do batchSize units or work
                            try {
                                res.setContext(worker.doWork(res.getContext(), batchSize, res));
                                runs += (batchSize - res.getErrorCount());
                            } catch (Exception e) {
                            }

                            if (res.isCancelled()) {
                                aborted.set(true);
                                setContext(res.getContext());

                                for (Future<Measurement<T>> task : tasks) {
                                    if (!task.equals(this))
                                        task.cancel(res.isMayInterruptIfRunning());
                                }

                                break;
                            }
                        }
                    } finally {
                        runCount.addAndGet(runs);
                    }

                    cyclicBarrier.await();

                    res.setTotalMillis((System.nanoTime() - start) / 1000000L);

                    return res;
                };
            }));
        }

        try {
            long start = System.nanoTime();

            cyclicBarrier.await(); // wait for each thread to arrive at the barrier
            cyclicBarrier.await(); // wait for each thread to finish

            long end = System.nanoTime();

            setTotalMillis((end - start) / 1000000L);

            worker.fini();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            if (!aborted.get())
                throw new RuntimeException(e);

            setCancelled(true);
        }

        setErrorCount(numberOfCalls - runCount.get());

        for (Future<Measurement<T>> t : tasks) {
            try {
                Measurement<T> outcome = t.get();
                T context = outcome.getContext();

                if (context != null)
                    addContext(context);

                if (outcome.isCancelled())
                    setCancelled(true);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Workers may abort the whole measurement and optionally set a context when doing so.
     *
     * @return if a worker aborted the measurement and set a context then that context is returned
     */
    public T getContext() {
        return context;
    }

    /**
     * Each thread that runs the workload may optionally set a thread specific object (of type T).
     * @see Worker#doWork(T, int, Measurement)
     *
     * @return the contexts that each thread created
     */
    public Set<T> getContexts() {
        return contexts;
    }

    /**
     * @return Total number of workloads the comprise the measurement
     */
    public int getNumberOfCalls() {
        return numberOfCalls;
    }

    /**
     * @return number of threads used to complete the workload
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Each thread will invoke the worker in batchSize chunks
     *
     * @return the batch size
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * @return total number of milliseconds used to complete the workload
     */
    public long getTotalMillis() {
        return totalMillis;
    }

    /**
     * @return number of workloads per second (any errors will skew the overall throughput)
     */
    public int getThroughput() {
        return throughput;
    }

    /**
     * @return number of workload errors (which will be <= getNumberOfCalls) (any errors will skew the overall throughput).
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     * @return whether or not any worker cancelled the measurement
     */
    public boolean isCancelled() {
        return cancelled;
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

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    /**
     * Increment the error count
     * @param delta number of extra errors
     */
    public void incrementErrorCount(int delta) {
        errorCount += delta;
    }

    public String toString() {
        return String.format("%11d %6d %9d %11d%n",
                getThroughput(), numberOfCalls, getErrorCount(), threadCount);
    }



    private void setContext(T value) {
        context = value;
    }

    private void addContext(T t) {
        contexts.add(t);
    }

    private void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private boolean isMayInterruptIfRunning() {
        return mayInterruptIfRunning;
    }

    private  void setNumberOfCalls(int numberOfCalls) {
        this.numberOfCalls = numberOfCalls;
    }

    private void setTotalMillis(long totalMillis) {
        this.totalMillis = totalMillis;

        if (totalMillis != 0)
            this.throughput = (int) ((1000 * numberOfCalls) / totalMillis);
    }

    private void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }
}
