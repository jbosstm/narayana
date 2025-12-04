/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.util.concurrent.*;
import java.util.function.BiFunction;

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

    protected static Future<Integer> submitJob(BiFunction<Boolean, AbstractRecord, Integer> func,
                                               AbstractRecord ar, boolean reportHeuristics) {
        return executor.submit(
                () -> func.apply(reportHeuristics, ar)
        );
    }

    public static void submitJob(java.util.function.Consumer<Boolean> func, boolean reportHeuristics) {
        // Don't want heuristic information otherwise would not be running async
        executor.submit(
                () -> func.accept(reportHeuristics)
        );
    }

    // TODO we are missing functionality to shut down the executor which should be implemented and called from
    //  BasicAction.finalizeInternal()
    // Refer to the javadoc for java.util.concurrent.ExecutorService to see how to do that safely.
    // But note that starting with jdk 21 there is an Executors.newVirtualThreadPerTaskExecutor()
    // executor which can be autoclosed using a try-with-resources statement
}
