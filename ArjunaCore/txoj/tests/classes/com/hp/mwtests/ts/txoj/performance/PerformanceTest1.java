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

public class PerformanceTest1 extends PerformanceTestBase
{
    @Test
    public void recoverableTest()
    {
        long iters = 4;

        RecoverableObject foo = new RecoverableObject();
        AtomicAction A = new AtomicAction();
        long t1 = System.currentTimeMillis();

        A.begin();

        for (int c = 0; c < iters; c++)
        {
            foo.set(2);
        }

        A.commit();

        reportThroughput("recoverableTest", iters, t1);
    }

    @Test
    public void persistentTest()
    {
        long iters = 4;

        AtomicObject foo = new AtomicObject();
        AtomicAction A = new AtomicAction();
        long t1 = System.currentTimeMillis();

        A.begin();

        try {
            for (int c = 0; c < iters; c++)
            {
                foo.set(2);
            }
        }
        catch (TestException e)
        {
            fail("AtomicObject exception raised.");
        }

        A.commit();

        reportThroughput("persistentTest", iters, t1);
    }
}