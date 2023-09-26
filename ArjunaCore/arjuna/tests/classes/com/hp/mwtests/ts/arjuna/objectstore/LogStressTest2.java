/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.objectstore;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.arjuna.objectstore.LogStore;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;

/*
 * Run with the log store for N hours and make sure there are
 * no logs left at the end.
 */

public class LogStressTest2
{
    @Test
    public void test()
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(false);
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(LogStore.class.getName());
        arjPropertyManager.getObjectStoreEnvironmentBean().setTxLogSize(10000);

        int timeLimit = 4; // hours

        System.err.println("WARNING: this test will run for " + timeLimit + " hours.");

        final long stime = System.currentTimeMillis();
        final long endTime = timeLimit * 60 * 60 * 1000;
        long ftime;

        do {
            try {
                AtomicAction A = new AtomicAction();

                A.begin();

                A.add(new BasicRecord());

                A.commit();
            }
            catch (final Exception ex) {
            }

            ftime = System.currentTimeMillis();

        } while ((ftime - stime) < endTime);
    }
}