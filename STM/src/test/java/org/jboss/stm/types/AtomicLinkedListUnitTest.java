/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.types;

import java.io.IOException;
import java.util.Hashtable;

import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.RecoverableContainer;

import com.arjuna.ats.arjuna.AtomicAction;

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