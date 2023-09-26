/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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