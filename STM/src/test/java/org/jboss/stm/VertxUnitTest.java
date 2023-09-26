/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

import java.io.IOException;

import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * @author Mark Little
 */

class DummyVerticle
{
    public DummyVerticle ()
    {
        transactionalObject = theContainer.create(new SampleLockable(10));

        System.out.println("Object name: "+theContainer.getIdentifier(transactionalObject));
    }

    @Transactional
    public interface Sample
    {
       public void increment ();
       public void decrement ();
       
       public int value ();
    }

    public class SampleLockable implements Sample
    {
        public SampleLockable (int init)
        {
            _isState = init;
        }
        
        @ReadLock
        public int value ()
        {
            return _isState;
        }

        @WriteLock
        public void increment ()
        {
            _isState++;
        }
        
        @WriteLock
        public void decrement ()
        {
            _isState--;
        }

        @State
        private int _isState;
    }

    static public int value ()
    {
        AtomicAction A = new AtomicAction();
        int result = -1;

        A.begin();

        transactionalObject.increment();

        result = transactionalObject.value();

        A.commit();

        return result;
    }

    static final private Container<Sample> theContainer = new Container<Sample>("Demo", Container.TYPE.PERSISTENT, Container.MODEL.SHARED);
    static private Sample transactionalObject = null;
}

public class VertxUnitTest extends TestCase
{
    public void testVerticle()
    {
      DummyVerticle vert = new DummyVerticle();

      // do something with verticle

      AtomicAction A = new AtomicAction();

      A.begin();

      int amount = vert.value();
      
      A.commit();  // flush state to disk (if relevant)!
      
      assertEquals(amount, 11);  // initial state of 10 plus 1 from call to value (increment).
      
      A = new AtomicAction();
      
      A.begin();
      
      amount = vert.value();

      A.abort();

      assertEquals(vert.value(), amount);
    }
}