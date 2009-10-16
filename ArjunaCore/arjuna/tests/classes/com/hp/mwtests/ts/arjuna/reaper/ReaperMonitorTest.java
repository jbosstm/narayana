/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.AtomicAction;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.listener.ReaperMonitor;

import org.junit.Test;
import static org.junit.Assert.*;

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
    }

    public static boolean success = false;
}
