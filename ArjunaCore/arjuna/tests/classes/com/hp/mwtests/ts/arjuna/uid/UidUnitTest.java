/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.uid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.FatalError;

public class UidUnitTest
{
    @Test
    public void test () throws Exception
    {
        long[] dummy = {0, 0};
        
        Uid u = new Uid(dummy, 0, 0, 0);
        
        assertTrue(u.lessThan(Uid.maxUid()));
        assertTrue(u.getHexPid() != null);
        
        u.print(new PrintStream("foo"));
    }
    
    @Test
    public void testInvalid () throws Exception
    {
        Uid u = null;
        
        try
        {
            u = new Uid(null, 0, 0, 0);
            
            fail();
        }
        catch (final FatalError ex)
        {
        }   

        try
        {
            u = new Uid("hello world", false);
            
            fail();
        }
        catch (final FatalError ex)
        {
        }

        try
        {
            u = new Uid((String) null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            u = new Uid((byte[]) null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        byte[] b = {0, 0};
        u = new Uid(b);
        
        assertEquals(u.valid(), false);
    }
    
    @Test
    public void testComparisons () throws Exception
    {
        Uid u = new Uid();

        assertEquals(u.equals(new Object()), false);
        assertTrue(u.notEquals(null));
        assertEquals(u.notEquals(u), false);
        
        assertEquals(u.lessThan(null), false);
        assertEquals(u.greaterThan(null), false);
        assertEquals(u.greaterThan(u), false);
    }
    
    @Test
    public void testSerialization () throws Exception
    {
        Uid u1 = new Uid();
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bs);
        
        os.writeObject(u1);
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bs.toByteArray());
        ObjectInputStream is = new ObjectInputStream(bis);
        
        Uid u2 = (Uid) is.readObject();
        
        assertTrue(u1.equals(u2));
    }

    @Test
    public void testMaxMinUid () throws Exception
    {
        Uid minUid = Uid.minUid();
        Uid uid = new Uid();
        Uid maxUid = Uid.maxUid();

        assertTrue(minUid.lessThan(uid));
        assertTrue(minUid.lessThan(maxUid));
        assertTrue(uid.lessThan(maxUid));

        // make sure that the Uid comparator is sane

        assertTrue(uid.greaterThan(minUid));
        assertTrue(maxUid.greaterThan(minUid));
        assertTrue(maxUid.greaterThan(uid));
    }
}