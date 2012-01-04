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
package com.hp.mwtests.ts.arjuna.threadaction;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ThreadActionTest.java 2342 2006-03-30 13:06:17Z  $
 */

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
