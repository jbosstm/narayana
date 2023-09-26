/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.types;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * Unit tests for the AtomicInteger class.
 * 
 * @author Mark Little
 */

public class AtomicArrayUnitTest extends TestCase
{   
    @SuppressWarnings("unchecked")
    public void test ()
    {
        AtomicArray<Integer> a1 = ArrayFactory.instance().createArray();
        
        for (int i = 0; i < a1.size(); i++)
            a1.set(i, i);
        
        for (int j = 0; j < a1.size(); j++)
            assertTrue(a1.get(j).equals(j));
    }
    
    @SuppressWarnings("unchecked")
    public void testTransaction ()
    {
        AtomicAction act = new AtomicAction();
        AtomicArray<Integer> a1 = ArrayFactory.instance().createArray(10);
        
        assertEquals(a1.size(), 10);
        
        for (int i = 0; i < a1.size(); i++)
            a1.set(i, 0);
        
        act.begin();
        
        a1.set(0, 1);
        a1.set(1, 2);
        
        assertTrue(a1.get(0).equals(1));
        assertTrue(a1.get(1).equals(2));
        
        act.abort();
        
        assertTrue(a1.get(0).equals(0));
        assertTrue(a1.get(1).equals(0));
    }
}