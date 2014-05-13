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

import org.jboss.stm.annotations.TransactionFree;
import org.jboss.stm.annotations.Transactional;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * @author Mark Little
 */

public class TransactionFreeUnitTest extends TestCase
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

        @TransactionFree
        public boolean increment ()
        {
            _isState++;

            if (AtomicAction.Current() == null)
                return true;
            else
                return false;
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
        
        // implicitly checks that the context has been re-associated.
        
        result = obj.decrement();
        
        assertTrue(result);
        
        act.commit();
    }
}
