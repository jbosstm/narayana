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
import com.arjuna.ats.internal.arjuna.common.UidHelper;
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

public class LinkedListUnitTest extends TestCase
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
    
    @Transactional
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
       
        @SaveState
        public void save_state (OutputObjectState os) throws IOException
        {
            if (_prev == null)
                os.packBoolean(false);
            else
            {
                os.packBoolean(true);
                UidHelper.packInto(theContainer.getUidForHandle(_prev), os);
            }
            
            if (_next == null)
                os.packBoolean(false);
            else
            {
                os.packBoolean(true); 
                UidHelper.packInto(theContainer.getUidForHandle(_next), os);
            }

            os.packString(_nodeName);
        }
        
        @RestoreState
        public void restore_state (InputObjectState os) throws IOException
        {
            boolean ptr = os.unpackBoolean();
            
            if (ptr == false)
                _prev = null;
            else
            {
            	Uid id = UidHelper.unpackFrom(os);
                _prev = theContainer.getHandle(id);
            }

            ptr = os.unpackBoolean();
            
            if (ptr == false)
                _next = null;
            else
            {
            	Uid id = UidHelper.unpackFrom(os);
                _next = theContainer.getHandle(id);
            }

            _nodeName = os.unpackString();
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
