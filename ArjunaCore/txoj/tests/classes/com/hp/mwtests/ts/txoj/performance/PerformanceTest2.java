/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.performance;



import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.hp.mwtests.ts.txoj.common.resources.RecoverableObject;

public class PerformanceTest2 extends PerformanceTestBase
{
    @Test
    public void recoverableTest()
    {
        long iters = 1000;

        RecoverableObject foo = new RecoverableObject();
        AtomicAction A = null;
        long t1 = System.currentTimeMillis();

        for (int c = 0; c < iters; c++)
        {
            A = new AtomicAction();

            A.begin();

            foo.set(2);

            A.commit();
        }

        reportThroughput("recoverableTest", iters, t1);
    }

    @Test
    public void persistentTest()
    {
        long iters = 1000;

        AtomicObject foo = new AtomicObject();
        AtomicAction A = null;
        long t1 = System.currentTimeMillis();

        try
        {
            for (int c = 0; c < iters; c++)
            {
                A = new AtomicAction();

                A.begin();

                foo.set(2);

                A.commit();
            }
        }
        catch (TestException e)
        {
            if (A != null)
                A.abort();

            fail("AtomicObject exception raised.");
        }

        reportThroughput("persistentTest", iters, t1);
    }
}