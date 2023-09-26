/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.performance;

public class PerformanceTestBase
{
    protected void reportThroughput(String method, long iterations,
                              long startTimeMillis) {
        System.out.printf("%s.%s: Time taken to perform %d iterations: %d ms%n",
            getClass().getCanonicalName(), method, iterations,
            (System.currentTimeMillis() - startTimeMillis));
    }
}