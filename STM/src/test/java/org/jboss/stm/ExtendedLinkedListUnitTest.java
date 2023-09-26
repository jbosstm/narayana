/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm;

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

public class ExtendedLinkedListUnitTest extends TestCase
{
    @Transactional
    public interface Node
    {
        public void setPrev (Node p);
        public Node getPrev ();
        
        public void setNext (Node n);
        public Node getNext ();
        
        public String nodeName ();
    }
    
    public class NodeImple implements Node
    {   
        public NodeImple (String name)
        {
            _nodeName = name;
        }
        
        @Override
        public Node getNext ()
        {
            return _next;
        }

        @Override
        public Node getPrev ()
        {
           return _prev;
        }

        @Override
        public void setNext (Node n)
        {
            _next = n;
        }

        @Override
        public void setPrev (Node p)
        {
            _prev = p;
        }
        
        public String nodeName ()
        {
            return _nodeName;
        }
        
        @State
        private Node _prev;
        
        @State
        private Node _next;
        
        @State
        private String _nodeName = "";
    }
    
    public void testLinkedList () throws Exception
    {
        NodeImple ni1 = new NodeImple("one");
        NodeImple ni2 = new NodeImple("two");
        NodeImple ni3 = new NodeImple("three");
        AtomicAction A = new AtomicAction();
        
        Node h1 = theContainer.enlist(ni1);
        Node h2 = theContainer.enlist(ni2);
        Node h3 = theContainer.enlist(ni3);
        
        h1.setNext(h2);
        h2.setPrev(h1);
        
        assertEquals(h1.getPrev(), null);
        assertEquals(h2.getPrev().nodeName(), h1.nodeName());
        
        A.begin();
        
        h1.setNext(h3);
        h2.setPrev(null);
        h3.setPrev(h1);
        
        A.abort();
        
        assertEquals(h1.getNext().nodeName(), h2.nodeName());
        assertEquals(h1.getPrev(), null);
        assertEquals(h2.getPrev().nodeName(), h1.nodeName());
    }
    
    public RecoverableContainer<Node> theContainer = new RecoverableContainer<Node>();
}