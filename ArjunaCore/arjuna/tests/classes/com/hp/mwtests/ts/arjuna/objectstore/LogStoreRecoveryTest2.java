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
 * (C) 2005-2009,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Performance2.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.hp.mwtests.ts.arjuna.resources.*;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.objectstore.LogStore;

import org.junit.Test;
import static org.junit.Assert.*;

class TestWorker extends Thread
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

public class LogStoreRecoveryTest2
{
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
