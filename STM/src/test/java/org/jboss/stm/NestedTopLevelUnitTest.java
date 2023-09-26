/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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