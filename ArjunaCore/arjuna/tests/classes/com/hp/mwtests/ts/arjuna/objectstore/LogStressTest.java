/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.objectstore;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.LogStore;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;

/*
 * Define our own transaction type to avoid conflicts
 * with other tests.
 */

class MyAtomicAction extends AtomicAction
{
    public String type ()
    {
        return "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/MyAtomicAction";
    }
}

class StressWorker extends Thread
{
    public StressWorker(int iters, int thread)
    {
        _iters = iters;
        _thread = thread;
    }

    public void run()
    {
        for (int i = 0; i < _iters; i++) {
            try {
                MyAtomicAction A = new MyAtomicAction();

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
    private int _thread;
}

public class LogStressTest
{
    @Test
    public void test()
    {
        int threads = 10;
        int work = 100;

        arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(false);
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(LogStore.class.getName());

        // the byteman script will manage this
        //System.setProperty(Environment.TRANSACTION_LOG_PURGE_TIME, "10000");

        StressWorker[] workers = new StressWorker[threads];

        for (int i = 0; i < threads; i++) {
            workers[i] = new StressWorker(work, i);

            workers[i].start();
        }

        for (int j = 0; j < threads; j++) {
            try {
                workers[j].join();
            }
            catch (final Exception ex) {
            }
        }

        InputObjectState ios = new InputObjectState();
        boolean passed = false;

        try {
            StoreManager.getRecoveryStore().allObjUids(new MyAtomicAction().type(), ios, StateStatus.OS_UNKNOWN);

            Uid tempUid = UidHelper.unpackFrom(ios);

            // there should be no entries left

            if (tempUid.equals(Uid.nullUid())) {
                passed = true;
            }
        }
        catch (final Exception ex) {
        }

        assertTrue(passed);
    }
}