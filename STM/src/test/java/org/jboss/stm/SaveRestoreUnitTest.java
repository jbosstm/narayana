/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

import java.io.IOException;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.RestoreState;
import org.jboss.stm.annotations.SaveState;
import org.jboss.stm.annotations.WriteLock;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class SaveRestoreUnitTest extends TestCase
{
    @Transactional
    public interface Dummy
    {
        public int getInt ();
        public void setInt (int value);
        
        public boolean getBoolean ();
        public void setBoolean (boolean value);
    }
    
    public class DummyImple implements Dummy
    {
        public DummyImple ()
        {
            _isNotState = false;
            _saved = 1234;
        }

        @ReadLock
        public int getInt ()
        {
            return _saved;
        }
        
        @WriteLock
        public void setInt (int value)
        {
            _saved = value;
        }
        
        @ReadLock
        public boolean getBoolean ()
        {
            return _isNotState;
        }
        
        @WriteLock
        public void setBoolean (boolean value)
        {
            _isNotState = value;
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
        
        public int _saved;
        public boolean _isNotState;
    }
    
    public void testSaveRestore ()
    {
        DummyImple obj = new DummyImple();
        Container<SaveRestoreUnitTest.Dummy> container = new Container<SaveRestoreUnitTest.Dummy>();
        Dummy handle = container.create(obj);
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        handle.setInt(5678);
        handle.setBoolean(true);
        
        A.abort();
        
        assertEquals(handle.getInt(), 1234);
        assertEquals(handle.getBoolean(), true);
    }
}