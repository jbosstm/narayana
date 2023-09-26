/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

import org.jboss.stm.annotations.NotState;
import org.jboss.stm.annotations.Optimistic;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * @author Mark Little
 */

public class TypicalExampleUnitTest extends TestCase
{   
    @Transactional
    public interface Sample
    {
       public void increment ();
       public void decrement ();
       
       public int value ();
    }

    public class MyExample implements Sample
    {
        public MyExample ()
        {
            this(0);
        }
        
        public MyExample (int init)
        {
            _isState = init;
        }
        
        @ReadLock
        public int value ()
        {
            checkTransaction();
            
            return _isState;
        }

        @WriteLock
        public void increment ()
        {
            checkTransaction();
            
            _isState++;
        }

        @WriteLock
        public void decrement ()
        {
            checkTransaction();
            
            _isState--;
        }

        private void checkTransaction ()
        {
            if (AtomicAction.Current() != null)
                _numberOfTransactions++;
        }
        
        private int _isState;
        
        @NotState
        public int _numberOfTransactions;
    }

    public void test ()
    {
        MyExample ex = new MyExample(10);
        Container<Sample> theContainer = new Container<Sample>();
        Sample obj1 = theContainer.create(ex);
        Sample obj2 = theContainer.clone(new MyExample(), obj1);
        
        assertTrue(obj2 != null);
        
        AtomicAction act = new AtomicAction();
        
        act.begin();
        
        obj1.increment();
        
        act.commit();
        
        act = new AtomicAction();
        
        act.begin();
        
        assertEquals(obj2.value(), 11);
        
        act.commit();
        
        act = new AtomicAction();
        
        act.begin();
        
        for (int i = 0; i < 1000; i++)
        {
            obj1.increment();
        }
        
        act.abort();
        
        assertEquals(ex._numberOfTransactions, 1002);
    }
}