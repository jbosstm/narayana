/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

import java.util.Random;

import org.jboss.stm.annotations.Optimistic;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;

import junit.framework.TestCase;

/**
 * @author Mark Little
 */

public class ContainerRecreatePessimisticUnitTest extends TestCase
{   
    @Transactional
    @Optimistic
    public interface Sample1
    {
       public void increment ();
       public void decrement ();
       
       public int value ();
    }
    
    @Transactional
    public interface Sample2
    {
       public void increment ();
       public void decrement ();
       
       public int value ();
    }
    
    public class Sample1Imple implements Sample1
    {
        public Sample1Imple ()
        {
            this(0);
        }
        
        public Sample1Imple (int init)
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
    
    public class Sample2Imple implements Sample2
    {
        public Sample2Imple ()
        {
            this(0);
        }
        
        public Sample2Imple (int init)
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
    
    public class Worker extends Thread
    {
        public Worker (Sample1 obj1, Sample1 obj2)
        {
            _obj1 = obj1;
            _obj2 = obj2;
        }
        
        public void run ()
        {
            Random rand = new Random();

            for (int i = 0; i < 2; i++)
            {
                AtomicAction A = new AtomicAction();
                boolean doCommit = true;
                
                A.begin();
                
                try
                {
                    // always keep the two objects in sync.

                   _obj1.increment();
                   _obj2.decrement();
                }
                catch (final Throwable ex)
                {
                    ex.printStackTrace();
                    
                    doCommit = false;
                }
                
                if (rand.nextInt() % 2 == 0)
                    doCommit = false;
                
                if (doCommit)
                {
                    A.commit();
                }
                else
                {
                    A.abort();
                }
            }
        }
        
        private Sample1 _obj1;
        private Sample1 _obj2;
    }
    
    public void testPessimisticRecreate ()
    {
        Container<Sample2> theContainer = new Container<Sample2>(Container.TYPE.PERSISTENT, Container.MODEL.SHARED);
        Sample2 obj1 = theContainer.create(new Sample2Imple(10));
        
        assertTrue(obj1 != null);
        
        /*
         * Do some basic checks and ensure state is in store prior to sharing.
         */
        
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        obj1.increment();
        obj1.decrement();
        
        A.commit();
        
        assertEquals(obj1.value(), 10);
        
        assertTrue(theContainer.getIdentifier(obj1).notEquals(Uid.nullUid()));
        
        Sample2 obj2 = theContainer.clone(new Sample2Imple(), theContainer.getIdentifier(obj1));

        assertTrue(obj2 != null);
        
        A = new AtomicAction();
        
        A.begin();
        
        obj2.increment();
        
        A.commit();
        
        assertEquals(obj2.value(), 11);
        
        A = new AtomicAction();
        
        A.begin();
        
        assertEquals(obj1.value(), obj2.value());
        
        A.commit();
    }
}