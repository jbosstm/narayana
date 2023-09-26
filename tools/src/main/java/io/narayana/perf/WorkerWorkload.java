/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.perf;

/**
 * Interface for running a workload (@link{io.narayana.perf.Measurement#measure})
 *
 * @param <T> caller specific context data
 */
public interface WorkerWorkload<T> {
    /**
     * Perform a batch of work units. @see Measurement#measure begins a number of threads and each thread
     * then invokes the doWork method in parallel until there is no more remaining work.
     *
     * If work throws an exception then whole test is cancelled and exception is reported via the
     * (@link{Measurement.exception}) property
     *
     * @param context a thread specific instance that may have been returned by a previous invocation of the doWork
     *                method by this thread. This may be useful if the worker needs to save thread specific data
     *                for use by later invocations on the same thread
     * @param batchSize the number of work iterations to perform in this batch
     * @param measurement config parameters for the work that triggered this call
     *
     * @return A thread specific instance that will be passed to subsequent calls to the doWork method by the thread
     */
    T doWork(T context, int batchSize, Measurement<T> measurement);

    /**
     * Notify the worker that the @link{doWork} method will not be called again on the current thread (but it may
     * still be called from other threads).
     *
     * @param measurement config parameters for the work that triggered this call - each worker thread gets its own copy
     *                    of the config. NB: Any context object associated with the last call to the @link{doWork} call is
     *                    available by calling @link{measurement.getContext} on this parameter.
     */
    void finishWork(Measurement<T> measurement);
}