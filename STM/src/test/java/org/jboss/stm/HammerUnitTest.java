/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

import java.util.Random;

import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.PersistentContainer;
import org.jboss.stm.internal.RecoverableContainer;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;

import junit.framework.TestCase;

/**
 * Hammer equivalent test.
 * 
 * @author Mark Little
 */

public class HammerUnitTest extends TestCase
{   
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
    
    public class Worker extends Thread
    {
        public Worker (Sample obj1, Sample obj2)
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
                    doCommit = false;
                }
                
                if (rand.nextInt() % 2 == 0)
                    doCommit = false;
                
                if (doCommit)
                    A.commit();
                else
                    A.abort();
            }
        }
        
        private Sample _obj1;
        private Sample _obj2;
    }

    public void testRecoverableHammer ()
    {
        RecoverableContainer<Sample> theContainer = new RecoverableContainer<Sample>();
        Sample obj1 = theContainer.enlist(new SampleLockable(10));
        Sample obj2 = theContainer.enlist(new SampleLockable(10));
        Worker worker1 = new Worker(obj1, obj2);
        Worker worker2 = new Worker(obj1, obj2);
        
        worker1.start();
        worker2.start();
        
        try
        {
            worker1.join();
            worker2.join();
        }
        catch (final Throwable ex)
        {
        }
        
        assertEquals(obj1.value()+obj2.value(), 20);
    }
    
    public void testPersistentHammer ()
    {
        PersistentContainer<Sample> theContainer = new PersistentContainer<Sample>();
        Sample obj1 = theContainer.enlist(new SampleLockable(10));
        Sample obj2 = theContainer.enlist(new SampleLockable(10));
        Worker worker1 = new Worker(obj1, obj2);
        Worker worker2 = new Worker(obj1, obj2);
        
        worker1.start();
        worker2.start();
        
        try
        {
            worker1.join();
            worker2.join();
        }
        catch (final Throwable ex)
        {
        }
        
        assertEquals(obj1.value()+obj2.value(), 20);
    }
    
    public void testPersistentHammerMULTIPLE ()
    {
        PersistentContainer<Sample> theContainer = new PersistentContainer<Sample>(ObjectModel.MULTIPLE);
        Sample obj1 = theContainer.enlist(new SampleLockable(10));
        Sample obj2 = theContainer.enlist(new SampleLockable(10));
        Worker worker1 = new Worker(obj1, obj2);
        Worker worker2 = new Worker(obj1, obj2);
        
        worker1.start();
        worker2.start();
        
        try
        {
            worker1.join();
            worker2.join();
        }
        catch (final Throwable ex)
        {
        }
        
        assertEquals(obj1.value()+obj2.value(), 20);
    }
}