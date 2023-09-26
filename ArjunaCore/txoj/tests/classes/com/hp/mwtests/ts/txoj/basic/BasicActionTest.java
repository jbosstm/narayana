/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.basic;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class BasicActionTest
{
    @Test
    public void run() throws TestException
    {
	AtomicObject foo = null;
	Uid u = null;

	if (u == null)
	    foo = new AtomicObject();
	else
	    foo = new AtomicObject(u);

	AtomicAction A = new AtomicAction();

        int value  = foo.get();
	try
	{
	    A.begin();

		foo.set(foo.get()+2);

	    A.commit();

        assertEquals(value+2, foo.get());

	}
	catch (Exception e)
	{
	    A.abort();
        fail("AtomicObject exception raised.");
	}

	System.out.println("\nWill now try some erroneous conditions.\n");

	    AtomicAction B = new AtomicAction();

	    u = new Uid();
	    foo = new AtomicObject(u);

	    B.begin();

	    try
	    {
		System.out.println("attempting to get value from non-existent object: "+foo.get());
	    }
	    catch (Exception e)
	    {
	    }

	    System.out.println("trying to set value to 5");

	    try
	    {
		foo.set(5);
	    }
	    catch (Exception e)
	    {
	    }

	    try
	    {
		System.out.println("attempting to get value again: "+foo.get());
	    }
	    catch (Exception e)
	    {
	    }

	    B.commit();
	}

}