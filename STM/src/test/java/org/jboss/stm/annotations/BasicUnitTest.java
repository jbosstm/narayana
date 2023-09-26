/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class BasicUnitTest extends TestCase
{

    @Transactional
    public class TestObject
    {
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
        private Integer _isNotState;
    }

    public void testLockableClass ()
    {
        TestObject tester = new TestObject();
        
        assertTrue(tester.getClass().isAnnotationPresent(Transactional.class));
    }
    
    public void testNotLockableClass ()
    {
        BasicUnitTest tester = new BasicUnitTest();
        
        assertFalse(tester.getClass().isAnnotationPresent(Transactional.class));
    }

    public void testMethodTypes () throws Exception
    {
        TestObject tester = new TestObject();
        Method[] methods = tester.getClass().getDeclaredMethods();

        assertNotNull(methods);

        Method someWork = tester.getClass().getDeclaredMethod("doSomeWork", (Class[]) null);
        
        assertNotNull(someWork);
        assertTrue(someWork.isAnnotationPresent(ReadLock.class));
        
        Method someOtherWork = tester.getClass().getDeclaredMethod("doSomeOtherWork", (Class[]) null);
        
        assertNotNull(someOtherWork);
        assertTrue(someOtherWork.isAnnotationPresent(WriteLock.class));
        
        Method someBasicWork = tester.getClass().getDeclaredMethod("notTransactionalWork", (Class[]) null);
        
        assertNotNull(someBasicWork);
        assertFalse(someBasicWork.isAnnotationPresent(WriteLock.class));
        assertFalse(someBasicWork.isAnnotationPresent(ReadLock.class));
    }
    
    public void testFields () throws Exception
    {
        TestObject tester = new TestObject();
        Field[] fields = tester.getClass().getDeclaredFields(); // get all fields including private
        
        assertNotNull(fields);
        
        for (Field afield : fields)
        {
            if (afield.getName().equals("_isState"))
            {
                assertTrue(afield.isAnnotationPresent(State.class));
                assertTrue(afield.getType().equals(Integer.TYPE));
            }
            
            if (afield.getName().equals("_isNotState"))
            {
                assertFalse(afield.isAnnotationPresent(State.class));
                assertTrue(afield.getType().equals(Integer.class));
            }
        }
    }
    
}