/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
