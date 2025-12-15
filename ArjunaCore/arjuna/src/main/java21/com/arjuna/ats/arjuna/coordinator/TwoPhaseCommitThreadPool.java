/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

public class TwoPhaseCommitThreadPool {//implements AutoCloseable {
    private static boolean isUsingVirtualThreads;
    private static ExecutorService executor = initializeExecutor();

    private static ExecutorService initializeExecutor() {
        if (executor != null) {
           close();
        }

        if (arjPropertyManager.getCoordinatorEnvironmentBean().isUseVirtualThreadsForTwoPhaseCommitThreads()) {
            isUsingVirtualThreads = true;
            return Executors.newVirtualThreadPerTaskExecutor();
        } else {
            isUsingVirtualThreads = false;
            return Executors.newFixedThreadPool(
                    arjPropertyManager.getCoordinatorEnvironmentBean().getMaxTwoPhaseCommitThreads());
        }
    }

    static boolean restartExecutor() {
        executor = initializeExecutor();

        return isUsingVirtualThreads;
    }

    public static Future<Integer> submitJob(Callable<Integer> job) {
        return executor.submit(job);
    }

    public static void submitJob(Runnable job) {
        executor.submit(job);
    }

    public static CompletionService<Boolean> getNewCompletionService() {
        return new ExecutorCompletionService<Boolean>(executor);
    }

    static void submitJob(Consumer<BasicAction> func, BasicAction action) {
        executor.submit(
                () -> func.accept(action)
        );
    }

    static void submitJob(java.util.function.Consumer<Boolean> func) {
        // Don't want heuristic information otherwise would not be running async
        executor.submit(
                () -> func.accept(false)
        );
    }

    /**
     * @see java.util.concurrent.ExecutorService
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted.
     */
    static void close() {
        executor.close();
    }

    record BAAsyncPrepareJobParams(BasicAction theAction, AbstractRecord ar, Boolean reportHeuristics) {}

    static Future<Integer> submitJob(Function<BAAsyncPrepareJobParams, Integer> func, BAAsyncPrepareJobParams args) {
        return executor.submit(
                () -> func.apply(args)
        );
    }
}
