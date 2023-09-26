/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

import org.jboss.stm.annotations.LockFree;
import org.jboss.stm.annotations.Transactional;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * @author Mark Little
 */

public class LockFreeUnitTest extends TestCase
{   
    @Transactional
    public interface Sample
    {
       public boolean increment ();
        
       public boolean decrement ();
       
       public int value ();
    }
    
    public class SampleLockable implements Sample
    {
        public SampleLockable ()
        {
            this(0);
        }
        
        public SampleLockable (int init)
        {
            _isState = init;
        }
        
        public int value ()
        {
            return _isState;
        }

        @LockFree
        public boolean increment ()
        {
            _isState++;

            if (AtomicAction.Current() == null)
                return false;
            else
                return true;
        }

        public boolean decrement ()
        {
            _isState--;
            
            if (AtomicAction.Current() == null)
                return false;
            else
                return true;
        }

        private int _isState;
    }

    public void test ()
    {
        Container<Sample> theContainer = new Container<Sample>();
        Sample obj = theContainer.create(new SampleLockable(10));
        
        assertTrue(obj != null);
        
        AtomicAction act = new AtomicAction();
        
        System.err.println("Started transaction: "+act);
        
        act.begin();
        
        boolean result = obj.increment();
        
        assertTrue(result);
        
        result = obj.decrement();
        
        assertTrue(result);
        
        act.commit();
    }
}