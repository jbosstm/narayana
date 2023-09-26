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

public class ArrayIntUnitTest extends TestCase
{   
    @Transactional
    public interface Atomic
    {
        public void change (int index, int value) throws Exception;
        
        public void set (int index, int value) throws Exception;
        
        public int get (int index) throws Exception;
    }
    
    public class ArrayType implements Atomic
    {   
        @ReadLock
        public int get (int index) throws Exception
        {
            return state[index];
        }

        @WriteLock
        public void set (int index, int value) throws Exception
        {
            state[index] = value;
        }
        
        @WriteLock
        public void change (int index, int value) throws Exception
        {
            state[index] += value;
        }

        private int[] state = new int[10];  // ignore error checking for now
    }
    
    public class MultiArrayType implements Atomic
    {   
        @ReadLock
        public int get (int index) throws Exception
        {
            return state[0][index];
        }

        @WriteLock
        public void set (int index, int value) throws Exception
        {
            state[0][index] = value;
        }
        
        @WriteLock
        public void change (int index, int value) throws Exception
        {
            state[0][index] += value;
        }

        private int[][] state = new int[10][10];  // ignore error checking for now
    }
    
    public void testArrayType () throws Exception
    {
        RecoverableContainer<Atomic> theContainer = new RecoverableContainer<Atomic>();
        ArrayType basic = new ArrayType();
        boolean success = true;
        Atomic obj = null;
        int index = 5; // arbitrary value
        
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
        
        obj.set(index, 1234);
        
        a.commit();

        assertEquals(obj.get(index), 1234);
        
        a = new AtomicAction();

        a.begin();

        obj.change(index, 1);
        
        a.abort();

        assertEquals(obj.get(index), 1234);
    }
    
    public void testMultiArrayType () throws Exception
    {
        RecoverableContainer<Atomic> theContainer = new RecoverableContainer<Atomic>();
        ArrayType basic = new ArrayType();
        boolean success = true;
        Atomic obj = null;
        int index = 5; // arbitrary value
        
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
        
        obj.set(index, 1234);
        
        a.commit();

        assertEquals(obj.get(index), 1234);
        
        a = new AtomicAction();

        a.begin();

        obj.change(index, 1);
        
        a.abort();

        assertEquals(obj.get(index), 1234);
    }
}