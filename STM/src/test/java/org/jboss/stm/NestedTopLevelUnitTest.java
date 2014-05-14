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

package org.jboss.stm;

import java.io.IOException;
import java.util.Hashtable;

import org.jboss.stm.annotations.NestedTopLevel;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class NestedTopLevelUnitTest extends TestCase
{
    @Transactional
    @NestedTopLevel
    public interface Counter
    {
        public void increment ();
        public int count ();
    }
    
    public class CounterImple implements Counter
    {
        @ReadLock
        public int count ()
        {
            return _count;
        }

        @WriteLock
        public void increment ()
        {
            _count++;
        }
        
        private int _count = 0;
    }
    
    public void test () throws Exception
    {
        Counter dt2 = new Container<Counter>().create(new CounterImple());
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        dt2.increment();

        A.abort();
        
        assertTrue(dt2.count() == 1);
    }
}
