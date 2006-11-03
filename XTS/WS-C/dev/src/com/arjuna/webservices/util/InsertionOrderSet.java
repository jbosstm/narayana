/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Set implementation which honours insertion order. &nbsp;The iterator returns
 * the elements in the order they were inserted into the set.
 * 
 * @author kevin
 */
public class InsertionOrderSet extends AbstractSet implements Cloneable, Serializable
{
    /**
     * Serial version UID for this class.
     */
    private static final long serialVersionUID = -5575694021209967201L ;
    
    /**
     * The map of entries to linked list entries in this set.
     */
    private transient HashMap entries = new HashMap() ;
    /**
     * The head of the linked list.
     */
    private transient LinkedListEntry head ;
    
    /**
     * Construct the insertion order set.
     */
    public InsertionOrderSet()
    {
        head = new LinkedListEntry(null) ;
        head.setNext(head) ;
        head.setPrevious(head) ;
    }
    
    /**
     * Construct the insertion order set with the specified collection.
     * @param collection The collection to add to this set.
     */
    public InsertionOrderSet(final Collection collection)
    {
        this() ;
        addAll(collection) ;
    }
    
    /**
     * Get the size of this set.
     * @return the set size.
     */
    public int size()
    {
        return entries.size() ;
    }

    /**
     * Does this set contain the specified object?
     * @param obj The object to test.
     * @return true if the object is in the set, false otherwise.
     */
    public boolean contains(final Object obj)
    {
        return (entries.get(obj) != null) ;
    }

    /**
     * Get the insertion order iterator for this set.
     * @return the iterator.
     */
    public Iterator iterator()
    {
        return new LinkedListEntryIterator() ;
    }

    /**
     * Add an object into the set if it is not already present.
     * 
     * @param obj The object to add into the set.
     * @return true if the object has been added to the set, false otherwise.
     */
    public boolean add(final Object obj)
    {
        final LinkedListEntry listEntry = new LinkedListEntry(obj) ;
        final Object previous = entries.put(obj, listEntry) ;
        if (previous != null)
        {
            entries.put(obj, previous) ;
            return false ;
        }
        
        final LinkedListEntry lastEntry = head.getPrevious() ;
        
        listEntry.setPrevious(lastEntry) ;
        listEntry.setNext(head) ;
        
        lastEntry.setNext(listEntry) ;
        head.setPrevious(listEntry) ;
        
        return true ;
    }

    /**
     * Remove an entry from the set.
     * @param obj The object to remove from the set.
     * @return true if the object has been remove from the set, false otherwise.
     */
    public boolean remove(final Object obj)
    {
        final LinkedListEntry entry = (LinkedListEntry)entries.remove(obj) ;
        if (entry == null)
        {
            return false ;
        }
        
        final LinkedListEntry previousEntry = entry.getPrevious() ;
        final LinkedListEntry nextEntry = entry.getNext() ;
        previousEntry.setNext(nextEntry) ;
        nextEntry.setPrevious(previousEntry) ;
        
        entry.setPrevious(null) ;
        entry.setNext(null) ;
        
        return true ;
    }

    /**
     * Clear the set
     */
    public void clear()
    {
        head.setNext(head) ;
        head.setPrevious(head) ;
        entries.clear() ;
    }
    
    /**
     * Is the specified object equal to this one?
     * @param rhs The object to compare.
     * @return true if the specified object is an insertion order set
     *   with the same entries and order as this one, false otherwise.
     */
    public boolean equals(final Object rhs)
    {
        if (rhs == this)
        {
            return true ;
        }
        
        if (!(rhs instanceof InsertionOrderSet))
        {
            return false ;
        }
        
        final InsertionOrderSet rhsSet = (InsertionOrderSet)rhs ;
        if (size() != rhsSet.size())
        {
            return false ;
        }
        
        final Iterator thisIter = iterator() ;
        final Iterator rhsIter = rhsSet.iterator() ;
        
        while(thisIter.hasNext())
        {
            if (!rhsIter.hasNext() || !equals(thisIter.next(), rhsIter.next()))
            {
                return false ;
            }
        }
        
        return true ; 
    }
    
    /**
     * Clone this object.
     * @return the clone of this object.
     */
    protected Object clone()
        throws CloneNotSupportedException
    {
        return new InsertionOrderSet(this) ;
    }


    /**
     * Compare the two objects for equality, including nulls.
     * @param lhs The first object to compare.
     * @param rhs The second object to compare.
     * @return true if the objects are equals, false otherwise.
     */
    private static boolean equals(final Object lhs, final Object rhs)
    {
        if (lhs == null)
        {
            return (rhs == null) ;
        }
        return lhs.equals(rhs) ;
    }
    
    /**
     * Write this set to the object output stream.
     * @param objectOutputStream The object output stream
     * @throws IOException For IO errors.
     */
    private void writeObject(final ObjectOutputStream objectOutputStream)
        throws IOException
    {
        // Write default entries (should do nothing but included for completeness)
        objectOutputStream.defaultWriteObject();
        
        objectOutputStream.writeInt(size()) ;
        final Iterator iterator = iterator() ;
        while(iterator.hasNext())
        {
            objectOutputStream.writeObject(iterator.next()) ;
        }
    }

    /**
     * Read this set from the object input stream.
     * @param objectInputStream The object input stream.
     * @throws IOException for IO errors.
     * @throws ClassNotFoundException if a dependent class cannot be found.
     */
    private void readObject(final ObjectInputStream objectInputStream)
         throws IOException, ClassNotFoundException
    {
        // Read default entries (should do nothing but included for completeness)
        objectInputStream.defaultReadObject() ;
        
        final int size = objectInputStream.readInt() ;
        
        head = new LinkedListEntry(null) ;
        head.setNext(head) ;
        head.setPrevious(head) ;
        entries = new HashMap(size) ;
        
        for(int count = 0 ; count < size ; count++)
        {
            add(objectInputStream.readObject()) ;
        }
    }
    
    /**
     * Private inner class providing double linked list functionality 
     * @author kevin
     */
    private static class LinkedListEntry
    {
        /**
         * The object associated with this entry in the list.
         */
        private final Object obj ;
        /**
         * The next list entry.
         */
        private LinkedListEntry next ;
        /**
         * The previous list entry.
         */
        private LinkedListEntry previous ;
        
        /**
         * Construct the linked list entry.
         * @param obj The associated object.
         */
        LinkedListEntry(final Object obj)
        {
            this.obj = obj ;
        }
        
        /**
         * Get the next entry in the list.
         * @return The next entry.
         */
        LinkedListEntry getNext()
        {
            return next ;
        }
        
        /**
         * Set the next entry in the list.
         * @param next The next entry.
         */
        void setNext(final LinkedListEntry next)
        {
            this.next = next ;
        }
        
        /**
         * Get the previous entry in the list.
         * @return The previous entry.
         */
        LinkedListEntry getPrevious()
        {
            return previous ;
        }
        
        /**
         * Set the previous entry in the list.
         * @param previous The previous entry.
         */
        void setPrevious(final LinkedListEntry previous)
        {
            this.previous = previous ;
        }
        
        /**
         * Get the object associated with this entry.
         * @return The object.
         */
        Object getObject()
        {
            return obj ;
        }
    }
    
    /**
     * The iterator class for the Insertion Order Set
     * @author kevin
     */
    private class LinkedListEntryIterator implements Iterator
    {
        /**
         * The current entry.
         */
        private LinkedListEntry current ;
        /**
         * The next entry.
         */
        private LinkedListEntry next = head.getNext() ;
        
        /**
         * Does the iterator have more entries?
         * @return true if the iterator contains more entries, false otherwise.
         */
        public boolean hasNext()
        {
            return (next != head) ;
        }
        
        /**
         * Get the next entry from the iterator.
         * @return the next entry in the iterator.
         * @throws NoSuchElementException if there are no more entries in the iterator.
         */
        public Object next()
            throws NoSuchElementException
        {
            if (!hasNext())
            {
                throw new NoSuchElementException("End of iterator") ;
            }
            current = next ;
            next = current.getNext() ;
            return current.getObject() ;
        }
        
        /**
         * Remove the current entry from the set.
         * @throws IllegalStateException if the next method has not been called or if
         * remove has already been called on the current 
         */
        public void remove()
            throws IllegalStateException
        {
            if (current == null)
            {
                throw new IllegalStateException("Nothing to remove") ;
            }
            InsertionOrderSet.this.remove(current.getObject()) ;
            current = null ;
        }
    }
}
