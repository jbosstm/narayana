/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.objectstore;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.objectstore.LogStore;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;

public class LogStoreRecoveryTest2
{
private class TestWorker extends Thread
{
    public TestWorker(int iters)
    {
        _iters = iters;
    }

    public void run()
    {
        for (int i = 0; i < _iters; i++) {
            try {
                AtomicAction A = new AtomicAction();

                A.begin();

                A.add(new BasicRecord());

                A.commit();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            Thread.yield();
        }
    }

    private int _iters;
}

    @Test
    public void test()
    {
        int threads = 10;
        int work = 100;

        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);

        arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(false);
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(LogStore.class.getName());
        arjPropertyManager.getObjectStoreEnvironmentBean().setSynchronousRemoval(false);
        // the byteman script will enforce this
        //System.setProperty(Environment.TRANSACTION_LOG_PURGE_TIME, "1000000");  // essentially infinite

        TestWorker[] workers = new TestWorker[threads];

        for (int i = 0; i < threads; i++) {
            workers[i] = new TestWorker(work);

            workers[i].start();
        }

        for (int j = 0; j < threads; j++) {
            try {
                workers[j].join();
                System.err.println("**terminated " + j);
            }
            catch (final Exception ex) {
            }
        }

        /*
           * Now have a log that hasn't been deleted. Run recovery and see
           * what happens!
           */

        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);

        manager.scan();
    }
}