/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.nestedtoplevelaction;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.TopLevelAction;
import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class NestedTopLevelAction
{
    @Test
    public void test()
    {
        AtomicAction A = new AtomicAction();
        TopLevelAction B = new TopLevelAction();
        AtomicAction C = new AtomicAction();
        AtomicObject foo1 = new AtomicObject();
        AtomicObject foo2 = new AtomicObject();

        try
        {
            A.begin();

            foo1.set(5);

            System.out.println("Current atomic object 1 state: " + foo1.get());

            System.out.println("\nStarting nested top-level action.");

            B.begin();

            System.out.println(B);

            foo2.set(7);

            System.out.println("Current atomic object 2 state: " + foo2.get());

            System.out.println("\nCommitting nested top-level action.");

            B.commit();

            System.out.println("\nAborting top-level action.");

            A.abort();

            C.begin();

            int val1 = foo1.get();
            int val2 = foo2.get();

            System.out.println("\nFinal atomic object 1 state: " + val1);

            assertEquals(0, val1);

            System.out.println("\nFinal atomic object 2 state: " + val2);

            assertEquals(7, val2);

            C.commit();
        }
        catch (TestException e)
        {
            A.abort();
            B.abort();
            C.abort();

            fail("AtomicObject exception raised.");
        }

    }

}