/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.txoj;

import com.arjuna.ats.txoj.Lock;


public class LockListIterator
{

    public LockListIterator(LockList L)
    {
        currentList = L;
        next = currentList.head;
    }

    public final synchronized Lock iterate ()
    {
        Lock current = next;

        if (current == null)
        {
            return null;
        }
        else
            next = LockFriend.getLink(current);

        return current;
    }

    public final synchronized void reset ()
    {
        next = null;
    }

    private LockList currentList;

    private Lock next;

}