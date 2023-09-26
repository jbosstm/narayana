package unit;

import org.junit.Test;

/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


import org.jboss.stm.annotations.Optimistic;
import org.jboss.stm.annotations.Transactional;

/**
 * @author Mark Little
 */

public class SampleUnitTest
{   
    @Transactional
    @Optimistic
    public interface Sample
    {
       public void increment ();
       public void decrement ();
       
       public int value ();
    }
    
    @Transactional
    @Optimistic
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

        public void increment ()
        {
            _isState++;
        }

        public void decrement ()
        {
            _isState--;
        }

        private int _isState;
    }

    @Test
    public void test ()
    {
	/*
	 * Commented out until we get a fix in Narayana.
	 */

	/*
        Container<Sample> theContainer = new Container<Sample>();
        Sample obj1 = theContainer.create(new SampleLockable(10));
        Sample obj2 = theContainer.clone(new SampleLockable(), obj1);  // could we do this by inference (look at 2nd parameter) or by annotation?
        
        assertTrue(obj2 != null);
        
        AtomicAction act = new AtomicAction();
        
        act.begin();
        
        obj1.increment();
        
        act.commit();
        
        act = new AtomicAction();
        
        act.begin();
        
        assertEquals(obj2.value(), 11);
        
        act.commit();
	*/
    }
}