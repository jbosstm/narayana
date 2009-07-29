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

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.InputObjectState;

import org.junit.Test;
import static org.junit.Assert.*;

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
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(ArjunaNames.Implementation_ObjectStore_ActionLogStore().stringForm());
        arjPropertyManager.getCoordinatorEnvironmentBean().setTransactionLog(true);
        
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
            TxControl.getStore().allObjUids(new MyAtomicAction().type(), ios, ObjectStore.OS_UNKNOWN);

            Uid tempUid = new Uid(Uid.nullUid());

            tempUid.unpack(ios);

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
