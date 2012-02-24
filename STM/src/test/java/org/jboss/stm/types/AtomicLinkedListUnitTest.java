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

package org.jboss.stm.types;

import java.io.IOException;
import java.util.Hashtable;

import org.jboss.stm.annotations.RestoreState;
import org.jboss.stm.annotations.SaveState;
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
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class AtomicLinkedListUnitTest extends TestCase
{
    @Transactional
    public interface ExtendedAtomicLinkedList extends AtomicLinkedList
    {
        @ReadLock
        public String nodeName ();
    }
    
    @Transactional
    public class LinkedListEntry implements ExtendedAtomicLinkedList
    {
        public LinkedListEntry (final String name)
        {
            _nodeName = name;
        }
        
        public final String nodeName ()
        {
            return _nodeName;
        }
        
        @Override
        public AtomicLinkedList getNext ()
        {
            return _next;
        }

        @Override
        public AtomicLinkedList getPrev ()
        {
            return _prev;
        }

        @Override
        public void setNext (AtomicLinkedList n)
        {
            _next = n;
        }

        @Override
        public void setPrev (AtomicLinkedList n)
        {
            _prev = n;
        }
        
        @State
        private AtomicLinkedList _prev;
        
        @State
        private AtomicLinkedList _next;
        
        @State
        private String _nodeName = "";
    }
    
    public void testLinkedList () throws Exception
    {
        ExtendedAtomicLinkedList ni1 = new LinkedListEntry("one");
        ExtendedAtomicLinkedList ni2 = new LinkedListEntry("two");
        ExtendedAtomicLinkedList ni3 = new LinkedListEntry("three");
        AtomicAction A = new AtomicAction();
        
        ExtendedAtomicLinkedList h1 = theContainer.enlist(ni1);
        ExtendedAtomicLinkedList h2 = theContainer.enlist(ni2);
        ExtendedAtomicLinkedList h3 = theContainer.enlist(ni3);
        
        h1.setNext(h2);
        h2.setPrev(h1);
        
        assertEquals(h1.getPrev(), null);
        assertEquals(((ExtendedAtomicLinkedList)h2.getPrev()).nodeName(), h1.nodeName());
        
        A.begin();
        
        h1.setNext(h3);
        h2.setPrev(null);
        h3.setPrev(h1);
        
        A.abort();
        
        assertEquals(((ExtendedAtomicLinkedList)h1.getNext()).nodeName(), h2.nodeName());
        assertEquals(h1.getPrev(), null);
        assertEquals(((ExtendedAtomicLinkedList)h2.getPrev()).nodeName(), h1.nodeName());
    }
    
    public RecoverableContainer<ExtendedAtomicLinkedList> theContainer = new RecoverableContainer<ExtendedAtomicLinkedList>();
}
