/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
