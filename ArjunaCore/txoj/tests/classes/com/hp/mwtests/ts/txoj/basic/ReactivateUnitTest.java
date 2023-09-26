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
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class ReactivateUnitTest
{
    @Test
    public void test()
    {
        try {
            AtomicObject obj = new AtomicObject();
            Uid objRef = obj.get_uid();

            AtomicAction A = new AtomicAction();

            A.begin();

            obj.set(1234);

            A.commit();

            AtomicObject recObj = new AtomicObject(objRef);

            AtomicAction B = new AtomicAction();

            B.begin();

            assertEquals(1234, recObj.get());

            B.abort();
        }
        catch (Exception ex)
        {
            fail(ex.toString());
        }
    }
}