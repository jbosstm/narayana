/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.uid;



import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;

public class UidUniqueness
{
    @Test
    public void test()
    {
        for (int i = 0; i < 100; i++) {
            Uid u = new Uid();
            System.out.println(u + " " + u.hashCode());
        }
        
        assertTrue(Uid.maxUid().greaterThan(Uid.minUid()));
        
        try
        {
            Uid a = new Uid();
            Uid b = (Uid) a.clone();
            
            assertTrue(a.equals(b));
        }
        catch (final Exception ex)
        {
            fail();
        }
    }

}