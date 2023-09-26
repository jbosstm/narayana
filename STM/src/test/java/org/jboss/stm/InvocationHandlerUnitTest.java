/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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