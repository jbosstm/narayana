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

public class InheritedUnitTest extends TestCase
{
    @Transactional
    public class Sample
    {
       @ReadLock
       public void myWork ()
       {
       }
    }
    
    public class TestObject extends Sample
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
        private int _isNotState;
    }

    @SuppressWarnings(value={"unchecked"})
    public void testLockableClass ()
    {
        TestObject tester = new TestObject();
        Class c = tester.getClass().getSuperclass();
        boolean present = false;
        
        while (c != null)
        {
            if (c.getAnnotation(Transactional.class) != null)
            {
                present = true;
                
                break;
            }
            
            c = c.getSuperclass();
        }
        
        assertTrue(present);
    }
    
    public void testNotLockableClass ()
    {
        InheritedUnitTest tester = new InheritedUnitTest();
        
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
        
        methods = tester.getClass().getMethods();
        boolean found = false;
        
        for (Method m : methods)
        {
            if (m.getName().equals("myWork"))
            {
                assertTrue(m.isAnnotationPresent(ReadLock.class));
                
                found = true;
                break;
            }
        }
        
        assertTrue(found);
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
            }
            
            if (afield.getName().equals("_isNotState"))
            {
                assertFalse(afield.isAnnotationPresent(State.class));
            }
        }
    }
    
}