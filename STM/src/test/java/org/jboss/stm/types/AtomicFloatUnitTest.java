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

public class AtomicFloatUnitTest extends TestCase
{
    public void test ()
    {
        AtomicFloat af = AtomicFactory.instance().createFloat();
        
        assertEquals(af.get(), (float) 0);
        
        af.set(1);
        
        assertEquals(af.get(), (float) 1);
        
        AtomicFloat temp = AtomicFactory.instance().createFloat(667);
        
        assertEquals(temp.get(), (float) 667);
        
        assertEquals(temp.subtract(af).get(), (float) 666);
    }
}