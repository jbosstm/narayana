/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

import java.io.IOException;
import java.util.Hashtable;

import org.jboss.stm.annotations.Nested;
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

public class NestedUnitTest extends TestCase
{
    @Transactional
    public interface Counter
    {
        public boolean increment (boolean inTransaction);
        public int count ();
        public boolean checkTransactionId (AtomicAction tx);
    }
    
    @Nested
    public class CounterImple implements Counter
    {
        @ReadLock
        public int count ()
        {
            return _count;
        }

        @WriteLock
        public boolean increment (boolean inTransaction)
        {
            if (inTransaction && (AtomicAction.Current() == null))
                return false;
            
            _count++;
            
            return true;
        }
        
        @ReadLock
        public boolean checkTransactionId (AtomicAction tx)
        {
            return (tx.equals(AtomicAction.Current()));
        }
        
        private int _count = 0;
    }
    
    public void testNested () throws Exception
    {
        Counter dt2 = new RecoverableContainer<Counter>().enlist(new CounterImple());
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        dt2.increment(true);
        
        assertFalse(dt2.checkTransactionId(A));

        A.abort();
        
        assertTrue(dt2.count() == 0);
    }
    
    public void testTopLevel () throws Exception
    {
        Counter dt2 = new RecoverableContainer<Counter>().enlist(new CounterImple());
        
        assertTrue(dt2.increment(true));
        
        assertTrue(dt2.count() == 1);
    }
}