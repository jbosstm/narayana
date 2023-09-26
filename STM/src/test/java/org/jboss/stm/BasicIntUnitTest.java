/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

import java.io.IOException;

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
 * @author Mark Little
 */

public class BasicIntUnitTest extends TestCase
{   
    @Transactional
    public interface Atomic
    {
        public void change (int value) throws Exception;
        
        public void set (int value) throws Exception;
        
        public int get () throws Exception;
    }
    
    public class ExampleSTM implements Atomic
    {   
        @ReadLock
        public int get () throws Exception
        {
            return state;
        }

        @WriteLock
        public void set (int value) throws Exception
        {
            state = value;
        }
        
        @WriteLock
        public void change (int value) throws Exception
        {
            state += value;
        }

        private int state;
    }
    
    public void testExampleSTM () throws Exception
    {
        RecoverableContainer<Atomic> theContainer = new RecoverableContainer<Atomic>();
        ExampleSTM basic = new ExampleSTM();
        boolean success = true;
        Atomic obj = null;
        
        try
        {
            obj = theContainer.enlist(basic);
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            
            success = false;
        }
        
        assertTrue(success);
        
        AtomicAction a = new AtomicAction();
        
        a.begin();
        
        obj.set(1234);
        
        a.commit();

        assertEquals(obj.get(), 1234);
        
        a = new AtomicAction();

        a.begin();

        obj.change(1);
        
        a.abort();

        assertEquals(obj.get(), 1234);
    }
    
    public void testExampleSTMContainer () throws Exception
    {
        Container<Atomic> theContainer = new Container<Atomic>();
        ExampleSTM basic = new ExampleSTM();
        boolean success = true;
        Atomic obj = null;
        
        try
        {
            obj = theContainer.create(basic);
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            
            success = false;
        }
        
        assertTrue(success);
        
        AtomicAction a = new AtomicAction();
        
        a.begin();
        
        obj.set(1234);
        
        a.commit();

        assertEquals(obj.get(), 1234);
        
        a = new AtomicAction();

        a.begin();

        obj.change(1);
        
        a.abort();

        assertEquals(obj.get(), 1234);
    }
}