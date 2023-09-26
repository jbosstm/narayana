/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance;

public class PerformanceTestRunnerThread extends Thread
{
    private int                     _numberOfIterations = 0;
    private PerformanceTest         _test = null;
    private boolean                 _failed = false;

    public PerformanceTestRunnerThread(PerformanceTest test, int numberOfIterations)
    {
        _numberOfIterations = numberOfIterations;
        _test = test;
    }

    public boolean success()
    {
        return !_failed;
    }

    public void run()
    {
        try
        {
            _test.performWork(_numberOfIterations);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            _failed = true;
        }
    }

}