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
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.PersistentContainer;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class InvocationHandlerUnitTest extends TestCase
{
    @Transactional
    public interface Sample
    {
        public void myWork ();

        public int readValue ();

        public boolean writeValue ();

        public void notTransactionalWork ();
    }

    @Transactional
    public class SampleLockable implements Sample
    {
        public void myWork ()
        {
            _isState = 0;
            _isNotState = 0;
        }

        @ReadLock
        public int readValue ()
        {
            return _isState;
        }

        @WriteLock
        public boolean writeValue ()
        {
            _isState++;
            
            return true;
        }

        @TransactionFree
        public void notTransactionalWork ()
        {
        }

        @State
        private int _isState;

        @SuppressWarnings(value =
        { "unused" })
        private int _isNotState;
    }

    @SuppressWarnings(value={"unused"})
    public void testNullTxWriteLock () throws Throwable
    {
        PersistentContainer<Sample> theContainer = new PersistentContainer<Sample>();
        SampleLockable tester = new SampleLockable();
        boolean success = true;
        Sample proxy = theContainer.enlist(tester);

        assertNotNull(proxy);
        
        proxy.writeValue();
    }
    
    @SuppressWarnings(value={"unused"})
    public void testTxWriteLockCommit () throws Throwable
    {
        PersistentContainer<Sample> theContainer = new PersistentContainer<Sample>();
        SampleLockable tester = new SampleLockable();
        boolean success = true;
        Sample proxy = theContainer.enlist(tester);
        
        assertNotNull(proxy);
        
        AtomicAction act = new AtomicAction();
        
        act.begin();
        
        proxy.writeValue();
        
        act.commit();
        
        assertEquals(proxy.readValue(), 1);
    }
    
    @SuppressWarnings(value={"unused"})
    public void testTxWriteLockRollback () throws Throwable
    {
        PersistentContainer<Sample> theContainer = new PersistentContainer<Sample>();
        SampleLockable tester = new SampleLockable();
        boolean success = true;
        Sample proxy = theContainer.enlist(tester);
        
        assertNotNull(proxy);
        
        AtomicAction act = new AtomicAction();

        act.begin();
        
        proxy.writeValue();

        act.abort();
        
        assertEquals(proxy.readValue(), 0);
    }
}
