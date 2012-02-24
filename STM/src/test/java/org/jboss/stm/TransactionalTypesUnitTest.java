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

import java.io.IOException;
import java.util.Hashtable;

import org.jboss.stm.annotations.RestoreState;
import org.jboss.stm.annotations.SaveState;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.RecoverableContainer;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class TransactionalTypesUnitTest extends TestCase
{
    @Transactional
    public interface Identifier
    {
        @ReadLock
        public String name ();
    }
    
    @Transactional
    public interface Counter
    {
        @WriteLock
        public void increment ();
        
        @ReadLock
        public int count ();
    }
    
    @Transactional
    public interface NamedCounter
    {
        @WriteLock
        public void setCounter (Counter c);
        
        @ReadLock
        public Counter getCounter ();
        
        @WriteLock
        public void setName (Identifier n);
        
        @ReadLock
        public Identifier getName ();
    }
    
    public class IdentifierImple implements Identifier
    {
        public IdentifierImple (final String n)
        {
            _name = n;
        }
        
        @ReadLock
        public String name ()
        {
            return _name;
        }
        
        private final String _name;
    }
    
    public class CounterImple implements Counter
    {
        @ReadLock
        public int count ()
        {
            return _count;
        }

        @WriteLock
        public void increment ()
        {
            _count++;
        }
        
        private int _count = 0;
    }
    
    public class NamedCounterImple implements NamedCounter
    {
        @ReadLock
        public Counter getCounter ()
        {
            return _count;
        }

        @ReadLock
        public Identifier getName ()
        {
            return _name;
        }

        @WriteLock
        public void setCounter (Counter c)
        {
            _count = c;
        }

        @WriteLock
        public void setName (Identifier n)
        {
            _name = n;
        }
        
        @State
        private Identifier _name;
        
        @State
        private Counter _count;
    }
    
    public void testBasicAssignment () throws Exception
    {
        Identifier dt1 = new RecoverableContainer<Identifier>().enlist(new IdentifierImple("hello"));
        Counter dt2 = new RecoverableContainer<Counter>().enlist(new CounterImple());
        NamedCounter dt3 = new RecoverableContainer<NamedCounter>().enlist(new NamedCounterImple());
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        dt3.setCounter(dt2);
        dt3.setName(dt1);
        
        assertTrue(dt3.getCounter() == dt2);
        assertTrue(dt3.getName() == dt1);
        
        A.abort();
        
        assertTrue(dt3.getCounter() == null);
        assertTrue(dt3.getName() == null);
    }
}
