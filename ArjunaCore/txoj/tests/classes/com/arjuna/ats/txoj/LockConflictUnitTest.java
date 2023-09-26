/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.txoj;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

import com.arjuna.ats.internal.txoj.LockConflictManager;

public class LockConflictUnitTest
{
    @Test
    public void test () throws Exception
    {
        ReentrantLock lock = new ReentrantLock();
        LockConflictManager manager = new LockConflictManager(lock);
        
        lock.lock();
        
        assertTrue(manager.wait(1, 100) != -1);
        assertTrue(manager.wait(LockManager.waitTotalTimeout, 100) != -1);
        
        lock.unlock();
        
        manager.signal();
    }
}