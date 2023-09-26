/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.concurrencycontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockStatus;

public class LockUnitTest
{
    @Test
    public void test () throws Exception
    {
        Lock lock = new Lock();
        
        lock = new Lock(LockMode.WRITE);
        
        assertTrue(lock.getAllOwners() != null);
        assertEquals(lock.getCurrentStatus(), LockStatus.LOCKFREE);
        
        assertFalse(lock.equals(new Object()));
        assertFalse(lock.equals((Object) new Lock()));
        assertTrue(lock.equals(lock));
        
        assertTrue(lock.toString() != null);
        
        assertEquals(lock.type(), "/StateManager/Lock");
        
        lock.print(new PrintWriter(new ByteArrayOutputStream()));
    }
}