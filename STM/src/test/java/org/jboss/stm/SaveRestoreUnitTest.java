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
