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

package org.jboss.stm;

import java.util.Random;

import org.jboss.stm.annotations.Optimistic;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.RecoverableContainer;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * Hammer equivalent test.
 * 
 * @author Mark Little
 */

public class OptimisticHammerUnitTest extends TestCase
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

            for (int i = 0; i < 10; i++)
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
        
        private Sample _obj1;
        private Sample _obj2;
    }

    public void testRecoverableHammer ()
    {
        RecoverableContainer<Sample> theContainer = new RecoverableContainer<Sample>();
        Sample obj1 = theContainer.enlist(new SampleLockable(10));
        Sample obj2 = theContainer.enlist(new SampleLockable(10));       
        Sample obj3 = theContainer.enlist(new SampleLockable(0), theContainer.getUidForHandle(obj1));
        Sample obj4 = theContainer.enlist(new SampleLockable(0), theContainer.getUidForHandle(obj1));
        int workers = 2;
        Worker[] worker = new Worker[workers];
        
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
        
        assertEquals(obj1.value()+obj2.value(), 20);
    }
}
