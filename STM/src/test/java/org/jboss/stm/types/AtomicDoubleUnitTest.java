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

public class AtomicDoubleUnitTest extends TestCase
{
    public void test ()
    {
        AtomicDouble ad = AtomicFactory.instance().createDouble();
        
        assertEquals(ad.get(), (double) 0);
        
        ad.set(1);
        
        assertEquals(ad.get(), (double) 1);
        
        AtomicDouble temp = AtomicFactory.instance().createDouble(667);
        
        assertEquals(temp.get(), (double) 667);
        
        assertEquals(temp.subtract(ad).get(), (double) 666);
    }
}