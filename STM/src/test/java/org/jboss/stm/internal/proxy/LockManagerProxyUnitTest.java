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

package org.jboss.stm.internal.proxy;

import org.jboss.stm.annotations.NotState;
import org.jboss.stm.annotations.State;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class LockManagerProxyUnitTest extends TestCase
{
    public class BasicLockable
    {
        public BasicLockable ()
        {
            _isState = 4;
            _isNotState = 2;
        }
        
        public String toString ()
        {
            return "SampleLockable < "+_isState+", "+_isNotState+" >";
        }
        
        @State
        public int _isState;

        @NotState
        public int _isNotState;
    }
    
    public class ExtendedLockable
    {
        public ExtendedLockable ()
        {
            _isState = new Double(1.234);
            _isNotState = new Integer(5678);
            _field = "Hello World";
        }
        
        public void set (String s)
        {
            _field = s;
        }
        
        public String get ()
        {
            return _field;
        }
        
        @State
        public Double _isState;
        
        @NotState
        public Integer _isNotState;
        
        @State
        private String _field;
    }

    public void testBasicSaveRestore ()
    {
       BasicLockable sample = new BasicLockable();
       LockManagerProxy<BasicLockable> proxy = new LockManagerProxy<BasicLockable>(sample);
       OutputObjectState os = new OutputObjectState();
       
       assertNotNull(proxy.type());
       
       assertTrue(proxy.save_state(os, ObjectType.RECOVERABLE));
       
       InputObjectState ios = new InputObjectState(os);
       
       sample._isState = -1;
       sample._isNotState = -1;
       
       assertTrue(proxy.restore_state(ios, ObjectType.RECOVERABLE));
       
       assertTrue(sample._isState == 4);
       assertTrue(sample._isNotState == -1);
    }
    
    public void testExtendedSaveRestore ()
    {
       ExtendedLockable sample = new ExtendedLockable();
       LockManagerProxy<ExtendedLockable> proxy = new LockManagerProxy<ExtendedLockable>(sample);
       OutputObjectState os = new OutputObjectState();
       
       assertNotNull(proxy.type());
       
       assertTrue(proxy.save_state(os, ObjectType.RECOVERABLE));
       
       InputObjectState ios = new InputObjectState(os);
       
       sample._isState = new Double(0.0);
       sample._isNotState = new Integer(0);
       sample.set("");
       
       assertTrue(proxy.restore_state(ios, ObjectType.RECOVERABLE));
       
       assertTrue(sample._isState.doubleValue() == 1.234);
       assertTrue(sample._isNotState.intValue() == 0);
       assertEquals(sample.get(), "Hello World");
    }
    
    public void testTransactionalUpdate ()
    {
       ExtendedLockable sample = new ExtendedLockable();
       LockManagerProxy<ExtendedLockable> proxy = new LockManagerProxy<ExtendedLockable>(sample);
       
       assertNotNull(proxy.type());
       
       AtomicAction A = new AtomicAction();
       
       A.begin();
       
       sample._isState = new Double(1.0);
       
       assertEquals(proxy.setlock(new Lock(LockMode.WRITE)), LockResult.GRANTED);
       
       sample._isState = new Double(4.0);
       
       A.abort();
       
       assertEquals(sample._isState, new Double(1.0));
    }
}
