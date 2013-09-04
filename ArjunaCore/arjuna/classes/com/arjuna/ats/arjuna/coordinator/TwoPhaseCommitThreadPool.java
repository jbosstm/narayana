package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.util.concurrent.*;

public class TwoPhaseCommitThreadPool {
    private static final int poolSize = arjPropertyManager.getCoordinatorEnvironmentBean().
            getMaxTwoPhaseCommitThreads();
    private static final ExecutorService executor = Executors.newFixedThreadPool(poolSize);

    public static Future<Integer> submitJob(Callable<Integer> job) {
        return executor.submit(job);
    }

    public static void submitJob(Runnable job) {
        executor.submit(job);
    }

    public static CompletionService<Boolean> getNewCompletionService() {
        return new ExecutorCompletionService<Boolean>(executor);
    }
}
