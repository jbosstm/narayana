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

public class InheritenceLockManagerProxyUnitTest extends TestCase
{
    public interface Blank
    {       
    }
    
    @Transactional
    public class Base
    {
        public Base ()
        {
            valid = true;
        }
        
        @SaveState
        public void saveMyState (OutputObjectState os) throws IOException
        {
            os.packBoolean(valid);
        }
        
        @RestoreState
        public void restoreMyState (InputObjectState os) throws IOException
        {
            valid = os.unpackBoolean();
        }
        
        public boolean valid;
    }
    
    @Transactional
    public class Inherit extends Base implements Blank
    {
        public Inherit ()
        {
            myString = "Hello World";
        }
        
        public String myString;
    }
    
    @Transactional
    public class BasicLockable extends Base
    {
        public BasicLockable ()
        {
            _isState = 4;
            _isNotState = 2;
            _saved = 1234;
        }
        
        public String toString ()
        {
            return "BasicLockable < "+_isState+", "+_isNotState+", "+_saved+" >";
        }
        
        @SaveState
        public void save_state (OutputObjectState os) throws IOException
        {
            super.saveMyState(os);
            
            os.packInt(_saved);
        }
        
        @RestoreState
        public void restore_state (InputObjectState os) throws IOException
        {
            super.restoreMyState(os);
            
            _saved = os.unpackInt();
        }
        
        @State // will be ignored!!
        public int _isState;

        public int _isNotState;
        
        public int _saved;
    }
    
    public void testInheritSaveRestore ()
    {
        Inherit obj = new Inherit();
        LockManagerProxy<Inherit> proxy = new LockManagerProxy<Inherit>(obj);
        OutputObjectState os = new OutputObjectState();

        assertTrue(proxy.save_state(os, ObjectType.RECOVERABLE));
        
        obj.myString = "";
        
        InputObjectState ios = new InputObjectState(os);
        
        assertTrue(proxy.restore_state(ios, ObjectType.RECOVERABLE));
        
        assertEquals(obj.myString, "");
    }
    
    public void testSaveRestore ()
    {
        BasicLockable obj = new BasicLockable();
        LockManagerProxy<BasicLockable> proxy = new LockManagerProxy<BasicLockable>(obj);
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(proxy.save_state(os, ObjectType.RECOVERABLE));     
        
        obj._saved = 4567;
        obj._isState = 0;  // make sure it's ignored by save/restore.
        ((Base) obj).valid = false;
        
        InputObjectState ios = new InputObjectState(os);
        
        assertTrue(proxy.restore_state(ios, ObjectType.RECOVERABLE));
        
        assertEquals(obj._saved, 1234);
        assertEquals(obj._isState, 0);
        
        assertTrue(((Base) obj).valid);
    }
}