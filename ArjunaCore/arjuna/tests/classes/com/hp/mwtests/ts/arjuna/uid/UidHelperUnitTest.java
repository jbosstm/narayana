/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.uid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

public class UidHelperUnitTest
{
    @Test
    public void test () throws Exception
    {
        Uid u = new Uid("hello", true);  // should be invalid!
        
        assertEquals(u.valid(), false);
        
        try
        {
            u = UidHelper.unpackFrom(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            UidHelper.packInto(null, new OutputObjectState());
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            UidHelper.packInto(u, new OutputObjectState());
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
    }
}