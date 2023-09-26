/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.internal;

import org.jboss.stm.annotations.TransactionFree;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.RecoverableContainer;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.LockManager;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class RecoverableContainerUnitTest extends TestCase
{

    public class TestObject extends LockManager
    {
        public TestObject ()
        {
            super();
        }
        
        public boolean save_state (OutputObjectState os)
        {
            return true;
        }
        
        public boolean restore_state (InputObjectState os)
        {
            return true;
        }
        
        public String type ()
        {
            return "/StateManager/LockManager/TestObject";
        }
    }
    
    @Transactional
    public interface Sample
    {
       public void myWork ();
       
       public void doSomeWork ();

       public boolean doSomeOtherWork ();
       
       public void notTransactionalWork ();
    }
    
    @Transactional
    public class SampleLockable implements Sample
    {
        public void myWork ()
        {
        }
        
        @ReadLock
        public void doSomeWork ()
        {

        }

        @WriteLock
        public boolean doSomeOtherWork ()
        {
            return true;
        }
        
        @TransactionFree
        public void notTransactionalWork ()
        {
        }

        @State
        @SuppressWarnings(value={"unused"})
        private int _isState;
        
        @SuppressWarnings(value={"unused"})
        private int _isNotState;
    }

    public void testInvalidEnlist ()
    {
        RecoverableContainer<TestObject> theContainer = new RecoverableContainer<TestObject>();
        TestObject tester = new TestObject();
        boolean success = false;
        
        try
        {
            theContainer.enlist(tester);
        }
        catch (final IllegalArgumentException ex)
        {
            success = true;
        }

        assertTrue(success);
    }
    
    @SuppressWarnings(value={"unused"})
    public void testValidEnlist ()
    {
        RecoverableContainer<Sample> theContainer = new RecoverableContainer<Sample>();
        SampleLockable tester = new SampleLockable();
        boolean success = true;
        
        try
        {
            Sample proxy = theContainer.enlist(tester);
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            
            success = false;
        }
        
        assertTrue(success);
    }
}