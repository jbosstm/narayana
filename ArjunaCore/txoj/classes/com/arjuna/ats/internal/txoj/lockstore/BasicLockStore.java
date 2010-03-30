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
 * $Id: BasicLockStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.txoj.lockstore;

import com.arjuna.ats.txoj.lockstore.LockStore;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.txoj.logging.txojLogger;

import com.arjuna.ats.txoj.exceptions.LockStoreException;

/**
 * A very basic lock store implementation. It saves the locks in process, i.e.,
 * in the memory of the JVM.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: BasicLockStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class BasicLockStore extends LockStore
{

    /*
     * This implementation is for purely local-applet locks, so we don't need
     * the key.
     */

    public BasicLockStore(String key)
    {
        if (txojLogger.aitLogger.isDebugEnabled()) {
            txojLogger.aitLogger.debug("BasicLockStore.BasicLockStore(" + key + ")");
        }
    }

    public InputObjectState read_state (Uid u, String tName)
            throws LockStoreException
    {
        if (txojLogger.aitLogger.isDebugEnabled()) {
            txojLogger.aitLogger.debug("BasicLockStore.read_state(" + u + ", " + tName + ")");
        }

        return segmentStore.read_state(u, tName);
    }

    public boolean remove_state (Uid u, String tName)
    {
        if (txojLogger.aitLogger.isDebugEnabled()) {
            txojLogger.aitLogger.debug("BasicLockStore.remove_state(" + u + ", " + tName + ")");
        }

        return segmentStore.remove_state(u, tName);
    }

    public boolean write_committed (Uid u, String tName, OutputObjectState state)
    {
        if (txojLogger.aitLogger.isDebugEnabled()) {
            txojLogger.aitLogger.debug("BasicLockStore.write_committed(" + u + ", " + tName + ", "
                    + state + ")");
        }

        return segmentStore.write_committed(u, tName, state);
    }

    private static LockStoreSegment segmentStore = new LockStoreSegment();

};

class LockStoreSegment
{

    public LockStoreSegment()
    {
        headOfList = null;
    }

    public synchronized boolean write_committed (Uid u, String tName,
            OutputObjectState state)
    {
        if (state.size() <= 0)
            return false;

        LockStoreList ptr = find(tName);

        if (ptr == null)
        {
            ptr = new LockStoreList(tName);
            ptr.setNext(headOfList);

            headOfList = ptr;
        }

        ptr.add(u, state);

        return true;
    }

    public synchronized InputObjectState read_state (Uid u, String tName)
            throws LockStoreException
    {
        LockStoreList ptr = find(tName);

        if (ptr == null)
            return null;

        return ptr.get(u);
    }

    public synchronized boolean remove_state (Uid u, String tName)
    {
        boolean found = false;
        LockStoreList ptr = headOfList;

        while ((!found) && (ptr != null))
        {
            if (ptr.name().compareTo(tName) == 0)
                found = true;
            else
                ptr = ptr.getNext();
        }

        if (!found)
            return true;
        else
            ptr.remove(u);

        return true;
    }

    public synchronized boolean remove_segment (String tName)
    {
        boolean found = false;
        LockStoreList ptr = headOfList;
        LockStoreList trail = null;

        while ((!found) && (ptr != null))
        {
            if (ptr.name().compareTo(tName) == 0)
                found = true;
            else
            {
                trail = ptr;
                ptr = ptr.getNext();
            }
        }

        if (!found)
            return true;
        else
        {
            if (trail == null) // remove headOfList
                headOfList = ptr.getNext();
            else
                trail.setNext(ptr.getNext());

            ptr = null;
        }

        return true;
    }

    private LockStoreList find (String tName)
    {
        boolean found = false;
        LockStoreList ptr = headOfList;

        while ((!found) && (ptr != null))
        {
            if (ptr.name().compareTo(tName) == 0)
                found = true;
            else
                ptr = ptr.getNext();
        }

        return ptr;
    }

    private LockStoreList headOfList;

};

class LockStoreList
{

    public LockStoreList(String tName)
    {
        _name = tName;
        _next = null;
    }

    public String name ()
    {
        return _name;
    }

    public void setNext (LockStoreList n)
    {
        _next = n;
    }

    public LockStoreList getNext ()
    {
        return _next;
    }

    public boolean add (Uid u, OutputObjectState state)
    {
        LockStateStore ptr = find(u);

        if (ptr == null)
        {
            ptr = new LockStateStore(u, state);
            ptr._next = headOfList;
            headOfList = ptr;
        }
        else
            ptr._state = state;

        return true;
    }

    public InputObjectState get (Uid u) throws LockStoreException
    {
        LockStateStore ptr = find(u);

        if (ptr == null)
            return null;
        else
            return new InputObjectState(ptr._state);
    }

    public boolean remove (Uid u)
    {
        boolean found = false;
        LockStateStore ptr = headOfList;
        LockStateStore trail = null;

        while ((!found) && (ptr != null))
        {
            if (ptr._id.equals(u))
                found = true;
            else
            {
                trail = ptr;
                ptr = ptr._next;
            }
        }

        if (!found)
            return false;
        else
        {
            if (trail == null) // remove headOfList
                headOfList = ptr._next;
            else
                trail._next = ptr._next;
        }

        return true;
    }

    private LockStateStore find (Uid u)
    {
        boolean found = false;
        LockStateStore ptr = headOfList;

        while ((!found) && (ptr != null))
        {
            if (ptr._id.equals(u))
                found = true;
            else
                ptr = ptr._next;
        }

        return ptr;
    }

    private LockStoreList _next;

    private String _name;

    private LockStateStore headOfList;

}

class LockStateStore
{

    public LockStateStore(Uid u, OutputObjectState s)
    {
        _id = u;
        _state = s;
        _next = null;
    }

    public Uid _id;

    public OutputObjectState _state;

    public LockStateStore _next;

};
