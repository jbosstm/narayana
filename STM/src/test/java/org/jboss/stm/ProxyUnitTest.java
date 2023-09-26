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

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class ProxyUnitTest extends TestCase
{
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
        @SuppressWarnings(value =
        { "unused" })
        private int _isState;

        @SuppressWarnings(value =
        { "unused" })
        private int _isNotState;
    }

    @SuppressWarnings(value={"unused"})
    public void testInvoke () throws Throwable
    {
        PersistentContainer<Sample> theContainer = new PersistentContainer<Sample>();
        SampleLockable tester = new SampleLockable();
        boolean success = true;
        Sample proxy = theContainer.enlist(tester);

        assertNotNull(proxy);
        
        proxy.myWork();
        proxy.doSomeWork();
        proxy.doSomeOtherWork();
        proxy.notTransactionalWork();
    }
}