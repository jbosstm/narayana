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

public class AtomicBooleanUnitTest extends TestCase
{
    public void test ()
    {
        AtomicBoolean ab = AtomicFactory.instance().createBoolean();
        
        assertEquals(ab.get(), false);
        
        ab.set(true);
        
        assertEquals(ab.get(), true);
        
        AtomicBoolean temp = AtomicFactory.instance().createBoolean(false);
        
        assertEquals(temp.get(), false);
        
        assertFalse((ab.and(temp).get()));
        assertTrue((ab.or(temp).get()));
        
        assertTrue((ab.xor(temp).get()));
        
        temp.set(true);
        
        assertFalse((ab.xor(temp).get()));
    }
}