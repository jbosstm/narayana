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
 * $Id: LockList.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.txoj;

import com.arjuna.ats.txoj.Lock;


public class LockList
{

    public LockList()
    {
        count = 0;
        head = null;
    }

    public void finalize ()
    {
        @SuppressWarnings("unused")
        Lock temp;

        while ((temp = pop()) != null)
            temp = null; // temp.finalize() ?
    }

    /*
     * Insert a new Lock. This returns TRUE if the insertion occurred, false
     * otherwise. Insertion fails if a matching lock already exists in the list.
     */

    public final boolean insert (Lock newlock)
    {
        LockListIterator next = new LockListIterator(this);
        Lock current = null;

        while ((current = next.iterate()) != null)
        {
            if (current.equals(newlock))
            {
                return false;
            }
        }

        push(newlock);

        return true;
    }

    /*
     * Pop the first element off the list and return it.
     */

    public final Lock pop ()
    {
        Lock current;

        if (count == 0)
            return null;

        current = (Lock) head;
        count--;
        head = LockFriend.getLink(head);
        LockFriend.setLink(current, null);

        return current;
    }

    /*
     * Push a new element at the head of the list. First set the link field to
     * be the old head, and then set head to be the new element.
     */

    public final void push (Lock newLock)
    {
        LockFriend.setLink(newLock, head);
        head = newLock;
        count++;
    }

    /*
     * Discard the element following the one pointed at. If it is the first
     * element (current = 0) then simply change the head pointer. Beware if
     * current points at the last element or the list is empty! This probably
     * indicates a bug in the caller.
     */

    public final void forgetNext (Lock current)
    {
        if (count > 0) /* something there to forget */
        {
            if (current == null)
                head = LockFriend.getLink(head);
            else
            {
                Lock nextOne = LockFriend.getLink(current);

                /* See if at list end */

                if (nextOne != null)
                    LockFriend.setLink(current, LockFriend.getLink(nextOne));
                else
                {
                    /*
                     * Probably an error - being asked to forget element after
                     * end of list
                     */
                    count++;
                    LockFriend.setLink(current, null);  /* force end of list */
                }
            }

            count--;
        }
    }

    public final int entryCount ()
    {
        return count;
    }

    protected Lock head;

    private int count;

}
