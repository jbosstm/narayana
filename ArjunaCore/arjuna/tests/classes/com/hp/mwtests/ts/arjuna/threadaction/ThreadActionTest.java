/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.threadaction;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.arjuna.thread.ThreadSetup;
import com.hp.mwtests.ts.arjuna.resources.BasicThreadedObject;

class DummySetup implements ThreadSetup
{
    public void setup ()
    {
        _called = true;
    }
    
    public boolean called ()
    {
        return _called;
    }
    
    private boolean _called = false;
}

public class ThreadActionTest
{
    @Test
    public void test()
    {
        BasicThreadedObject object1 = new BasicThreadedObject(true);
        BasicThreadedObject object2 = new BasicThreadedObject(false);

        System.out.println("Main thread has action " + BasicAction.Current());

        object1.start();
        object2.start();

        Thread.yield();

        try {
            object1.join();
            object2.join();
        }
        catch (InterruptedException e) {
        }

        System.out.println("Main thread has action " + BasicAction.Current());
    }

    @Test
    public void testOthers ()
    {
        DummySetup ds = new DummySetup();
        
        ThreadActionData.addSetup(ds);

        AtomicAction A = new AtomicAction();
        AtomicAction B = new AtomicAction();
        
        A.begin();
        B.begin();      
        
        assertTrue(ThreadActionData.currentAction() != null);
        
        ThreadActionData.restoreActions(B);
        
        assertEquals(ThreadActionData.popAction(), B);
        
        ThreadActionData.purgeActions(Thread.currentThread());
        
        assertTrue(ds.called());
        
        ThreadActionData.removeSetup(ds);
        
        ThreadActionData.popAction(Thread.currentThread().getName());
    }
}