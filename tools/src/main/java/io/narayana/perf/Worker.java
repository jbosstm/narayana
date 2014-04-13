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

/**
 * @author <a href="mailto:mmusgrov@redhat.com">M Musgrove</a>
 *
 * Interface for running a batch of work
 */
public interface Worker<T> {
    /**
     * Perform a single unit of work. @see PerformanceTester begins a number of threads and each thread
     * then invokes the doWork method in parallel until there is no more remaining work.
     *
     * @param context a thread specific instance that may have been returned by a previous invocation of the doWork
     *                method by this thread. This may be useful if the worker needs to save thread specific data
     * @param niters the number of work iterations to perform in this batch
     * @param opts config parameters for the work that triggered this call
     * @return A thread specific instance that will be passed to subsequent calls to the doWork method by the thread
     */
    T doWork(T context, int niters, Result<T> opts);

    /**
     * notify the worker that the test is starting
     */
    void init();

    /**
     * notify the worker that the test has finished
     */
    void fini();
}
