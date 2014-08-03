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

import java.io.IOException;
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
    String name;

    int numberOfMeasurements = 1; // if > 1 then an average of each run is taken (after removing outliers)
    int numberOfCalls; // the total number of iterations used in this measurement
    int numberOfThreads = 1; // the number of threads used to complete the measurement
    int batchSize = 1; // iterations are executed in batches
    private long maxTestTime = 0; // terminate measurement if test takes longer than this value
    private int numberOfWarmupCalls = 0; //number of iterations before starting the measurement
    private String info;

    RegressionChecker config; // holds file based measurment data

    private T context;
    private final Set<T> contexts = new HashSet<T>();
    int numberOfErrors = 0;
    long totalMillis = 0L;
    int one = 0; // time in msecs to do one call
    double throughput = 0; // calls per second

    private boolean cancelled;
    private boolean mayInterruptIfRunning;
    int numberOfBatches = 0;
    boolean regression;
    boolean failOnRegression;

    public Measurement(int numberOfThreads, int numberOfCalls) {
        this(numberOfThreads, numberOfCalls, 10);
    }

    public Measurement(int numberOfThreads, int numberOfCalls, int batchSize) {
        this(0L, numberOfThreads, numberOfCalls, batchSize);
    }

    public Measurement(long maxTestTime, int numberOfThreads, int numberOfCalls, int batchSize) {
        this(new Builder("").
             maxTestTime(maxTestTime).numberOfThreads(numberOfThreads).numberOfCalls(numberOfCalls).batchSize(batchSize).construct());
    }

    public Measurement(Measurement result) {
        this(result.maxTestTime, result.numberOfThreads, result.numberOfCalls, result.batchSize);
    }

    private Measurement(Builder builder) {
        name = builder.name;
        numberOfMeasurements = builder.numberOfMeasurements;
        numberOfCalls = builder.numberOfCalls;
        numberOfThreads = builder.numberOfThreads;
        batchSize = builder.batchSize;
        maxTestTime = builder.maxTestTime;
        numberOfWarmupCalls = builder.numberOfWarmupCalls;
        numberOfBatches = builder.numberOfBatches;
        config = builder.config;
        info = builder.info;
        failOnRegression = config == null ? false : config.isFailOnRegression();
    }

    public boolean isRegression() {
        return regression;
    }

    public boolean isFailOnRegression() {
        return failOnRegression;
    }

    public boolean shouldFail() {
        return isFailOnRegression() && isRegression();
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

    public int getNumberOfMeasurements() {
        return numberOfMeasurements;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public int getNumberOfCalls() {
        return numberOfCalls;
    }

    void setNumberOfCalls(int numberOfCalls) {
        this.numberOfCalls = numberOfCalls;
    }

    public int getBatchSize() {
        return batchSize;
    }

    int getNumberOfBatches() {
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
            this.throughput =  (1000.0 * numberOfCalls) / totalMillis;
        }
    }

    public double getThroughput() {
        return throughput;
    }

    public int getNumberOfErrors() {
        return numberOfErrors;
    }

    public void setNumberOfErrors(int numberOfErrors) {
        this.numberOfErrors = numberOfErrors;
    }

    public void incrementErrorCount() {
        this.numberOfErrors += 1;
    }

    public void incrementErrorCount(int delta) {
        this.numberOfErrors += delta;
    }

    public String toString() {
        return String.format("%f calls / second (%d calls in %d ms using %d threads. %d errors)",
                getThroughput(), getNumberOfCalls(),
                getTotalMillis(), getNumberOfThreads(),
                getNumberOfErrors());
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

    /**
     * Cancel the measurement.
     *
     * A worker may cancel a measurement by invoking this method on the Measurement object it was
     * passed in its @see Worker#doWork(T, int, Measurement) method
     * @param reason the reason for the cancelation
     * @param mayInterruptIfRunning if false then any running calls to @see Worker#doWork will be allowed to finish
     *                              before the the measurement is cancelled.
     */
    public void cancel(String reason, boolean mayInterruptIfRunning) {
        this.setInfo(reason);
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

    public int getNumberOfWarmupCalls() {
        return numberOfWarmupCalls;
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

    public Measurement<T> measure(final WorkerLifecycle<T> lifecycle, final WorkerWorkload<T> workload) {
        if (workload == null)
            throw new IllegalArgumentException("workload must not be null");

        if (lifecycle != null)
            lifecycle.init();

        if (numberOfWarmupCalls > 0) {
            System.out.printf("Test Warm Up: %s: (%d calls using %d threads)%n", name, numberOfWarmupCalls, numberOfThreads);
            doWork(workload, new Measurement<T>(maxTestTime, numberOfThreads, numberOfWarmupCalls, 1));
        }

        System.out.printf("Test Run: %s (%d calls using %d threads)%n", name, numberOfCalls, numberOfThreads);

        if (numberOfMeasurements == 1) {
            doWork(workload, this);
        } else if (config == null) {
            for (int i = 0; i < numberOfMeasurements; i++)
                doWork(workload, this);
        } else {
            boolean wasFailOnRegression = config.isFailOnRegression();
            List<Double> metrics = new ArrayList<Double>();

            if (wasFailOnRegression) {
                config.setFailOnRegression(false);
                this.failOnRegression = false;
            }

            for (int i = 0; i < numberOfMeasurements; i++) {
                doWork(workload, this);
                metrics.add(getThroughput());
            }

            config.setFailOnRegression(wasFailOnRegression);
            this.failOnRegression = wasFailOnRegression;

            throughput = Averager.getAverage(metrics);
        }

        if (lifecycle != null)
            lifecycle.fini();

        StringBuilder sb = new StringBuilder();

        if (config != null)
            setRegression(!config.updateMetric(sb, name, getThroughput(), true));

        setInfo(sb.toString());

        return this;
    }

    private Measurement<T> doWork(final WorkerWorkload<T> workload, final Measurement<T> opts) {
        ExecutorService executor = Executors.newFixedThreadPool(opts.getNumberOfThreads());
        final AtomicInteger count = new AtomicInteger(opts.getNumberOfBatches());

        final Collection<Future<Measurement<T>>> tasks = new ArrayList<Future<Measurement<T>>>();
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(opts.getNumberOfThreads() + 1); // workers + self

        totalMillis = 0;

        for (int i = 0; i < opts.getNumberOfThreads(); i++)
            tasks.add(executor.submit(new Callable<Measurement<T>>() {
                public Measurement<T> call() throws Exception {
                    Measurement<T> res =  new Measurement<>(
                            opts.getMaxTestTime(), opts.getNumberOfThreads(), opts.getNumberOfCalls(), opts.getBatchSize());
                    int errorCount = 0;

                    cyclicBarrier.await();
                    long start = System.nanoTime();

                    // all threads are ready - this thread gets more work in batch size chunks until there isn't anymore
                    while(count.decrementAndGet() >= 0) {
                        res.setNumberOfCalls(opts.getBatchSize());
                        // ask the worker to do batchSize units or work
                        res.setContext(workload.doWork(res.getContext(), opts.getBatchSize(), res));
                        errorCount += res.getNumberOfErrors();

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

                    res.setNumberOfErrors(errorCount);

                    return res;
                };
            }));

        opts.setNumberOfErrors(0);

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

                opts.incrementErrorCount(outcome.getNumberOfErrors());
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

    public static final class Builder<T> {
        private String name;
        private int numberOfCalls = 10;
        private int numberOfThreads = 1;
        private int batchSize = 1;
        private long maxTestTime = 0L;
        private int numberOfWarmupCalls = 0;
        private int numberOfBatches;
        private int numberOfMeasurements = 1;
        private String info;
        RegressionChecker config;

        public Builder(String name) {
            name(name);
        }

        public Builder name(String name) {
            if (name == null)
                throw new IllegalArgumentException("name must be null");
            this.name = name;
            return this;
        }

        /**
         * The number of times to run the measurement.
         * If greater than one then outliers are discounted and an average is taken of the remaining runs.
         * @param numberOfMeasurements number of measurements
         * @return the builder
         */
        public Builder numberOfMeasurements(int numberOfMeasurements) {
            this.numberOfMeasurements = numberOfMeasurements;
            return this;
        }

        public Builder numberOfCalls(int numberOfCalls) {
            this.numberOfCalls = numberOfCalls;
            return this;
        }

        public Builder numberOfThreads(int numberOfThreads) {
            this.numberOfThreads = numberOfThreads;
            return this;
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder maxTestTime(long maxTestTime) {
            this.maxTestTime = maxTestTime;
            return this;
        }

        public Builder numberOfWarmupCalls(int numberOfWarmupCalls) {
            this.numberOfWarmupCalls = numberOfWarmupCalls;
            return this;
        }

        public Builder info(String info) {
            this.info = info;
            return this;
        }

        public Builder config() throws IOException {
            return config(new RegressionChecker());
        }

        public Builder config(RegressionChecker config) {
            this.config = config;
            return this;
        }

        private Builder construct() {
            if (config == null && RegressionChecker.isRegressionCheckEnabled()) {
                try {
                    config = new RegressionChecker();
                } catch (IOException e) {
                }
            }

            if (config != null) {
                String[] xargs = config.getTestArgs(name);

                numberOfMeasurements = config.getArg(name, xargs, 0, numberOfMeasurements, Integer.class);
                maxTestTime = config.getArg(name, xargs, 1, maxTestTime, Long.class);
                numberOfWarmupCalls = config.getArg(name, xargs, 2, numberOfWarmupCalls, Integer.class);
                numberOfCalls = config.getArg(name, xargs, 3, numberOfCalls, Integer.class);
                numberOfThreads = config.getArg(name, xargs, 4, numberOfThreads, Integer.class);
                batchSize = config.getArg(name, xargs, 5, batchSize, Integer.class);
            }

            if (batchSize <= 0) {
                System.err.printf("Invalid batch size (%d) setting to 1%n", batchSize);
                batchSize = 1;
            }

            if (numberOfCalls < batchSize) {
                System.err.println("Updating call count (request size less than batch size)");
                numberOfCalls = batchSize;
            }

            numberOfBatches = numberOfCalls / batchSize;

            if (numberOfBatches < numberOfThreads) {
                System.err.printf("Too few batches - reducing thread count (%d %d %d)%n",
                        numberOfThreads, numberOfBatches, numberOfCalls);
                numberOfThreads = numberOfBatches;
            }

            return this;
        }

        public <T> Measurement<T> build() {
            construct();

            return new Measurement<>(this);
        }
    }
}
