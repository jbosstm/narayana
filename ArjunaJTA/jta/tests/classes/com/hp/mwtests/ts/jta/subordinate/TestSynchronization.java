/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.subordinate;

import jakarta.transaction.Synchronization;

/**
 * Implementation of Synchronization for use in tx test cases.
 */
public class TestSynchronization implements Synchronization
{
    private boolean beforeCompletionDone = false;
    private boolean afterCompletionDone = false;

    public boolean isBeforeCompletionDone()
    {
        return beforeCompletionDone;
    }

    public boolean isAfterCompletionDone()
    {
        return afterCompletionDone;
    }

    public void beforeCompletion() {
        if(beforeCompletionDone) {
            System.out.println("beforeCompletion called more than once");
            throw new RuntimeException("beforeCompletion called more than once");
        }

        beforeCompletionDone = true;
        System.out.println("TestSynchronization.beforeCompletion()");
    }

    public void afterCompletion(int i) {
        if(afterCompletionDone) {
            System.out.println("afterCompletion called more than once");
            throw new RuntimeException("afterCompletion called more than once");
        }

        afterCompletionDone = true;
        System.out.println("TestSynchronization.afterCompletion("+i+")");
    }
}