/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

import org.jboss.stm.annotations.NotState;
import org.jboss.stm.annotations.TransactionFree;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;

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

public class BasicContainerUnitTest extends TestCase
{

    public class TestObject extends LockManager
    {
        public TestObject ()
        {
            super(ObjectType.ANDPERSISTENT);
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
        private int _isState;
        
        @NotState
        private int _isNotState;
    }

    public void testInvalidEnlist ()
    {
        TestContainer<TestObject> theContainer = new TestContainer<TestObject>();
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
    
    public void testInvalidModel ()
    {
        boolean success = true;
        
        try
        {
            Container<TestObject> theContainer = new Container<TestObject>("Foobar", Container.TYPE.RECOVERABLE, Container.MODEL.SHARED);
        
            success = false;
        }
        catch (final RuntimeException ex)
        {
            success = true;
        }
        catch (final Throwable ex)
        {
            success = false;
        }
        
        assertTrue(success);
    }
    
    @SuppressWarnings(value={"unused"})
    public void testValidEnlist ()
    {
        TestContainer<Sample> theContainer = new TestContainer<Sample>();
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