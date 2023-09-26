/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.types;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Unit tests for the AtomicInteger class.
 * 
 * @author Mark Little
 */

public class AtomicLongUnitTest extends TestCase
{
    public void test ()
    {
        AtomicLong al = AtomicFactory.instance().createLong();
        
        assertEquals(al.get(), 0);
        
        al.set(1);
        
        assertEquals(al.get(), 1);
        
        AtomicLong temp = AtomicFactory.instance().createLong(667);
        
        assertEquals(temp.get(), 667);
        
        assertEquals(temp.subtract(al).get(), 666);
        
        assertEquals(al.increment().get(), 2);
        
        assertEquals(al.decrement().get(), 1);
    }
}