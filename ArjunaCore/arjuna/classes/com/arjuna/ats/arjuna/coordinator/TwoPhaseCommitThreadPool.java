/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

// Remark: this class should be package private - change it or use a different class in BasicAction
public class TwoPhaseCommitThreadPool {
    private static ExecutorService executor = initializeExecutor();

    private static ExecutorService initializeExecutor() {
        return Executors.newFixedThreadPool(
                arjPropertyManager.getCoordinatorEnvironmentBean().getMaxTwoPhaseCommitThreads());
    }

    private static boolean restartExecutor() {
        shutdownExecutor();
        executor = initializeExecutor();

        return false; // not using virtual threads
    }

    private static boolean shutdownExecutor() {
        executor.shutdown();

        return executor.isShutdown();
    }

    private static List<Runnable> shutdownExecutorNow() {
        return executor.shutdownNow();
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

    static void submitJob(Consumer<BasicAction> func,
                          BasicAction action) {
        executor.submit(
                () -> func.accept(action)
        );
    }

    record BAAsyncPrepareJobParams(BasicAction theAction, AbstractRecord ar, Boolean reportHeuristics) {}

    static Future<Integer> submitJob(Function<BAAsyncPrepareJobParams, Integer> func, BAAsyncPrepareJobParams args) {
        return executor.submit(
                () -> func.apply(args)
        );
    }
}
