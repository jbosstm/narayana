/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.concurrencycontrol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.internal.txoj.LockList;
import com.arjuna.ats.internal.txoj.LockListIterator;

public class LockListIteratorUnitTest
{
    @Test
    public void test () throws Exception
    {
        LockList list = new LockList();
        LockListIterator iter = new LockListIterator(list);
        
        assertEquals(iter.iterate(), null);
        
        iter.reset();
    }
}