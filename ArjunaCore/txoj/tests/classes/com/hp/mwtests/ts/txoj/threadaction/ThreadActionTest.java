/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.threadaction;



import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.hp.mwtests.ts.txoj.common.resources.BasicThreadedObject;

public class ThreadActionTest
{
    @Test
    public void test()
    {
	BasicThreadedObject object1 = new BasicThreadedObject(true);
	BasicThreadedObject object2 = new BasicThreadedObject(false);

	System.out.println("Main thread has action "+BasicAction.Current());

	assertEquals(BasicAction.Current(), null);
	
	object1.start();
	object2.start();

	Thread.yield();
	
	try
	{
	    object1.join();
	    object2.join();
	}
	catch (InterruptedException e)
	{
	}

	System.out.println("Main thread has action "+BasicAction.Current());
	
	assertEquals(BasicAction.Current(), null);
    }
}