/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

// Remark: this class should be package private - change it or use a different class in BasicAction
public class TwoPhaseCommitThreadPool {
    private static final ExecutorService executor = initializeExecutor();

    private static ExecutorService initializeExecutor() {
        return Executors.newFixedThreadPool(
                arjPropertyManager.getCoordinatorEnvironmentBean().getMaxTwoPhaseCommitThreads());
    }

    static boolean restartExecutor() {
        // only implemented when running on JRE 21 and above
        return false;
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

    static void submitJob(BiConsumer<BasicAction, Boolean> func,
                                               BasicAction action, boolean reportHeuristics) {
        executor.submit(
                () -> func.accept(action, reportHeuristics)
        );
    }

    static void submitJob(java.util.function.Consumer<Boolean> func, boolean reportHeuristics) {
        // Don't want heuristic information otherwise would not be running async
        executor.submit(
                () -> func.accept(reportHeuristics)
        );
    }

    record BAAsyncPrepareJobParams(BasicAction theAction, AbstractRecord ar, Boolean reportHeuristics) {}

    static Future<Integer> submitJob(Function<BAAsyncPrepareJobParams, Integer> func, BAAsyncPrepareJobParams args) {
        return executor.submit(
                () -> func.apply(args)
        );
    }
}
