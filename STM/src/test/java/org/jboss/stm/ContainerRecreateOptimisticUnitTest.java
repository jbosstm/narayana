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

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;

import junit.framework.TestCase;

/**
 * @author Mark Little
 */

public class ContainerRecreateOptimisticUnitTest extends TestCase
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
    
    public void testOptimisticRecreate ()
    {
        Container<Sample1> theContainer = new Container<Sample1>(Container.TYPE.RECOVERABLE, Container.MODEL.EXCLUSIVE);
        Sample1 obj1 = theContainer.create(new Sample1Imple(10));
        
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
        
        assertTrue(theContainer.getUidForHandle(obj1).notEquals(Uid.nullUid()));
        
        Sample1 obj2 = theContainer.clone(new Sample1Imple(), theContainer.getUidForHandle(obj1));

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
