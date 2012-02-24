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
    
    @Transactional
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
}
