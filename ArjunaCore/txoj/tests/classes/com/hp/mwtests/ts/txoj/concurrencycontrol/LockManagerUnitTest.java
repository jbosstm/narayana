/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.concurrencycontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.txoj.LockResult;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class LockManagerUnitTest
{
    @Test
    public void test () throws Throwable
    {
        AtomicObject obj = new AtomicObject();

        obj = new AtomicObject();
        
        assertTrue(obj.releaselock(new Uid()));
        assertEquals(obj.setlock(null), LockResult.REFUSED);
        
        obj.print(new PrintWriter(new ByteArrayOutputStream()));
        obj.printState(new PrintWriter(new ByteArrayOutputStream()));
        
        assertEquals(new DummyLockManager().type(), "StateManager/LockManager");
    }
}