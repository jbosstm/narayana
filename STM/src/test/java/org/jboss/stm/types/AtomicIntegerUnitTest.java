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
