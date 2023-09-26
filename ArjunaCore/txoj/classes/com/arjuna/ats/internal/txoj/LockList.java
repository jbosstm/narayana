/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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