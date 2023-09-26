/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.reaper;

import static org.junit.Assert.assertTrue;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.listener.ReaperMonitor;

@RunWith(BMUnitRunner.class)
@BMScript("reaper")
public class ReaperMonitorTest
{
    class DummyMonitor implements ReaperMonitor
    {
        public synchronized void rolledBack (Uid txId)
        {
            success = true;
            notify();
            notified = true;
        }
        
        public synchronized void markedRollbackOnly (Uid txId)
        {
            success = false;
            notify();
            notified = true;
        }
        
        public boolean success = false;
        public boolean notified = false;

        public synchronized boolean checkSucceeded(int msecsTimeout)
        {
            if (!notified) {
                try {
                    wait(msecsTimeout);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            return success;
        }
    }
    
    @Test
    public void test()
    {
        TransactionReaper reaper = TransactionReaper.transactionReaper();
        DummyMonitor listener = new DummyMonitor();
       
        reaper.addListener(listener);
        
        AtomicAction A = new AtomicAction();

        A.begin();

        /*
         * the reaper byteman script will make sure we synchronize with the reaper after this call
         * just before it schedules the reapable for processing. the timout in the check method is
         * there in case something is really wrong and the reapable does not get cancelled
         */
        reaper.insert(A, 1);

        assertTrue(listener.checkSucceeded(30 * 1000));

        assertTrue(reaper.removeListener(listener));

        // insert a new transaction with a longer timeout and check that we can find the remaining time

        A = new AtomicAction();

        reaper.insert(A, 1000);

        long remaining = reaper.getRemainingTimeoutMills(A);

        assertTrue(remaining != 0);

        // ok now remove it

        reaper.remove(A);

    }

    public static boolean success = false;
}