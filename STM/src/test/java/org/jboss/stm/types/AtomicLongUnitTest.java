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
