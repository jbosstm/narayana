/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.perf;

/**
 * @author <a href="mailto:mmusgrov@redhat.com">M Musgrove</a>
 *
 * Interface for running a batch of work and for receiving test lifecycle notifications
 */
public interface Worker<T> extends WorkerLifecycle<T>, WorkerWorkload<T> {
}