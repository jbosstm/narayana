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

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.types.AtomicArrayImpl;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * Unit tests for the AtomicInteger class.
 * 
 * @author Mark Little
 */

public class ExtendedArrayUnitTest extends TestCase
{
    @Transactional
    public interface Sample
    {
        @WriteLock
        public void set (int val);
     
        @ReadLock
        public int get ();
    }
    
    @Transactional
    public class SampleLockable implements Sample
    {
        public SampleLockable ()
        {
            this(0);
        }
        
        public SampleLockable (int val)
        {
            _val = val;
        }
        
        @ReadLock
        public int get ()
        {
            return _val;
        }

        @WriteLock
        public void set (int val)
        {
            _val = val;
        }

        @State
        private int _val;
    }
    
    @SuppressWarnings("unchecked")
    public void test ()
    {
        AtomicArray<Sample> a1 = ArrayFactory.instance().createArray();
        
        a1.set(0, new SampleLockable());
        a1.set(1, new SampleLockable());
        
        assertEquals(a1.get(0).get(), 0);
        assertEquals(a1.get(1).get(), 0);
    }
    
    @SuppressWarnings("unchecked")
    public void testTransaction ()
    {
        AtomicAction act = new AtomicAction();
        AtomicArray<Sample> a1 = ArrayFactory.instance().createArray();
        
        a1.set(0, new SampleLockable());
        a1.set(1, new SampleLockable());
        
        assertEquals(a1.get(0).get(), 0);
        assertEquals(a1.get(1).get(), 0);
        
        act.begin();
        
        a1.set(0, new SampleLockable(1));
        a1.set(1, new SampleLockable(1));
        
        assertEquals(a1.get(0).get(), 1);
        assertEquals(a1.get(1).get(), 1);
        
        act.abort();
        
        assertEquals(a1.get(0).get(), 0);
        assertEquals(a1.get(1).get(), 0);
    }
}
