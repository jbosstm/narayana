/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.destroy;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class DestroyTest
{
    @Test
    public void test() throws TestException
    {
        AtomicObject atomicObject = new AtomicObject();
        Uid u = atomicObject.get_uid();
        AtomicAction a = new AtomicAction();

        a.begin();

        atomicObject.set(10);

        assertTrue(atomicObject.destroy());

        a.commit();

        atomicObject = new AtomicObject(u);
        
        int val;
        
        try
        {
            val = atomicObject.get();
        }
        catch (final TestException ex)
        {
            // activate should fail so setlock should fail
            
            val = -2;  // differentiate between -1
        }
        
        assertEquals(-2, val);
    }
}