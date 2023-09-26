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
import org.jboss.stm.internal.RecoverableContainer;
import org.jboss.stm.internal.proxy.OptimisticLockManagerProxy;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;

import junit.framework.TestCase;

/**
 * @author Mark Little
 */

public class ContainerUnitTest extends TestCase
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
   
    public void testPessimistic ()
    {
        Container<Sample2> theContainer = new Container<Sample2>();
        Sample2 obj1 = theContainer.create(new Sample2Imple(10));
        Sample2 obj2 = theContainer.clone(new Sample2Imple(), obj1);
        
        assertTrue(obj1 != null);
        assertTrue(obj2 != null);
        
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        obj1.increment();
        obj2.value();
        
        A.commit();
        
        A = new AtomicAction();
        
        A.begin();
        
        obj1.increment();       
        obj2.increment();
        
        A.commit();
        
        assertEquals(obj1.value(), 13);
        assertEquals(obj2.value(), 13);
    }
    
    public void testOptimisticHammer ()
    {
        if (true)
            return;
        
        Container<Sample1> theContainer = new Container<Sample1>();
        Sample1 obj1 = theContainer.create(new Sample1Imple(10));
        Sample1 obj2 = theContainer.create(new Sample1Imple(10));
        Sample1 obj3 = theContainer.clone(new Sample1Imple(), obj1);
        Sample1 obj4 = theContainer.clone(new Sample1Imple(), obj2);
        int workers = 2;
        Worker[] worker = new Worker[workers];
        
        assertTrue(obj3 != null);
        assertTrue(obj4 != null);
        
        /*
         * TODO cannot share state until the state is written, and it isn't written
         * until an explicit set is called. What we want is for the state to be in the
         * store when create (clone) returns.
         * 
         * So currently you need to force the saving of the object states (we use increment
         * here for that purpose) and then force a read of the state through the clones
         * (we use value for that purpose). Not as opaque as we'd like and we should be able
         * to force the save and restore via the proxy classes.
         */
        
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        obj1.increment();
        obj2.increment();
        
        A.commit();
        
        assertEquals(obj1.value(), 11);
        assertEquals(obj2.value(), 11);
        
        A = new AtomicAction();
        
        A.begin();
        
        assertEquals(obj3.value(), 11);
        
        A.commit();
        
        worker[0] = new Worker(obj1, obj2);
        worker[1] = new Worker(obj3, obj4);
       
        for (int j = 0; j < workers; j++)
            worker[j].start();
        
        try
        {
            for (int k = 0; k < workers; k++)
                worker[k].join();
        }
        catch (final Throwable ex)
        {
        }

        assertEquals(obj1.value()+obj2.value(), 22);
        
        @SuppressWarnings("unchecked")
        RecoverableContainer<Sample1> cont = ((OptimisticLockManagerProxy<Sample1>) obj1).getContainer();
        
        assertTrue(cont != null);
        
        @SuppressWarnings("unchecked")
        Container<Sample1> duplicateContainer = (Container<Sample1>) Container.getContainer(obj1);
        
        assertTrue(duplicateContainer != null);
    }
}