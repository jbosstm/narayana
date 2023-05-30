/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package io.narayana.perf;

/**
 * Test lifecycle notifications
 *
 * @param <T> caller specific context data
 */
public interface WorkerLifecycle<T> {

    /**
     * notify the worker that the test is starting (@link{io.narayana.perf.Measurement#measure})
     */
    void init();

    /**
     * notify the worker that the test has finished
     */
    void fini();
}