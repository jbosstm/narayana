/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: SimpleList.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.template;

/**
 * A SimpleList maintains entries in a linked list.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: SimpleList.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class SimpleList
{

    /**
     * Create a new, empty list.
     */

public SimpleList ()
    {
	head_ = null;
	entryCount = 0;
    }

public void finalize ()
    {
	empty();
    }

    /**
     * Empty the list.
     */

public synchronized void empty ()
    {
	ListElement te = null;

	while ((te = orderedPop()) != null)
	{
	}
    }

    /**
     * Return the head of the list.
     */

public synchronized ListElement head ()
    {
	if (head_ != null)
	    return head_.theData;
	else
	    return null;
    }

    /**
     * Returns the number of entries in the list.
     */

public synchronized int size ()
    {
	return entryCount;
    }

    /**
     * Is the specified entry in the list. Uses Object.equals
     * for comparison.
     */

public synchronized boolean lookFor (ListElement th)
    {
	if (th != null)
	{
	    SimpleListEntry te = head_;
        
	    while (te != null)
	    {
		/* exists already ? */
            
		if ((te.theData == th) ||
		    (te.equals(th)))
		{     
		    return true;
		}
                
		te = te.next;
	    }
	}
    
	return false;
    }

    /**
     * Push the specified item onto the list.
     */

public synchronized boolean push (ListElement th)
    {
	if (th != null)
	{
	    SimpleListEntry te = new SimpleListEntry(head_, th);
	
	    head_ = te;
	    entryCount++;
	    
	    return true;
	}
    
	return false;
    }

    /**
     * Add the specified item to the list. Same as push.
     * Will add the entry even if it is already in the list.
     */

public synchronized boolean add (ListElement th)
    {
	return push(th);
    }

    /**
     * Insert is like add except that it only adds once, i.e., if the entry
     * already exists it returns false. So, two different instances of
     * ListElement with the same value will not be allowed.
     */

public synchronized boolean insert (ListElement th)
    {
	if (th != null)
	{
	    SimpleListEntry te = head_;
	
	    while (te != null)
	    {
		/* exists already ? */

		if (th == te)
		{
		    return false;
		}
		
		te = te.next;
	    }
	
	    /* insert if not found */
	
	    te = new SimpleListEntry(head_, th);
	
	    head_ = te;
	    entryCount++;
	    
	    return true;
	}
    
	return false;
    }

    /**
     * Remove the specified entry. Returns an indication of
     * whether the item was in the list.
     */

public synchronized boolean remove (ListElement findMe)
    {
	SimpleListEntry te = head_;
	SimpleListEntry old = null;
	
	while (te != null)
	{
	    if (te.theData == findMe)
	    {
		if (old == null)
		    head_ = te.next;
		else
		    old.next = te.next;
	    
		te = null;
		entryCount--;
	    
		return true;
	    }
	
	    old = te;
	    te = te.next;
	}
    
	return false;
    }

    /**
     * Pop (remove) the first entry off the list.
     */

public synchronized ListElement orderedPop ()
    {
	ListElement th = null;
	
	if (head_ != null)
	{
	    SimpleListEntry te = head_;
	    
	    head_ = te.next;
	    th = te.theData;
	    entryCount--;

	    te = null;
	}
    
	return th;
    }

protected SimpleListEntry head_;
protected int             entryCount;

};
