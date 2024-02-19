/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.reaper;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReaperTestCase4 extends ReaperTestCaseControl {

    @Test
    public void testReaperForce() throws Exception
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setTxReaperMode(TransactionReaper.PERIODIC);
        arjPropertyManager.getCoordinatorEnvironmentBean().setTxReaperTimeout(100);
        try {
            shutdown(true);
        } finally {
            arjPropertyManager.getCoordinatorEnvironmentBean().setTxReaperMode(TransactionReaper.DYNAMIC);
            arjPropertyManager.getCoordinatorEnvironmentBean().setTxReaperTimeout(TransactionReaper.defaultCheckPeriod);
        }
    }

    private void shutdown(boolean wait) throws InterruptedException {
        TransactionReaper reaper = TransactionReaper.transactionReaper();

        // give the reaper worker time to start too

        Thread.sleep(1000);

        // create test reapables some of which will not respond immediately to cancel requests

        Uid uid0 = new Uid();
        Uid uid1 = new Uid();
        Uid uid2 = new Uid();
        Uid uid3 = new Uid();

        // reapable0 will return CANCELLED from cancel and will rendezvous inside the cancel call
        // so we can delay it. prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable0 = new TestReapable(uid0, true, false, false, false);
        // reapable1 will return CANCELLED from cancel and will not rendezvous inside the cancel call
        // prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable1 = new TestReapable(uid1, true, false, false, false);
        // reapable2 will return CANCELLED from cancel and will not rendezvous inside the cancel call
        // prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable2 = new TestReapable(uid2, true, false, false, false);
        // reapable3 will return CANCELLED from cancel and will not rendezvous inside the cancel call
        // prevent_commit should not get called so we don't care about the arguments
        TestReapable reapable3 = new TestReapable(uid3, true, false, false, false);

        reaper.insert(reapable0, 1);

        reaper.insert(reapable1, 2);

        reaper.insert(reapable2, 3);

        reaper.insert(reapable3, 4);

        long start = System.currentTimeMillis();

        TransactionReaper.terminate(wait);

        long duration = System.currentTimeMillis() - start;

        if (wait) {
            assertTrue(String.valueOf(duration), duration < 4500);
        } else {
            assertTrue(String.valueOf(duration), duration < 500);
        }

        assertEquals(0, reaper.numberOfTransactions());

        assertTrue(reapable0.getCancelTried());
        assertTrue(reapable1.getCancelTried());
        assertTrue(reapable2.getCancelTried());
        assertTrue(reapable3.getCancelTried());
    }

}