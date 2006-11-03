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
 * $Id: HashList.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.template;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * This hash table for objects which define get_uid does not use
 * the Java Hashtable class, since we sometimes want to add multiple
 * different values under the same key.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: HashList.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class HashList
{

    /**
     * Create a new HashList with the specified size.
     */

public HashList (int hashBucket)
    {
	maxBucket = hashBucket;
	buckets = new BasicList[hashBucket];
    
	for (int i = 0; i < hashBucket; i++)
	    buckets[i] = null;
    }

public void finalize ()
    {
	empty();

	buckets = null;
    }

    /**
     * Empty the list.
     */

public synchronized void empty ()
    {
	for (int i = 0; i < maxBucket; i++)
	    if (buckets[i] != null)
		buckets[i] = null;
    }

    /**
     * Return the total number of entries in the list.
     */

public synchronized int size ()
    {
	int rVal = 0;
    
	for (int i = 0; i < maxBucket; i++)
	{
	    if (buckets[i] != null)
	    {
		rVal += buckets[i].size();
	    }
	}
    
	return rVal;
    }

    /**
     * Add the entry to the list. If the item is already present
     * then it will be added again.
     */

public synchronized boolean add (ListElement th)
    {
	if (th != null)
	{
	    int entry = th.get_uid().hashCode() % maxBucket;
	    
	    if (buckets[entry] == null)
		buckets[entry] = new BasicList();

	    return buckets[entry].add(th);
	}
    
	return false;
    }

    /**
     * Insert is like add execpt that it only adds once, i.e., if the entry
     * already exists it returns false.
     */

public synchronized boolean insert (ListElement th)
    {
	if (th != null)
	{
	    int entry = th.get_uid().hashCode() % maxBucket;

	    if (buckets[entry] != null)
		return buckets[entry].insert(th);
	    else
	    {
		buckets[entry] = new BasicList();
		return buckets[entry].add(th);
	    }
	}
    
	return false;
    }

    /**
     * Remove the specified item. If it is not present then
     * returns false, otherwise true.
     */

public synchronized boolean remove (ListElement findMe)
    {
	if (findMe != null)
	{
	    int entry = findMe.get_uid().hashCode() % maxBucket;

	    if (buckets[entry] != null)
		return buckets[entry].remove(findMe);
	}
	
	return false;
    }

    /**
     * Remove (and return) the item with the specified Uid.
     */

public synchronized ListElement remove (Uid findMe)
    {
	int entry = findMe.hashCode() % maxBucket;
    
	if (buckets[entry] != null)
	    return buckets[entry].remove(findMe);

	return null;
    }

    /**
     * Look for (and return) the item with the specified Uid.
     */

public synchronized ListElement lookFor (Uid findMe)
    {
	int entry = findMe.hashCode() % maxBucket;
	
	if (buckets[entry] != null)
	    return buckets[entry].lookFor(findMe);

	return null;
    }

    /**
     * Remove (and return) the first element from the list.
     */

public synchronized ListElement orderedPop ()
    {
	ListElement th = null;
    
	for (int i = 0; (i < maxBucket) && (th != null); i++)
	{
	    if ((buckets[i] != null) && (buckets[i].size() > 0))
	    {
		th = buckets[i].orderedPop();
	    }
	}
    
	return th;
    }

protected BasicList[] buckets;
protected int         maxBucket;

}
