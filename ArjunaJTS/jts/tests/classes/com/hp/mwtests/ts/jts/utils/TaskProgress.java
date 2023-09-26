/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jts.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class for threads to report forward progress (@see TaskMonitor.monitorProgress)
 */
public class TaskProgress extends AtomicInteger {
    private boolean finished = false;

    public TaskProgress(int initialValue) {
        super(initialValue);
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Indicate that the associated thread no longer need to be monitored
     */
    public void setFinished() {
        this.finished = true;
    }

    /**
     * report forward progress
     */
    public void tick() {
        if (finished)
            System.err.println("request to progress a finished task");
        else
            incrementAndGet();
    }
}