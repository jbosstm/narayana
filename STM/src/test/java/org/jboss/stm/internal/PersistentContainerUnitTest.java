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

package org.jboss.stm.internal;

import org.jboss.stm.annotations.TransactionFree;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.PersistentContainer;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.LockManager;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class PersistentContainerUnitTest extends TestCase
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
        PersistentContainer<TestObject> theContainer = new PersistentContainer<TestObject>();
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
        PersistentContainer<Sample> theContainer = new PersistentContainer<Sample>();
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
    
    public void testGetUid ()
    {
        PersistentContainer<Sample> theContainer = new PersistentContainer<Sample>();
        SampleLockable tester = new SampleLockable();
        boolean success = true;
        
        try
        {
            Sample proxy = theContainer.enlist(tester);
            Uid u = theContainer.getUidForOriginal(proxy);
            
            assertNotNull(u);
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            
            success = false;
        }
        
        assertTrue(success);
    }
}
