/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.internal.proxy;

import java.io.IOException;

import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.RestoreState;
import org.jboss.stm.annotations.SaveState;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class ComplexLockManagerProxyUnitTest extends TestCase
{
    @Transactional
    public class BasicLockable
    {
        public BasicLockable ()
        {
            _isState = 4;
            _isNotState = 2;
            _saved = 1234;
        }
        
        public String toString ()
        {
            return "SampleLockable < "+_isState+", "+_isNotState+", "+_saved+" >";
        }
        
        @SaveState
        public void save_state (OutputObjectState os) throws IOException
        {
           os.packInt(_saved);
        }
        
        @RestoreState
        public void restore_state (InputObjectState os) throws IOException
        {
            _saved = os.unpackInt();
        }
        
        @State // will be ignored!!
        public int _isState;

        public int _isNotState;
        
        public int _saved;
    }
    
    // this one will fail because it defined a save but no restore
    
    @Transactional
    class InvalidLockable
    {
        @SaveState
        public void save_state (OutputObjectState os) throws IOException
        {
        }
    }
    
    public void testInvalidSaveRestore ()
    {
       InvalidLockable obj = new InvalidLockable();
       LockManagerProxy<InvalidLockable> proxy = new LockManagerProxy<InvalidLockable>(obj);
       OutputObjectState os = new OutputObjectState();
       
       assertFalse(proxy.save_state(os, ObjectType.RECOVERABLE));
    }
    
    public void testSaveRestore ()
    {
        BasicLockable obj = new BasicLockable();
        LockManagerProxy<BasicLockable> proxy = new LockManagerProxy<BasicLockable>(obj);
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(proxy.save_state(os, ObjectType.RECOVERABLE));     
        
        obj._saved = 4567;
        obj._isState = 0;  // make sure it's ignored by save/restore.
        
        InputObjectState ios = new InputObjectState(os);
        
        assertTrue(proxy.restore_state(ios, ObjectType.RECOVERABLE));
        
        assertEquals(obj._saved, 1234);
        assertEquals(obj._isState, 0);
    }
}