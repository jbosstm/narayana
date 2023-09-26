/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.types;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * Unit tests for the AtomicInteger class.
 * 
 * @author Mark Little
 */

public class AtomicIntegerUnitTest extends TestCase
{
    public void test ()
    {
        AtomicInteger ai = AtomicFactory.instance().createInteger();
        
        assertEquals(ai.get(), 0);
        
        ai.set(1);
        
        assertEquals(ai.get(), 1);
        
        AtomicInteger temp = AtomicFactory.instance().createInteger(667);
        
        assertEquals(temp.get(), 667);
        
        assertEquals(temp.subtract(ai).get(), 666);
        
        assertEquals(ai.increment().get(), 2);
        
        assertEquals(ai.decrement().get(), 1);
    }
    
    public void testTransaction ()
    {
        AtomicAction act = new AtomicAction();
        AtomicInteger ai = AtomicFactory.instance().createInteger();

        assertEquals(ai.get(), 0);
        
        /*
         * Do this within a transaction so we can roll it all back
         * afterwards.
         */

        act.begin();
        
        ai.set(1);
        
        assertEquals(ai.get(), 1);

        act.abort();

        assertEquals(ai.get(), 0);
    }
}