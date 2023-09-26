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

public class AtomicTest
{
    @Test
    public void run()
    {
	AtomicObject foo = new AtomicObject();
	Uid u = foo.get_uid();

	AtomicAction A = new AtomicAction();

    try {
	    A.begin();

	    foo.set(2);

	    A.commit();

	    int finalVal = foo.get();

        assertEquals(2, finalVal);


	    foo = new AtomicObject(u);

	    A = new AtomicAction();

	    A.begin();

	    foo.set(4);

	    A.commit();

	    finalVal = foo.get();

        assertEquals(4, finalVal);

	}
	catch (TestException e)
	{
	    A.abort();

        fail("AtomicObject exception raised.");
	}

    }

}