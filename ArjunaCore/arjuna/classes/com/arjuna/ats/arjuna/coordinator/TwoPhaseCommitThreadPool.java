/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TwoPhaseCommitThreadPool {
    private static final int poolSize = arjPropertyManager.getCoordinatorEnvironmentBean().
            getMaxTwoPhaseCommitThreads();
    private static final ExecutorService executor = Executors.newFixedThreadPool(poolSize);

    public static String getExecutorClassName() {
        return executor.getClass().getName();
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

    protected static void submitJob(BiConsumer<BasicAction, Boolean> func,
                                               BasicAction action, boolean reportHeuristics) {
        executor.submit(
                () -> func.accept(action, reportHeuristics)
        );
    }

    public static void submitJob(java.util.function.Consumer<Boolean> func, boolean reportHeuristics) {
        // Don't want heuristic information otherwise would not be running async
        executor.submit(
                () -> func.accept(reportHeuristics)
        );
    }

    protected record BAAsyncPrepareJobParams(BasicAction theAction, AbstractRecord ar, Boolean reportHeuristics) {}

    protected static Future<Integer> submitJob(Function<BAAsyncPrepareJobParams, Integer> func, BAAsyncPrepareJobParams args) {
        return executor.submit(
                () -> func.apply(args)
        );
    }
}
