/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.basic;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.hp.mwtests.ts.txoj.common.resources.RecoverableObject;

public class RecoverableTest
{
    @Test
    public void test()
    {
        RecoverableObject foo = new RecoverableObject();

        AtomicAction A = new AtomicAction();

        A.begin();

        foo.set(2);

        assertEquals(2, foo.get());

        A.abort();

        assertEquals(0, foo.get());

        AtomicAction B = new AtomicAction();

        B.begin();

        foo.set(4);

        assertEquals(4, foo.get());

        B.commit();

        assertEquals(4, foo.get());
    }
}